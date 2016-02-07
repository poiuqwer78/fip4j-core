package ch.poiuqwer.saitek.fip4j;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
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

    private final Library dll;
    private final Map<Pointer, Device> devices = new HashMap<>();
    private final Set<DeviceChangeListener> deviceChangeListeners = new HashSet<>();

    private HRESULT result;


    DirectOutput(Library dll) {
        this.dll = dll;
    }

    @SuppressWarnings("unused")
    HRESULT getResult() {
        return result;
    }

    private final Library.Pfn_DirectOutput_DeviceChange deviceChangeCallback =
            (hDevice, bAdded, pCtxt) -> handleDeviceChange(hDevice, bAdded);

    private final Library.Pfn_DirectOutput_SoftButtonChange softButtonCallback =
            (Pointer hDevice, int dwButtons, Pointer pCtxt) -> handleSoftButtonEvent(hDevice, dwButtons);

    private final Library.Pfn_DirectOutput_PageChange pageChangeCallback =
            (Pointer hDevice, int dwPage, byte bSetActive, Pointer pCtxt) -> handlePageChangeEvent(hDevice, dwPage, bSetActive);

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
            fireDeviceDisconnectedEvents(device);
        }
    }

    private void fireDeviceDisconnectedEvents(Device device) {
        for (DeviceChangeListener handler : deviceChangeListeners) {
            handler.deviceDisconnected(device);
        }
    }

    private void handleDeviceConnected(Pointer hDevice) {
        if (isFlightInstrumentPanel(hDevice)) {
            Device device = setupDevice(hDevice);
            devices.put(hDevice, device);
            fireDeviceConnectedEvents(device);
        }
    }

    private void fireDeviceConnectedEvents(Device device) {
        for (DeviceChangeListener handler : deviceChangeListeners) {
            handler.deviceConnected(device);
        }
    }

    @SuppressWarnings("unused")
    public void addDeviceChangeListener(DeviceChangeListener listener) {
        deviceChangeListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeDeviceChangeListener(DeviceChangeListener listener) {
        deviceChangeListeners.remove(listener);
    }

    private void handleSoftButtonEvent(Pointer hDevice, int dwButtons) {
        LOGGER.debug("Soft Button State: {}", dwButtons);
        Device device = devices.get(hDevice);
        if (device != null) {
            device.handleSoftButtonChange(dwButtons);
        }
    }

    private void handlePageChangeEvent(Pointer hDevice, int dwPage, byte bSetActive) {
        LOGGER.debug("Page Change - Page: {} Active: {}", dwPage, bSetActive);
        Device device = devices.get(hDevice);
        if (device != null) {
            device.handlePageChange(dwPage, bSetActive);
        }
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

    public void cleanup() {
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
            //noinspection Convert2streamapi
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
        try {
            call(dll.DirectOutput_GetSerialNumber(devicePointer, serialNumberMemory, 16));
        } catch (UnsatisfiedLinkError e){
            LOGGER.warn("Could not retrieve serial number. Upgrade of DirectOutput driver required.");
            return "N/A";
        }
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

    void addPage(Page page, PageState state) {
        call(dll.DirectOutput_AddPage(
                page.getDevice().getPointer(),
                page.getIndex(),
                new WString(Integer.toString(page.getIndex())),
                state.value));
    }

    void removePage(Page page) {
        call(dll.DirectOutput_RemovePage(
                page.getDevice().getPointer(),
                page.getIndex()));
    }

    void setLed(Page page, Button button, LedState state) {
        call(dll.DirectOutput_SetLed(
                page.getDevice().getPointer(),
                page.getIndex(),
                button.led,
                state.value));
    }

    void setImage(Page page, BufferedImage image) {
        DisplayBuffer buffer = page.getDisplayBuffer();
        buffer.loadImage(image);
        call(dll.DirectOutput_SetImage(
                page.getDevice().getPointer(),
                page.getIndex(),
                0,
                buffer.getSize(),
                buffer.getMemory()));
    }

    void clearScreen(Page page) {
        DisplayBuffer buffer = page.getDisplayBuffer();
        buffer.clear();
        call(dll.DirectOutput_SetImage(
                page.getDevice().getPointer(),
                page.getIndex(),
                0,
                buffer.getSize(),
                buffer.getMemory()));
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
