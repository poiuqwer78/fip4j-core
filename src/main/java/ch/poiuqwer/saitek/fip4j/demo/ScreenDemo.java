package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.ProFlightInstrumentPanel;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutput;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutputLibrary;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int COLOR_DEPTH = 3;

    DirectOutput wrapper;
    DirectOutputLibrary lib;
    Pointer device;

    public ScreenDemo(ProFlightInstrumentPanel flightInstrumentPanel) {
        this.lib = flightInstrumentPanel.getDirectOutput();
        this.wrapper = new DirectOutput(lib);
        this.device = flightInstrumentPanel.getDevice();
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
            for (int j = 0; j < img.length; j++) {
                long start = System.nanoTime();
                g.drawImage(img[j], 0, 0, null);
                LOGGER.debug("Time to draw image in buffer: {}ms", millisecondsSince(start));
                displayImage(bufferedImage);
                Thread.sleep(500);
            }
        }
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

    private void displayImage(BufferedImage bufferedImage) {
        assert bufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR;
        byte[] bytes = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        int size = bytes.length;
        Memory imagePointer = new Memory(size);
        writeBytesToMemory(bytes, size, imagePointer);
        long start = System.nanoTime();
        wrapper.call(lib.DirectOutput_SetImage(device, 0, 0, size, imagePointer));
        LOGGER.debug("Time to send image to device: {}ms", millisecondsSince(start));
    }

    private long millisecondsSince(long start) {
        return (System.nanoTime() - start) / 1000000;
    }

    private void writeBytesToMemory(byte[] bytes, int size, Memory imagePointer) {
        int lineLength = WIDTH * COLOR_DEPTH;
        int pointerOffset = size - lineLength;
        for (int i = 0; i < HEIGHT; i++) {
            imagePointer.write(pointerOffset - (i * lineLength), bytes, i * lineLength, lineLength);
        }
    }

}
