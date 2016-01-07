package ch.poiuqwer.saitek.fip4j.impl;

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

    private final List<Page> pages = new ArrayList<>();

    private Page activePage;

    private Set<Button> downButtons = new HashSet<>();

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

    private boolean connected = true;

    @SuppressWarnings("unused")
    public boolean isConnected() {
        return connected;
    }

    private final Set<PageChangeEventHandler> pageChangeEventHandlers = new HashSet<>();
    private final Set<SoftButtonEventHandler> softButtonEventHandlers = new HashSet<>();

    public Device(Pointer pointer, String serialNumber) {
        this.pointer = pointer;
        this.serialNumber = serialNumber;
    }

    public void addPageChangeEventHandler(PageChangeEventHandler handler) {
        pageChangeEventHandlers.add(handler);
    }

    @SuppressWarnings("unused")
    public void removePageChangeEventHandler(PageChangeEventHandler handler) {
        pageChangeEventHandlers.remove(handler);
    }

    public void addSoftButtonEventHandler(SoftButtonEventHandler handler) {
        softButtonEventHandlers.add(handler);
    }

    @SuppressWarnings("unused")
    public void removeSoftButtonEventHandler(SoftButtonEventHandler handler) {
        softButtonEventHandlers.remove(handler);
    }

    void firePageChangeEventHandlers(int dwPage, byte bSetActive) {
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
        for (PageChangeEventHandler handler : pageChangeEventHandlers) {
            if (bSetActive == 1) {
                try {
                    handler.pageActivated(pages.get(dwPage));
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            } else {
                try {
                    handler.pageDeactivated(pages.get(dwPage));
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            }
        }
    }

    void fireSoftButtonEventHandlers(int dwButtons) {
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
        for (SoftButtonEventHandler handler : softButtonEventHandlers) {
            for (Button pressed : pressedButtons) {
                try {
                    handler.buttonPressed(pressed);
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            }
            for (Button released : releasedButtons) {
                try {
                    handler.buttonReleased(released);
                } catch (Throwable t) {
                    LOGGER.error("Error in EventHandler.", t);
                }
            }
            for (Knob knob : Knob.values()) {
                if (turnedClockwise(knob, dwButtons)) {
                    try {
                        handler.knobTurnUp(knob);
                    } catch (Throwable t) {
                        LOGGER.error("Error in EventHandler.", t);
                    }
                } else if (turnedCounterClockwise(knob, dwButtons)) {
                    try {
                        handler.knobTurnDown(knob);
                    } catch (Throwable t) {
                        LOGGER.error("Error in EventHandler.", t);
                    }
                }
            }
        }
        downButtons = newDownButtons;
    }

    private boolean pressed(Button s, int dwButtons) {
        return (s.VALUE & dwButtons) != 0;
    }

    private boolean turnedCounterClockwise(Knob knob, int dwButtons) {
        return (knob.COUNTER_CLOCK_WISE & dwButtons) != 0;
    }

    private boolean turnedClockwise(Knob knob, int dwButtons) {
        return (knob.CLOCK_WISE & dwButtons) != 0;
    }


    void disconnect() {
        this.connected = false;

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
