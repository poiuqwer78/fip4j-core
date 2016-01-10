package ch.poiuqwer.saitek.fip4j;

import java.awt.image.BufferedImage;

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
public class Page {

    private final int index;
    private final Device device;
    private final DisplayBuffer displayBuffer;
    private final DirectOutput directOutput;

    private boolean active = true;
    private boolean alive = true;

    public Page(Device device, int index) {
        this.index = index;
        this.device = device;
        this.displayBuffer = new DisplayBuffer();
        this.directOutput = LibraryManager.getDirectOutput();
    }

    public void activate() {
        if (alive) {
            active = true;
        }
    }

    public void deactivate() {
        if (alive) {
            active = false;
        }
    }

    public void kill() {
        alive = false;
    }

    public int getIndex() {
        return index;
    }

    public Device getDevice() {
        return device;
    }

    DisplayBuffer getDisplayBuffer() {
        return displayBuffer;
    }

    public void setLed(Button button, LedState state) {
        if (active && alive) {
            directOutput.setLed(this, button, state);
        }
    }

    public void setImage(BufferedImage image) {
        if (active && alive) {
            directOutput.setImage(this, image);
        }
    }

    public void clearScreen() {
        if (active && alive) {
            directOutput.clearScreen(this);
        }
    }

    public void addSoftButtonEventHandler(SoftButtonListener handler) {
        device.addSoftButtonListener(handler);
    }

    public void removeSoftButtonEventHandler(SoftButtonListener handler) {
        device.removeSoftButtonListener(handler);
    }

    @Override
    public String toString() {
        return "Page{" +
                "index=" + index +
                '}';
    }

}
