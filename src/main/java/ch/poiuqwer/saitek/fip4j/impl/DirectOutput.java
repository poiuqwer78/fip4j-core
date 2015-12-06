package ch.poiuqwer.saitek.fip4j.impl;

import com.google.common.base.Preconditions;
import com.sun.jna.Memory;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DirectOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectOutput.class);

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int COLOR_DEPTH = 3;

    public final DirectOutputLibrary dll;

    public DirectOutput(DirectOutputLibrary dll) {
        this.dll = dll;
    }

    public HRESULT call(int code) {
        HRESULT result = HRESULT.of(code);
        switch (result){
            case S_OK:
                if (LOGGER.isDebugEnabled()) {
                    // Expensive operation, only perform if logging really happens on this level.
                    String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                    LOGGER.debug("Call '{}' {}", methodName, result);
                }
                break;
            default:
                String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                LOGGER.error("Call '{}' {}",methodName,result);
        }
        return result;
    }

    //// Wrappers for low-level calls ////

    public HRESULT initialize(String pluginName){
        return call(dll.DirectOutput_Initialize(new WString(pluginName)));
    }

    public HRESULT deinitialize() {
        return call(dll.DirectOutput_Deinitialize());
    }

    public HRESULT addPage(Device device, int index, PageState state) {
        return call(dll.DirectOutput_AddPage(device.getPointer(),index,new WString(Integer.toString(index)),state.getValue()));
    }

    public HRESULT removePage(Device device, int index) {
        return call(dll.DirectOutput_RemovePage(device.getPointer(),index));
    }

    public HRESULT setLed(Device device, Page page, Button button){
        return call(dll.DirectOutput_SetLed(device.getPointer(),page.getIndex(),button.getLed(),1));
    }

    public HRESULT clearLed(Device device, Page page, Button button){
        return call(dll.DirectOutput_SetLed(device.getPointer(),page.getIndex(),button.getLed(),0));
    }

    public HRESULT setImage(Device device, Page page, BufferedImage bufferedImage){
        Preconditions.checkArgument(bufferedImage.getType()==BufferedImage.TYPE_3BYTE_BGR);
        Preconditions.checkArgument(bufferedImage.getWidth()==WIDTH);
        Preconditions.checkArgument(bufferedImage.getHeight()==HEIGHT);
        byte[] bytes = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        int size = bytes.length;
        Memory memory = new Memory(size);
        writeBytesToMemory(bytes, size, memory);
        return call(dll.DirectOutput_SetImage(device.getPointer(),page.getIndex(),0,size,memory));
    }

    public HRESULT clearImage(Device device, Page page){
        int size = WIDTH*HEIGHT*COLOR_DEPTH;
        Memory memory = new Memory(size);
        memory.clear();
        return call(dll.DirectOutput_SetImage(device.getPointer(),page.getIndex(),0,size,memory));
    }

    private void writeBytesToMemory(byte[] bytes, int size, Memory imagePointer) {
        int lineLength = WIDTH * COLOR_DEPTH;
        int pointerOffset = size - lineLength;
        for (int i = 0; i < HEIGHT; i++) {
            imagePointer.write(pointerOffset - (i * lineLength), bytes, i * lineLength, lineLength);
        }
    }

}
