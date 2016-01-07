package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static ch.poiuqwer.saitek.fip4j.impl.Button.DOWN;
import static ch.poiuqwer.saitek.fip4j.impl.Button.UP;
import static ch.poiuqwer.saitek.fip4j.impl.LedState.OFF;
import static ch.poiuqwer.saitek.fip4j.impl.LedState.ON;

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
public class InputDemo {

    private static Logger LOGGER = LoggerFactory.getLogger(InputDemo.class);

    private final DirectOutput directOutput;
    private final Device device;
    private final Page page;

    private transient boolean waitForKey;

    private final Set<Button> toggleButtons = new HashSet<>();

    public InputDemo(Page page) {
        this.directOutput = LibraryManager.getDirectOutput();
        this.page = page;
        this.device = page.getDevice();
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running input demo.");
        LOGGER.info("Try the buttons on the device.");
        LOGGER.info("Program stops if no action is performed on the device for more than five seconds.");
        directOutput.setLed(page, UP, OFF);
        directOutput.setLed(page, DOWN, OFF);
        device.addSoftButtonEventHandler(new DemoSoftButtonEventHandler());
        waitForKey=true;
        while (waitForKey){
            waitForKey=false;
            Thread.sleep(5000);
        }
        LOGGER.info("Inactivity for more than five seconds. Stopping input demo.");
    }

    private void blink(Button button) {
        directOutput.setLed(page, button, ON);
        sleep(50);
        directOutput.setLed(page, button, OFF);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class DemoSoftButtonEventHandler implements SoftButtonEventHandler {
        @Override
        public void buttonPressed(Button button) {
            LOGGER.info("Button pressed: {}", button);
            if (toggleButtons.contains(button)) {
                directOutput.setLed(page, button, OFF);
                toggleButtons.remove(button);
            } else {
                directOutput.setLed(page, button, ON);
                toggleButtons.add(button);
            }
            directOutput.setLed(page, UP, ON);
            directOutput.setLed(page, DOWN, ON);
            waitForKey = true;
        }

        @Override
        public void buttonReleased(Button button) {
            LOGGER.info("Button released: {}", button);
            directOutput.setLed(page, UP, OFF);
            directOutput.setLed(page, DOWN, OFF);
            waitForKey = true;
        }

        @Override
        public void knobTurnUp(Knob knob) {
            LOGGER.info("Knob turned up: {}", knob);
            blink(DOWN);
            waitForKey = true;
        }

        @Override
        public void knobTurnDown(Knob knob) {
            LOGGER.info("Knob turned down: {}", knob);
            blink(UP);
            waitForKey = true;
        }
    }
}
