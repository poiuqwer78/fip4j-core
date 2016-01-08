package ch.poiuqwer.saitek.fip4j;

import com.google.common.base.Preconditions;
import com.sun.jna.Memory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

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
public class DisplayBuffer {

    public static final int DISPLAY_WIDTH = 320;
    public static final int DISPLAY_HEIGHT = 240;
    public static final int DISPLAY_COLOR_DEPTH = 3;
    public static final int LINE_SIZE = DISPLAY_WIDTH * DISPLAY_COLOR_DEPTH;
    public static final int SIZE = DISPLAY_HEIGHT * LINE_SIZE;

    private final Memory displayBuffer = new Memory(SIZE);

    DisplayBuffer() {
        clear();
    }

    void loadImage(BufferedImage image) {
        checkImage(image);
        byte[] bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        int pointerOffset = SIZE - LINE_SIZE;
        for (int i = 0; i < DISPLAY_HEIGHT; i++) {
            displayBuffer.write(pointerOffset - (i * LINE_SIZE), bytes, i * LINE_SIZE, LINE_SIZE);
        }
    }

    private void checkImage(BufferedImage image) {
        Preconditions.checkArgument(image.getType() == BufferedImage.TYPE_3BYTE_BGR);
        Preconditions.checkArgument(image.getWidth() == DISPLAY_WIDTH);
        Preconditions.checkArgument(image.getHeight() == DISPLAY_HEIGHT);
    }

    int getSize() {
        return SIZE;
    }

    void clear() {
        displayBuffer.clear();
    }

    Memory getMemory() {
        return displayBuffer;
    }

    public static BufferedImage getSuitableBufferedImage() {
        return new BufferedImage(320, 240, BufferedImage.TYPE_3BYTE_BGR);
    }

}
