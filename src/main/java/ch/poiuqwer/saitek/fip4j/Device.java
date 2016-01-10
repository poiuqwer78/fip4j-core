package ch.poiuqwer.saitek.fip4j;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class Device {
    private static final Logger LOGGER = LoggerFactory.getLogger(Device.class);
    private final String serialNumber;
    private final Pointer pointer;

    private boolean connected = true;

    private final List<Page> pages = new ArrayList<>();
    private Page activePage;

    private Set<Button> downButtons = new HashSet<>();

    private final Set<PageChangeListener> pageChangeListeners = new HashSet<>();
    private final Set<SoftButtonListener> softButtonListeners = new HashSet<>();

    public Device(Pointer pointer, String serialNumber) {
        this.pointer = pointer;
        this.serialNumber = serialNumber;
    }

    public Page addPage() {
        Preconditions.checkState(connected);
        Page page = new Page(this, pages.size());
        pages.add(page);
        LibraryManager.getDirectOutput().addPage(page, PageState.ACTIVE);
        activePage = page;
        return page;
    }

    @SuppressWarnings("unused")
    public Page getActivePage() {
        Preconditions.checkState(connected);
        return activePage;
    }


    @SuppressWarnings("unused")
    public void removePage(Page page) {
        LibraryManager.getDirectOutput().removePage(page);
        page.kill();
    }

    @SuppressWarnings("unused")
    public boolean isConnected() {
        return connected;
    }

    public void addPageChangeListener(PageChangeListener listener) {
        pageChangeListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removePageChangeListener(PageChangeListener listener) {
        pageChangeListeners.remove(listener);
    }

    public void addSoftButtonListener(SoftButtonListener listener) {
        softButtonListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeSoftButtonListener(SoftButtonListener listener) {
        softButtonListeners.remove(listener);
    }

    void handlePageChange(int dwPage, byte bSetActive) {
        Preconditions.checkState(connected);
        if (activePage != null && activePage.getIndex() == dwPage) {
            if (bSetActive == 0) {
                activePage.deactivate();
                activePage = null;
            }
        } else {
            if (bSetActive == 1) {
                activePage = pages.get(dwPage);
                activePage.activate();
            }
        }
        firePageChangeEvents(dwPage, bSetActive);
    }

    private void firePageChangeEvents(int dwPage, byte bSetActive) {
        for (PageChangeListener listener : pageChangeListeners) {
            if (bSetActive == 1) {
                try {
                    listener.pageActivated(pages.get(dwPage));
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            } else {
                try {
                    listener.pageDeactivated(pages.get(dwPage));
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            }
        }
    }

    void handleSoftButtonChange(int dwButtons) {
        Set<Button> newDownButtons = new HashSet<>();
        Set<Button> pressedButtons = new HashSet<>();
        Set<Button> releasedButtons = new HashSet<>();
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
        fireSoftButtonEvents(dwButtons, pressedButtons, releasedButtons);
        downButtons = newDownButtons;
    }

    private void fireSoftButtonEvents(int dwButtons, Set<Button> pressedButtons, Set<Button> releasedButtons) {
        for (SoftButtonListener handler : softButtonListeners) {
            firePressedSoftButtonEvents(pressedButtons, handler);
            fireReleasedSoftButtonEvents(releasedButtons, handler);
            fireKnobEvents(dwButtons, handler);
        }
    }

    private void firePressedSoftButtonEvents(Set<Button> pressedButtons, SoftButtonListener handler) {
        for (Button pressed : pressedButtons) {
            try {
                handler.buttonPressed(pressed);
            } catch (Throwable t) {
                LOGGER.error("Error in EventHandler.", t);
            }
        }
    }

    private void fireReleasedSoftButtonEvents(Set<Button> releasedButtons, SoftButtonListener handler) {
        for (Button released : releasedButtons) {
            try {
                handler.buttonReleased(released);
            } catch (Throwable t) {
                LOGGER.error("Error in EventHandler.", t);
            }
        }
    }

    private void fireKnobEvents(int dwButtons, SoftButtonListener handler) {
        for (Knob knob : Knob.values()) {
            if (turnedClockwise(knob, dwButtons)) {
                try {
                    handler.knobTurnedClockwise(knob);
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            } else if (turnedCounterclockwise(knob, dwButtons)) {
                try {
                    handler.knobTurnedCounterclockwise(knob);
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            }
        }
    }

    private boolean pressed(Button s, int dwButtons) {
        return (s.value & dwButtons) != 0;
    }

    private boolean turnedCounterclockwise(Knob knob, int dwButtons) {
        return (knob.ccwValue & dwButtons) != 0;
    }

    private boolean turnedClockwise(Knob knob, int dwButtons) {
        return (knob.cwValue & dwButtons) != 0;
    }

    void disconnect() {
        this.connected = false;
        pages.forEach(Page::deactivate);
    }

    @SuppressWarnings("unused")
    public String getSerialNumber() {
        return serialNumber;
    }

    public Pointer getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        return "Device{" +
                "S/N='" + serialNumber + '\'' +
                '}';
    }
}
