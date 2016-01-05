package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    private static final String IMG_PATH = "C:/Program Files/Saitek/DirectOutput/Werbung";

    private static Logger LOGGER = LoggerFactory.getLogger(ScreenDemo.class);

    DirectOutput directOutput;
    Page page;

    public ScreenDemo(Page page) {
        this.directOutput = LibraryManager.getDirectOutput();
        this.page = page;
    }

    public void run() throws InterruptedException, IOException {
        LOGGER.info("Running screen demo.");
        if (!new File(IMG_PATH).exists()){
            LOGGER.error("This demo requires to set the path to the five advertisements (Fip1.jpg .. Fip2.jpg) in ScreenDemo:IMG_PATH");
            return;
        }
        BufferedImage[] img = loadImages();
        BufferedImage bufferedImage = getBufferedImage();
        Graphics g = bufferedImage.getGraphics();
        for (int i = 0; i < 1; i++) {
            for (BufferedImage anImg : img) {
                g.drawImage(anImg, 0, 0, null);
                directOutput.setImage(page, bufferedImage);
                Thread.sleep(500);
            }
        }
        directOutput.clearScreen(page);
    }

    private BufferedImage getBufferedImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_3BYTE_BGR);
    }

    private BufferedImage[] loadImages() throws IOException {
        BufferedImage[] img = new BufferedImage[5];
        for (int i = 0; i < img.length; i++) {
            img[i] = ImageIO.read(new File(IMG_PATH +"/Fip" + (i + 1) + ".jpg"));
        }
        return img;
    }

}
