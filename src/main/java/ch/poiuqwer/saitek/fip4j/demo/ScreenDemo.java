package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.DisplayBuffer;
import ch.poiuqwer.saitek.fip4j.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
public class ScreenDemo {

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenDemo.class);

    private static final Color TEXT_COLOR = new Color(191, 191, 191);

    Page page;

    public ScreenDemo(Page page) {
        this.page = page;
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running screen demo.");

        BufferedImage bufferedImage = DisplayBuffer.getSuitableBufferedImage();
        Graphics g = bufferedImage.getGraphics();
        Font font = new Font(MONOSPACED, PLAIN, 14);
        g.setFont(font);

        g.setColor(DARK_GRAY);
        drawLayout(g);

        g.setColor(GREEN);
        drawButtons(g);

        drawColors(g);

        g.setColor(TEXT_COLOR);
        drawText(g);

        measurePerformance(bufferedImage, g);

        Thread.sleep(1000);

        page.clearScreen();
    }

    private void measurePerformance(BufferedImage bufferedImage, Graphics g) {
        g.drawString("Testing performance ...", 50, 180);

        page.setImage(bufferedImage);

        g.drawRect(48, 188, 269, 19);
        long start = System.nanoTime();
        int frames = 0;
        for (int i = 0; i < 265; i++) {
            g.drawLine(50 + i, 190, 50 + i, 205);
            if (i % 2 == 0) {
                page.setImage(bufferedImage);
                frames++;
                float duration = (System.nanoTime() - start) / 1000000000;
                float framerate = frames / duration;
                g.setColor(BLACK);
                g.fillRect(50,210,269,20);
                g.setColor(TEXT_COLOR);
                if (Float.isFinite(framerate)) {
                    g.drawString(String.format("Framerate: %5.2f", framerate), 50, 225);
                }
            }
        }
        page.setImage(bufferedImage);
    }

    private void switchOnTheLights() {
        page.setLed(S1, ON);
        page.setLed(S2, ON);
        page.setLed(S3, ON);
        page.setLed(S4, ON);
        page.setLed(S5, ON);
        page.setLed(S6, ON);
    }

    private void drawColors(Graphics g) {
        for (int i = 0; i < 256; i++) {
            g.setColor(new Color(i, 0, 0));
            g.drawLine(60 + i, 75, 60 + i, 95);
        }
        for (int i = 0; i < 256; i++) {
            g.setColor(new Color(0, i, 0));
            g.drawLine(60 + i, 105, 60 + i, 125);
        }
        for (int i = 0; i < 256; i++) {
            g.setColor(new Color(0, 0, i));
            g.drawLine(60 + i, 135, 60 + i, 155);
        }
    }

    private void drawText(Graphics g) {
        g.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 50, 17);
        g.drawString("abcdefghijklmnopqrstuvwxyz", 50, 32);
        g.drawString("1234567890", 50, 47);
        g.drawString("!@#$%^&*()_+{}:<>?,./;'[]-=`", 50, 62);
    }

    private void drawLayout(Graphics g) {
        g.drawLine(0, 33, 45, 33);
        g.drawLine(0, 77, 45, 77);
        g.drawLine(0, 120, 45, 120);
        g.drawLine(0, 164, 45, 164);
        g.drawLine(0, 207, 45, 207);
        g.drawLine(45, 0, 45, 239);
        g.drawLine(45, 70, 319, 70);
        g.drawLine(45, 160, 319, 160);
    }

    private void drawButtons(Graphics g) {
        g.drawRoundRect(0, 3, 30, 15, 2, 2);
        g.drawString("S1\u25b6", 4, 15);
        g.drawRoundRect(0, 47, 30, 15, 2, 2);
        g.drawString("S2\u25b6", 4, 59);
        g.drawRoundRect(0, 90, 30, 15, 2, 2);
        g.drawString("S3\u25b6", 4, 102);
        g.drawRoundRect(0, 134, 30, 15, 2, 2);
        g.drawString("S4\u25b6", 4, 146);
        g.drawRoundRect(0, 177, 30, 15, 2, 2);
        g.drawString("S5\u25b6", 4, 189);
        g.drawRoundRect(0, 221, 30, 15, 2, 2);
        g.drawString("S6\u25b6", 4, 233);
    }

}
