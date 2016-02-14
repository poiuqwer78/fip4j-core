package ch.poiuqwer.saitek.fip4j;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

import static ch.poiuqwer.saitek.fip4j.ButtonState.PRESSED;
import static ch.poiuqwer.saitek.fip4j.ButtonState.RELEASED;
import static ch.poiuqwer.saitek.fip4j.DeviceState.*;
import static ch.poiuqwer.saitek.fip4j.PageState.ACTIVE;
import static ch.poiuqwer.saitek.fip4j.PageState.INACTIVE;

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
@SuppressWarnings("unused")
public class DirectOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectOutput.class);

    private static final GUID FIP = GUID.fromString("{3E083CD8-6A37-4A58-80A8-3D6A2C07513E}");
    private static final GUID X52 = GUID.fromString("{29DAD506-F93B-4F20-85FA-1E02C04FAC17}");

    private final Library dll;
    private final EventBus eventBus;

    private Set<Button> downButtons = new HashSet<>();

    private final Map<Pointer, Device> devices = new HashMap<>();

    private final Library.Pfn_DirectOutput_DeviceChange deviceChangeCallback =
            (hDevice, bAdded, pCtxt) -> handleDeviceChange(hDevice, bAdded);
    private final Library.Pfn_DirectOutput_SoftButtonChange softButtonCallback =
            (Pointer hDevice, int dwButtons, Pointer pCtxt) -> handleSoftButtonEvent(hDevice, dwButtons);
    private final Library.Pfn_DirectOutput_PageChange pageChangeCallback =
            (Pointer hDevice, int dwPage, byte bSetActive, Pointer pCtxt) -> handlePageChangeEvent(hDevice, dwPage, bSetActive);

    DirectOutput(Library dll, EventBus eventBus) {
        this.dll = dll;
        this.eventBus = eventBus;
    }

    public void setup(String pluginName) {
        initialize(pluginName);
        enumerateDevices();
        registerDeviceChangeCallback();
    }

    public void cleanup() {
        call(dll.DirectOutput_Deinitialize());
    }

    public Collection<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    public void registerSubscriber(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregisterSubscriber(Object subscriber) {
        eventBus.unregister(subscriber);
    }

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
            eventBus.post(new DeviceEvent(device, DISCONNECTED));
        }
    }

    private void handleDeviceConnected(Pointer hDevice) {
        if (isFlightInstrumentPanel(hDevice)) {
            Device device = setupDevice(hDevice);
            devices.put(hDevice, device);
            eventBus.post(new DeviceEvent(device, CONNECTED));
        }
    }

    private void handleSoftButtonEvent(Pointer hDevice, int dwButtons) {
        LOGGER.debug("Soft Button State: {}", dwButtons);
        Device device = devices.get(hDevice);
        if (device != null) {
            handleSoftButtonChange(device, dwButtons);
            handleKnobChange(device, dwButtons);
        }
    }

    private void handleSoftButtonChange(Device device, int dwButtons) {
        Set<Button> newDownButtons = new HashSet<>();
        Set<Button> pressedButtons = new HashSet<>();
        Set<Button> releasedButtons = new HashSet<>();
        determineButtonStates(dwButtons, newDownButtons, pressedButtons, releasedButtons);
        fireSoftButtonEvents(device, dwButtons, pressedButtons, releasedButtons);
        downButtons = newDownButtons;
    }

    private void determineButtonStates(int dwButtons, Set<Button> newDownButtons, Set<Button> pressedButtons, Set<Button> releasedButtons) {
        for (int i = 1; i <= 6; i++) {
            Button s = Button.S(i);
            if (pressed(s, dwButtons)) {
                newDownButtons.add(Button.S(i));
                if (!downButtons.contains(s)) {
                    pressedButtons.add(s);
                }
            } else {
                if (downButtons.contains(s)) {
                    releasedButtons.add(s);
                }
            }
        }
    }

    private boolean pressed(Button s, int dwButtons) {
        return (s.value & dwButtons) != 0;
    }

    private void fireSoftButtonEvents(Device device, int dwButtons, Set<Button> pressedButtons, Set<Button> releasedButtons) {
        Page page = device.getActivePage();
        for (Button pressed : pressedButtons) {
            eventBus.post(new ButtonEvent(page, pressed, PRESSED));
        }
        for (Button released : releasedButtons) {
            eventBus.post(new ButtonEvent(page, released, RELEASED));
        }
    }

    private void handleKnobChange(Device device, int dwButtons) {
        Page page = device.getActivePage();
        for (Knob knob : Knob.values()) {
            if (turnedClockwise(knob, dwButtons)) {
                eventBus.post(new KnobEvent(page, knob, KnobState.TURNED_CLOCKWISE));
            } else if (turnedCounterclockwise(knob, dwButtons)) {
                eventBus.post(new KnobEvent(page, knob, KnobState.TURNED_COUNTERCLOCKWISE));
            }
        }
    }

    private boolean turnedCounterclockwise(Knob knob, int dwButtons) {
        return (knob.counterclockwiseValue & dwButtons) != 0;
    }

    private boolean turnedClockwise(Knob knob, int dwButtons) {
        return (knob.clockwiseValue & dwButtons) != 0;
    }

    private void handlePageChangeEvent(Pointer hDevice, int dwPage, byte bSetActive) {
        LOGGER.debug("Page Change - Page: {} Active: {}", dwPage, bSetActive);
        Device device = devices.get(hDevice);
        if (device != null) {
            handlePageChange(device, dwPage, bSetActive);
        }
    }

    private void handlePageChange(Device device, int dwPage, byte bSetActive) {
        Page activePage = device.getActivePage();
        if (activePage != null && activePage.getIndex() == dwPage) {
            if (bSetActive == 0) {
                activePage.deactivate();
                device.setActivePage(null);
                eventBus.post(new PageEvent(activePage, INACTIVE));
            }
        } else {
            if (bSetActive == 1) {
                Page newActivePage = device.getPages().get(dwPage);
                newActivePage.activate();
                device.setActivePage(newActivePage);
                eventBus.post(new PageEvent(newActivePage, ACTIVE));
            }
        }
    }

    private void registerDeviceChangeCallback() {
        call(dll.DirectOutput_RegisterDeviceCallback(deviceChangeCallback, null));
    }

    private void initialize(String pluginName) {
        call(dll.DirectOutput_Initialize(new WString(pluginName)));
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
        Device result = new Device(this, devicePointer, serialNumber);
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
        } catch (UnsatisfiedLinkError e) {
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

    EventBus getEventBus() {
        return eventBus;
    }

    private void call(int code) {
        HRESULT result = HRESULT.of(code);
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
