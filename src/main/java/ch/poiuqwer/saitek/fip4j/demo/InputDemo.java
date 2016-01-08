package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.*;
import ch.poiuqwer.saitek.fip4j.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static ch.poiuqwer.saitek.fip4j.Button.DOWN;
import static ch.poiuqwer.saitek.fip4j.Button.UP;
import static ch.poiuqwer.saitek.fip4j.LedState.OFF;
import static ch.poiuqwer.saitek.fip4j.LedState.ON;
import static java.awt.Color.DARK_GRAY;
import static java.awt.Font.BOLD;
import static java.awt.Font.MONOSPACED;

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

    private final Page page;

    private transient boolean waitForKey;

    private final Set<Button> toggleButtons = new HashSet<>();

    public InputDemo(Page page) {
        this.page = page;
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running input demo.");
        LOGGER.info("Try the buttons on the device.");
        LOGGER.info("Program stops if no action is performed on the device for more than five seconds.");
        showDescription();

        SoftButtonListener handler = new DemoSoftButtonListener();
        page.setLed(UP, OFF);
        page.setLed(DOWN, OFF);
        page.addSoftButtonEventHandler(handler);
        waitForKey=true;
        while (waitForKey){
            waitForKey=false;
            Thread.sleep(5000);
        }
        page.removeSoftButtonEventHandler(handler);
        LOGGER.info("Inactivity for more than five seconds. Stopping input demo.");
    }

    private void showDescription() {
        BufferedImage bufferedImage = DisplayBuffer.getSuitableBufferedImage();
        Graphics g = bufferedImage.getGraphics();
        Font font = new Font(MONOSPACED, BOLD, 14);
        g.setFont(font);
        g.setColor(DARK_GRAY);
        g.drawString("Press the buttons on the right and turn", 2, 15);
        g.drawString("the knobs on the bottom.", 2, 30);
        g.drawString("The test stops after 5 seconds of", 2, 60);
        g.drawString("inactivity.", 2, 75);
        page.setImage(bufferedImage);
    }

    private void blink(Button button) {
        page.setLed(button, ON);
        sleep(50);
        page.setLed(button, OFF);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class DemoSoftButtonListener implements SoftButtonListener {
        @Override
        public void buttonPressed(Button button) {
            LOGGER.info("Button pressed: {}", button);
            if (toggleButtons.contains(button)) {
                page.setLed(button, OFF);
                toggleButtons.remove(button);
            } else {
                page.setLed(button, ON);
                toggleButtons.add(button);
            }
            page.setLed(UP, ON);
            page.setLed(DOWN, ON);
            waitForKey = true;
        }

        @Override
        public void buttonReleased(Button button) {
            LOGGER.info("Button released: {}", button);
            page.setLed(UP, OFF);
            page.setLed(DOWN, OFF);
            waitForKey = true;
        }

        @Override
        public void knobTurnedClockwise(Knob knob) {
            LOGGER.info("Knob turned up: {}", knob);
            blink(DOWN);
            waitForKey = true;
        }

        @Override
        public void knobTurnedCounterclockwise(Knob knob) {
            LOGGER.info("Knob turned down: {}", knob);
            blink(UP);
            waitForKey = true;
        }
    }
}
