package ch.poiuqwer.saitek.fip4j.impl;

import com.google.common.base.Preconditions;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;

/**
 * Copyright 2015 Hermann Lehner
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DirectOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectOutput.class);

    public static final GUID FIP = GUID.fromString("{3E083CD8-6A37-4A58-80A8-3D6A2C07513E}");
    public static final GUID X52 = GUID.fromString("{29DAD506-F93B-4F20-85FA-1E02C04FAC17}");
    public static final int DISPLAY_WIDTH = 320;
    public static final int DISPLAY_HEIGHT = 240;
    public static final int DISPLAY_COLOR_DEPTH = 3;

    private final Library dll;

    private Map<Pointer, Device> devices = new HashMap<>();

    public DirectOutput(Library dll) {
        this.dll = dll;
    }

    private final Library.Pfn_DirectOutput_DeviceChange deviceChangeCallback = (hDevice, bAdded, pCtxt) -> {
        if (bAdded == 1) {
            if (isFlightInstrumentPanel(hDevice)) {
                Device device = buildDevice(hDevice);
                devices.put(hDevice, device);
            }
        } else {
            if (devices.containsKey(hDevice)) {
                LOGGER.info("Device removed: {}", devices.get(hDevice).toString());
                devices.remove(hDevice);
            }
        }
    };

    private HRESULT result;

    public HRESULT getResult() {
        return result;
    }

    public void initialize(String pluginName) {
        call(dll.DirectOutput_Initialize(new WString(pluginName)));
        loadDevices();
        call(dll.DirectOutput_RegisterDeviceCallback(deviceChangeCallback, null));
    }

    public void deinitialize() {
        call(dll.DirectOutput_Deinitialize());
    }

    public Collection<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    public void loadDevices() {
        List<Pointer> devicePointers = new ArrayList<>();
        call(dll.DirectOutput_Enumerate((hDevice, pCtxt) -> devicePointers.add(hDevice), null));
        if (devicePointers.size() > 0) {
            LOGGER.debug("Found {} device(s). Will check if they are Saitek Pro Flight Instrument Panels.", devicePointers.size());
            for (Pointer devicePointer : devicePointers) {
                if (isFlightInstrumentPanel(devicePointer)) {
                    Device device = buildDevice(devicePointer);
                    devices.put(devicePointer, device);
                }
            }
        }
        if (devices.isEmpty()) {
            LOGGER.info("No Saitek Pro Flight Instrument Panel could be found.");
        }

    }

    private Device buildDevice(Pointer devicePointer) {
        GUID deviceGUID = getDeviceGuid(devicePointer);
        String serialNumber = getSerialNumber(devicePointer);
        Device result = new Device(devicePointer, deviceGUID, serialNumber);
        LOGGER.info(result.toString());
        return result;
    }

    private String getSerialNumber(Pointer devicePointer) {
        Memory serialNumberMemory = new Memory(32);
        char[] serialNumberCharArray = new char[16];
        call(dll.DirectOutput_GetSerialNumber(devicePointer, serialNumberMemory, 16));
        serialNumberMemory.read(0, serialNumberCharArray, 0, 16);
        return String.valueOf(serialNumberCharArray).split("\0")[0];
    }

    private GUID getTypeGuid(Pointer devicePointer) {
        Memory guidMemory = new Memory(16);
        call(dll.DirectOutput_GetDeviceType(devicePointer, guidMemory));
        return GUID.fromBinary(guidMemory.getByteArray(0, 16));
    }

    private GUID getDeviceGuid(Pointer devicePointer) {
        Memory guidMemory = new Memory(16);
        call(dll.DirectOutput_GetDeviceInstance(devicePointer, guidMemory));
        return GUID.fromBinary(guidMemory.getByteArray(0, 16));
    }

    private boolean isFlightInstrumentPanel(Pointer devicePointer) {
        GUID deviceTypeGUID = getTypeGuid(devicePointer);
        if (FIP.equals(deviceTypeGUID)) {
            LOGGER.debug("Saitek Pro Flight Instrument Panel found.");
            return true;
        } else if (X52.equals(deviceTypeGUID)) {
            LOGGER.debug("Saitek X52 Pro found.");
        } else {
            LOGGER.debug("Unknown device found with GUID {}.", deviceTypeGUID);
        }
        return false;
    }

    public void addPage(Page page, PageState state) {
        call(dll.DirectOutput_AddPage(
                page.getDevice().getPointer(),
                page.getIndex(),
                new WString(Integer.toString(page.getIndex())),
                state.getValue()));
    }

    public void removePage(Page page) {
        call(dll.DirectOutput_RemovePage(
                page.getDevice().getPointer(),
                page.getIndex()));
    }

    public void setLed(Page page, Button button, LedState state) {
        call(dll.DirectOutput_SetLed(
                page.getDevice().getPointer(),
                page.getIndex(),
                button.getLed(),
                state.getValue()));
    }

    public void setImage(Page page, BufferedImage image) {
        Preconditions.checkArgument(image.getType() == BufferedImage.TYPE_3BYTE_BGR, "image needs to be of type 3BYTE_BGR.");
        Preconditions.checkArgument(image.getWidth() == DISPLAY_WIDTH, "image width needs to be %s", DISPLAY_WIDTH);
        Preconditions.checkArgument(image.getHeight() == DISPLAY_HEIGHT, "image height needs to be %s", DISPLAY_HEIGHT);
        byte[] bytes = bufferedImageToBytes(image);
        Memory memory = bytesToMemory(bytes);
        call(dll.DirectOutput_SetImage(
                page.getDevice().getPointer(),
                page.getIndex(),
                0,
                bytes.length,
                memory));
    }

    public void clearScreen(Page page) {
        int size = DISPLAY_WIDTH * DISPLAY_HEIGHT * DISPLAY_COLOR_DEPTH;
        Memory memory = new Memory(size);
        memory.clear();
        call(dll.DirectOutput_SetImage(
                page.getDevice().getPointer(),
                page.getIndex(),
                0,
                size,
                memory));
    }

    private byte[] bufferedImageToBytes(BufferedImage image) {
        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    private Memory bytesToMemory(byte[] bytes) {
        int size = bytes.length;
        Memory image = new Memory(size);
        int lineLength = DISPLAY_WIDTH * DISPLAY_COLOR_DEPTH;
        int pointerOffset = size - lineLength;
        for (int i = 0; i < DISPLAY_HEIGHT; i++) {
            image.write(pointerOffset - (i * lineLength), bytes, i * lineLength, lineLength);
        }
        return image;
    }

    private void call(int code) {
        result = HRESULT.of(code);
        String methodName;
        switch (result) {
            case S_OK:
                if (LOGGER.isDebugEnabled()) {
                    // Expensive operation, only perform if logging really happens on this level.
                    methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                    LOGGER.debug("Call '{}' {}", methodName, result);
                }
                break;
            case W_UNKNOWN:
                methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                LOGGER.warn("Call '{}' {}", methodName, result);
                break;
            default:
                methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                LOGGER.error("Call '{}' {}", methodName, result);
        }
    }

}
