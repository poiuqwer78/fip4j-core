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

    private static final GUID FIP = GUID.fromString("{3E083CD8-6A37-4A58-80A8-3D6A2C07513E}");
    private static final GUID X52 = GUID.fromString("{29DAD506-F93B-4F20-85FA-1E02C04FAC17}");
    private static final int DISPLAY_WIDTH = 320;
    private static final int DISPLAY_HEIGHT = 240;
    private static final int DISPLAY_COLOR_DEPTH = 3;

    private final Library dll;

    private final Map<Pointer, Device> devices = new HashMap<>();

    private final Set<DeviceChangeEventHandler> deviceChangeEventHandlers = new HashSet<>();

    public DirectOutput(Library dll) {
        this.dll = dll;
    }

    private final Library.Pfn_DirectOutput_DeviceChange deviceChangeCallback =
            (hDevice, bAdded, pCtxt) -> handleDeviceChange(hDevice, bAdded);

    private final Library.Pfn_DirectOutput_SoftButtonChange softButtonCallback =
            (Pointer hDevice, int dwButtons, Pointer pCtxt) -> fireSoftButtonEvents(hDevice, dwButtons);

    private final Library.Pfn_DirectOutput_PageChange pageChangeCallback =
            (Pointer hDevice, int dwPage, byte bSetActive, Pointer pCtxt) -> firePageChangeEvents(hDevice, dwPage, bSetActive);

    private void handleDeviceChange(Pointer hDevice, byte bAdded) {
        if (bAdded == 1) {
            handleDeviceConnected(hDevice);
        } else {
            handleDeviceDisconnected(hDevice);
        }
    }

    private void handleDeviceDisconnected(Pointer hDevice) {
        if (devices.containsKey(hDevice)) {
            LOGGER.info("Device disconnected: {}", devices.get(hDevice).toString());
            Device device = devices.remove(hDevice);
            device.disconnect();
            for (DeviceChangeEventHandler handler : deviceChangeEventHandlers) {
                handler.deviceDisconnected(device);
            }
        }
    }

    private void handleDeviceConnected(Pointer hDevice) {
        if (isFlightInstrumentPanel(hDevice)) {
            Device device = setupDevice(hDevice);
            devices.put(hDevice, device);
            for (DeviceChangeEventHandler handler : deviceChangeEventHandlers) {
                handler.deviceConnected(device);
            }
        }
    }

    public void addDeviceChangeEventHandler(DeviceChangeEventHandler handler) {
        deviceChangeEventHandlers.add(handler);
    }

    public void removeDeviceChangeEventHandler(DeviceChangeEventHandler handler) {
        deviceChangeEventHandlers.remove(handler);
    }

    private void fireSoftButtonEvents(Pointer hDevice, int dwButtons) {
        LOGGER.debug("Soft Button State: {}", dwButtons);
        Device device = devices.get(hDevice);
        if (device != null) {
            device.fireSoftButtonEventHandlers(dwButtons);
        }
    }

    private void firePageChangeEvents(Pointer hDevice, int dwPage, byte bSetActive) {
        LOGGER.debug("Page Change - Page: {} Active: {}", dwPage, bSetActive);
        Device device = devices.get(hDevice);
        if (device != null) {
            device.firePageChangeEventHandlers(dwPage, bSetActive);
        }
    }

    private HRESULT result;

    @SuppressWarnings("unused")
    public HRESULT getResult() {
        return result;
    }

    public void setup(String pluginName) {
        initialize(pluginName);
        enumerateDevices();
        registerDeviceChangeCallback();
    }

    private void registerDeviceChangeCallback() {
        call(dll.DirectOutput_RegisterDeviceCallback(deviceChangeCallback, null));
    }

    private void initialize(String pluginName) {
        call(dll.DirectOutput_Initialize(new WString(pluginName)));
    }

    public void deinitialize() {
        call(dll.DirectOutput_Deinitialize());
    }

    public Collection<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    private void enumerateDevices() {
        List<Pointer> devicePointers = new ArrayList<>();
        call(dll.DirectOutput_Enumerate((hDevice, pCtxt) -> devicePointers.add(hDevice), null));
        if (devicePointers.size() > 0) {
            LOGGER.debug("Found {} device(s). Will check if they are Saitek Pro Flight Instrument Panels.", devicePointers.size());
            for (Pointer devicePointer : devicePointers) {
                if (isFlightInstrumentPanel(devicePointer)) {
                    Device device = setupDevice(devicePointer);
                    devices.put(devicePointer, device);
                }
            }
        }
        if (devices.isEmpty()) {
            LOGGER.info("No Saitek Pro Flight Instrument Panel could be found.");
        }

    }

    private Device setupDevice(Pointer devicePointer) {
        String serialNumber = getSerialNumber(devicePointer);
        Device result = new Device(devicePointer, serialNumber);
        registerSoftButtonCallback(devicePointer);
        registerPageChangeCallback(devicePointer);
        LOGGER.info("Device connected: {}", result.toString());
        return result;
    }

    private void registerPageChangeCallback(Pointer devicePointer) {
        call(dll.DirectOutput_RegisterPageCallback(devicePointer, pageChangeCallback, null));
    }

    private void registerSoftButtonCallback(Pointer devicePointer) {
        call(dll.DirectOutput_RegisterSoftButtonCallback(devicePointer, softButtonCallback, null));
    }


    private String getSerialNumber(Pointer devicePointer) {
        Memory serialNumberMemory = new Memory(32);
        serialNumberMemory.clear();
        char[] serialNumberCharArray = new char[16];
        call(dll.DirectOutput_GetSerialNumber(devicePointer, serialNumberMemory, 16));
        serialNumberMemory.read(0, serialNumberCharArray, 0, 16);
        return String.valueOf(serialNumberCharArray).split("\0")[0];
    }

    private GUID getTypeGuid(Pointer devicePointer) {
        Memory guidMemory = new Memory(16);
        guidMemory.clear();
        call(dll.DirectOutput_GetDeviceType(devicePointer, guidMemory));
        return GUID.fromBinary(guidMemory.getByteArray(0, 16));
    }

    @SuppressWarnings("unused")
    private GUID getDeviceGuid(Pointer devicePointer) {
        Memory guidMemory = new Memory(16);
        guidMemory.clear();
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
                button.LED,
                state.VALUE));
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
