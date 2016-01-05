package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.impl.*;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    DirectOutput directOutput;
    Device device;

    Library.Pfn_DirectOutput_SoftButtonChange softButtonChange =
            (Pointer hDevice, int dwButtons, Pointer pCtxt) -> printSoftButton(dwButtons);

    Library.Pfn_DirectOutput_PageChange pageChange =
            (Pointer hDevice, int dwPage, byte bSetActive, Pointer pCtxt) -> printPageChange(dwPage,bSetActive);
    private transient boolean waitForKey;


    public InputDemo(Page page) {
        this.directOutput = LibraryManager.getDirectOutput();
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running input demo.");
        LOGGER.info("Try the buttons on the device.");
        LOGGER.info("Program stops if no action is performed on the device for more than five seconds.");
//        directOutput.dll.DirectOutput_RegisterSoftButtonCallback(device.getPointer(), softButtonChange, Pointer.NULL);
//        directOutput.dll.DirectOutput_RegisterPageCallback(device.getPointer(),pageChange,Pointer.NULL);
        waitForKey=true;
        while (waitForKey){
            waitForKey=false;
            Thread.sleep(5000);
        }
        LOGGER.info("Inactivity for more than five seconds. Stopping input demo.");
    }

    private void printSoftButton(int dwButtons) {
        LOGGER.debug("Soft Button State: {}",dwButtons);
        waitForKey=true;
    }

    private void printPageChange(int dwPage, byte bSetActive) {
        LOGGER.debug("Page Change - Page: {} Active: {}",dwPage, bSetActive);
        waitForKey=true;
    }

}
