package ch.poiuqwer.saitek.fip4j;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
public class Device {
    private static final Logger LOGGER = LoggerFactory.getLogger(Device.class);
    private final String serialNumber;
    private final Pointer pointer;

    private boolean connected = true;

    private final Set<Consumer<Page>> pageActivatedCallbacks = new HashSet<>();
    private final Set<Consumer<Page>> pageDeactivatedCallbacks = new HashSet<>();
    private final Set<Consumer<Button>> buttonPressedCallbacks = new HashSet<>();
    private final Set<Consumer<Button>> buttonReleasedCallbacks = new HashSet<>();
    private final Set<BiConsumer<Knob, TurnDirection>> knobTurnedCallbacks = new HashSet<>();

    private final List<Page> pages = new ArrayList<>();
    private Page activePage;

    private Set<Button> downButtons = new HashSet<>();

    public Device(Pointer pointer, String serialNumber) {
        this.pointer = pointer;
        this.serialNumber = serialNumber;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Page addPage() {
        Preconditions.checkState(connected);
        Page page = new Page(this, pages.size());
        pages.add(page);
        LibraryManager.getDirectOutput().addPage(page, PageState.ACTIVE);
        activePage = page;
        return page;
    }

    public void removePage(Page page) {
        LibraryManager.getDirectOutput().removePage(page);
        page.kill();
    }

    public Page getActivePage() {
        Preconditions.checkState(connected);
        return activePage;
    }

    void onPageActivated(Consumer<Page> callback) {
        pageActivatedCallbacks.add(callback);
    }

    void onPageDeactivated(Consumer<Page> callback) {
        pageDeactivatedCallbacks.add(callback);
    }

    void onButtonPressed(Consumer<Button> callback) {
        buttonPressedCallbacks.add(callback);
    }

    void onButtonReleased(Consumer<Button> callback) {
        buttonReleasedCallbacks.add(callback);
    }

    void onKnobTurned(BiConsumer<Knob, TurnDirection> callback) {
        knobTurnedCallbacks.add(callback);
    }

    void handlePageChange(int dwPage, byte bSetActive) {
        Preconditions.checkState(connected);
        if (activePage != null && activePage.getIndex() == dwPage) {
            if (bSetActive == 0) {
                activePage.deactivate();
                CallbackHandler.executeAll(pageDeactivatedCallbacks, activePage);
                activePage = null;
            }
        } else {
            if (bSetActive == 1) {
                activePage = pages.get(dwPage);
                activePage.activate();
                CallbackHandler.executeAll(pageActivatedCallbacks, activePage);
            }
        }
    }


    void handleSoftButtonChange(int dwButtons) {
        Set<Button> newDownButtons = new HashSet<>();
        Set<Button> pressedButtons = new HashSet<>();
        Set<Button> releasedButtons = new HashSet<>();
        determineButtonStates(dwButtons, newDownButtons, pressedButtons, releasedButtons);
        fireSoftButtonEvents(dwButtons, pressedButtons, releasedButtons);
        downButtons = newDownButtons;
    }

    void handleKnobChange(int dwButtons) {
        for (Knob knob : Knob.values()) {
            if (turnedClockwise(knob, dwButtons)) {
                CallbackHandler.executeAll(knobTurnedCallbacks, knob, TurnDirection.CLOCKWISE);
            } else if (turnedCounterclockwise(knob, dwButtons)) {
                CallbackHandler.executeAll(knobTurnedCallbacks, knob, TurnDirection.COUNTERCLOCKWISE);
            }
        }
    }

    private boolean turnedCounterclockwise(Knob knob, int dwButtons) {
        return (knob.counterclockwiseValue & dwButtons) != 0;
    }

    private boolean turnedClockwise(Knob knob, int dwButtons) {
        return (knob.clockwiseValue & dwButtons) != 0;
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

    private void fireSoftButtonEvents(int dwButtons, Set<Button> pressedButtons, Set<Button> releasedButtons) {
        for (Button pressed : pressedButtons) {
            CallbackHandler.executeAll(buttonPressedCallbacks, pressed);
        }
        for (Button released : releasedButtons) {
            CallbackHandler.executeAll(buttonReleasedCallbacks, released);
        }
    }

    void disconnect() {
        this.connected = false;
        pages.forEach(Page::deactivate);
    }

    Pointer getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        return "Device{" +
                "S/N='" + serialNumber + '\'' +
                '}';
    }
}
