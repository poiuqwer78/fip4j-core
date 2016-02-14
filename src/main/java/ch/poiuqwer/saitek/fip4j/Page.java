package ch.poiuqwer.saitek.fip4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
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
public final class Page {
    private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);
    private final int index;
    private final Device device;
    private final DisplayBuffer displayBuffer;
    private final DirectOutput directOutput;

    private boolean active = true;
    private boolean alive = true;

    Page(Device device, int index) {
        this.index = index;
        this.device = device;
        this.displayBuffer = new DisplayBuffer();
        this.directOutput = device.getDirectOutput();
    }

    void activate() {
        if (alive) {
            active = true;
        }
    }

    void deactivate() {
        if (alive) {
            active = false;
        }
    }

    public Device getDevice() {
        return device;
    }

    public DirectOutput getDirectOutput() {
        return directOutput;
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

    @Override
    public String toString() {
        return "Page{" +
                "index=" + index +
                '}';
    }

    DisplayBuffer getDisplayBuffer() {
        return displayBuffer;
    }

    void kill() {
        alive = false;
    }

    int getIndex() {
        return index;
    }

}
