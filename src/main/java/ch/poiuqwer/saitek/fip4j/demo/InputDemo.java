package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.ProFlightInstrumentPanel;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutputLibrary;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class InputDemo {

    private static Logger LOGGER = LoggerFactory.getLogger(InputDemo.class);

    DirectOutputLibrary directOutput;
    Pointer device;

    DirectOutputLibrary.Pfn_DirectOutput_SoftButtonChange softButtonChange =
            (Pointer hDevice, int dwButtons, Pointer pCtxt) -> printSoftButton(dwButtons);

    DirectOutputLibrary.Pfn_DirectOutput_PageChange pageChange =
            (Pointer hDevice, int dwPage, byte bSetActive, Pointer pCtxt) -> printPageChange(dwPage,bSetActive);


    public InputDemo(ProFlightInstrumentPanel flightInstrumentPanel) {
        this.directOutput = flightInstrumentPanel.getDirectOutput();
        this.device = flightInstrumentPanel.getDevice();
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running input demo.");
        LOGGER.info("Try the buttons on the device or press ENTER to continue.");
        directOutput.DirectOutput_RegisterSoftButtonCallback(device, softButtonChange, Pointer.NULL);
        directOutput.DirectOutput_RegisterPageCallback(device,pageChange,Pointer.NULL);
        printSoftButton(0);
        waitForEnter();
    }

    private void printSoftButton(int dwButtons) {
        LOGGER.debug("Soft Button State: {}",dwButtons);
    }

    private void printPageChange(int dwPage, byte bSetActive) {
        LOGGER.debug("Page Change - Page: {}",dwPage);
        LOGGER.debug("Page Change - SetActive: {}",bSetActive);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void waitForEnter() {
        try {
            System.in.read();
        } catch (IOException ignore) {

        }
    }

}
