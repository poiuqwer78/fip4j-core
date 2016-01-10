package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.*;
import ch.poiuqwer.saitek.fip4j.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static ch.poiuqwer.saitek.fip4j.Button.*;
import static ch.poiuqwer.saitek.fip4j.LedState.*;
import static java.awt.Color.*;
import static java.awt.Font.*;

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
public class ScreenDemo implements PageChangeListener {

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenDemo.class);

    private static final Color TEXT_COLOR = new Color(191, 191, 191);
    private static final Color VERY_DARK_GRAY = new Color(16, 16, 16);

    private transient boolean waitForKey;

    private final Set<Button> toggleButtons = new HashSet<>();
    private int[] buttonYCoordinate = new int[]{3, 47, 90, 134, 177, 221};
    private int[] knobXCoordinate = new int[]{80, 217};

    private final Page page;
    private BufferedImage bufferedImage;
    private Graphics g;
    private BufferedImage savedBufferedImage;

    public ScreenDemo(Page page) {
        LOGGER.info("Running screen demo.");
        this.page = page;
        page.getDevice().addPageChangeListener(this);
        bufferedImage = DisplayBuffer.getSuitableBufferedImage();
        g = bufferedImage.getGraphics();
    }

    public void run() {
        drawLayout();
        displayTitleText();
        drawColorBands();
        measureFrameRateDemo();
        interactiveButtonsDemo();
        cleanup();
    }

    private void cleanup() {
        page.clearScreen();
    }

    private void interactiveButtonsDemo() {

        LOGGER.info("Try the buttons on the device.");
        LOGGER.info("Program stops if no action is performed on the device for more than five seconds.");
        SoftButtonListener handler = new DemoSoftButtonListener();
        drawButton(1, false);
        drawButton(2, false);
        drawButton(3, false);
        drawButton(4, false);
        drawButton(5, false);
        drawButton(6, false);
        drawKnob(Knob.LEFT, 0);
        drawKnob(Knob.RIGHT, 0);
        savedBufferedImage = DisplayBuffer.getSuitableBufferedImage();
        savedBufferedImage.setData(bufferedImage.getData());
        drawDialog();
        page.setImage(bufferedImage);
        page.addSoftButtonEventHandler(handler);
        waitForKey = true;
        while (waitForKey) {
            waitForKey = false;
            sleep(5000);
        }
        page.removeSoftButtonEventHandler(handler);
        LOGGER.info("Inactivity for more than five seconds. Stopping input demo.");
    }

    private void drawDialog() {
        g.setColor(DARK_GRAY);
        g.fillRect(100, 80, 175, 80);
        g.setColor(GRAY);
        g.drawRect(100, 80, 175, 80);
        g.setColor(WHITE);
        Font font = new Font(DIALOG, PLAIN, 14);
        g.setFont(font);
        g.drawString("Try the buttons on the left", 104, 95);
        g.drawString("and the knobs below.", 104, 110);
        g.drawString("The demo stops after five", 104, 135);
        g.drawString("seconds inactivity.", 104, 150);
    }

    private void measureFrameRateDemo() {
        boolean on = true;
        Font font = new Font(MONOSPACED, PLAIN, 14);
        g.setFont(font);
        g.setColor(TEXT_COLOR);
        g.drawString("Testing performance ...", 50, 157);

        page.setImage(bufferedImage);

        g.drawRect(68, 173, 228, 9);
        long start = System.nanoTime();
        int frames = 0;
        for (int i = 0; i < 225; i++) {
            g.setColor(GRAY);
            g.drawLine(70 + i, 175, 70 + i, 180);
            if (i % 3 == 0) {
                page.setImage(bufferedImage);
                frames++;
                float duration = (System.nanoTime() - start) / 1000000000;
                float frameRate = frames / duration;
                g.setColor(BLACK);
                g.fillRect(240, 145, 100, 15);
                g.setColor(TEXT_COLOR);
                if (Float.isFinite(frameRate)) {
                    g.drawString(String.format("%5.2f fps", frameRate), 240, 157);
                }
                page.setLed(UP, on ? ON : OFF);
                page.setLed(DOWN, on ? OFF : ON);
                on = !on;
            }
        }
        page.setImage(bufferedImage);
        page.setLed(UP, OFF);
        page.setLed(DOWN, OFF);
    }

    private void drawColorBands() {
        for (int i = 0; i < 256; i++) {
            g.setColor(new Color(i, 0, 0));
            g.drawLine(60 + i, 57, 60 + i, 77);
            g.setColor(new Color(0, i, 0));
            g.drawLine(60 + i, 85, 60 + i, 105);
            g.setColor(new Color(0, 0, i));
            g.drawLine(60 + i, 113, 60 + i, 133);
        }
        page.setImage(bufferedImage);
    }

    private void displayTitleText() {
        g.setColor(TEXT_COLOR);
        Font font = new Font(MONOSPACED, BOLD, 16);
        g.setFont(font);
        g.drawString("Demo Screen by Fip4j-Core", 55, 20);
        font = new Font(MONOSPACED, PLAIN, 12);
        g.setFont(font);
        g.drawString("\u00a9 2016 The PoiuQWeR Network", 55, 39);
    }

    private void drawLayout() {
        g.setColor(VERY_DARK_GRAY);
        g.fillRect(0, 0, 44, 239);
        g.fillRect(46, 0, 273, 49);
        g.fillRect(46, 207, 273, 33);
        g.setColor(DARK_GRAY);
        g.drawLine(0, 33, 45, 33);
        g.drawLine(0, 77, 45, 77);
        g.drawLine(0, 120, 45, 120);
        g.drawLine(0, 164, 45, 164);
        g.drawLine(0, 206, 45, 206);
        g.drawLine(45, 0, 45, 239);
        g.drawLine(45, 50, 319, 50);
        g.drawLine(45, 140, 319, 140);
        g.drawLine(45, 206, 319, 206);
        g.drawLine(182, 206, 182, 239);
    }

    private void drawButton(int i, boolean pressed) {
        Font font = new Font(MONOSPACED, BOLD, 14);
        g.setFont(font);
        g.setColor(pressed ? RED : DARK_GRAY);
        g.drawRoundRect(6, buttonYCoordinate[i - 1], 30, 15, 3, 3);
        g.drawString("S" + i + "\u25b6", 10, buttonYCoordinate[i - 1] + 12);
    }

    private void drawKnob(Knob knob, int value) {
        Font font = new Font(MONOSPACED, BOLD, 14);
        g.setFont(font);
        int i = knob == Knob.LEFT ? 0 : 1;
        g.setColor(DARK_GRAY);
        g.drawRoundRect(knobXCoordinate[i], 217, 60, 15, 3, 3);
        g.drawLine(knobXCoordinate[i] + 30, 217, knobXCoordinate[i] + 30, 232);
        g.drawString("\u21b6", knobXCoordinate[i] + 8, 229);
        g.drawString("\u21b7", knobXCoordinate[i] + 38, 229);
        if (value == knob.ccwValue) {
            g.setColor(RED);
            g.drawRoundRect(knobXCoordinate[i], 217, 30, 15, 3, 3);
            g.drawString("\u21b6", knobXCoordinate[i] + 8, 229);
        }
        if (value == knob.cwValue) {
            g.setColor(RED);
            g.drawRoundRect(knobXCoordinate[i] + 30, 217, 30, 15, 3, 3);
            g.drawString("\u21b7", knobXCoordinate[i] + 38, 229);
        }
    }

    private void blink(Knob knob, Button button) {
        drawKnob(knob, button == UP ? knob.ccwValue : knob.cwValue);
        drawKnob(knob == Knob.LEFT ? Knob.RIGHT : Knob.LEFT, 0);
        page.setImage(bufferedImage);
        page.setLed(button, ON);
        sleep(100);
        drawKnob(Knob.LEFT, 0);
        drawKnob(Knob.RIGHT, 0);
        page.setImage(bufferedImage);
        page.setLed(button, OFF);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pageActivated(Page page) {
        page.setImage(bufferedImage);
    }

    @Override
    public void pageDeactivated(Page page) {

    }

    private class DemoSoftButtonListener implements SoftButtonListener {
        @Override
        public void buttonPressed(Button button) {
            removeDialog();
            LOGGER.info("Button pressed: {}", button);
            if (toggleButtons.contains(button)) {
                drawButton(button.led, false);
                page.setImage(bufferedImage);
                page.setLed(button, OFF);
                toggleButtons.remove(button);
            } else {
                drawButton(button.led, true);
                page.setImage(bufferedImage);
                page.setLed(button, ON);
                toggleButtons.add(button);
            }
            page.setLed(UP, ON);
            page.setLed(DOWN, ON);
            waitForKey = true;
        }

        @SuppressWarnings("SynchronizeOnNonFinalField")
        private void removeDialog() {
            if (savedBufferedImage != null) {
                synchronized (savedBufferedImage) {
                    if (savedBufferedImage != null) {
                        bufferedImage = savedBufferedImage;
                        g = bufferedImage.getGraphics();
                        savedBufferedImage = null;
                    }
                }
            }
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
            removeDialog();
            LOGGER.info("Knob turned up: {}", knob);
            blink(knob, DOWN);
            waitForKey = true;
        }

        @Override
        public void knobTurnedCounterclockwise(Knob knob) {
            removeDialog();
            LOGGER.info("Knob turned down: {}", knob);
            blink(knob, UP);
            waitForKey = true;
        }
    }

}
