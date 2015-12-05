package ch.poiuqwer.saitek.fip4j;

import ch.poiuqwer.saitek.fip4j.demo.InputDemo;
import ch.poiuqwer.saitek.fip4j.demo.LedDemo;
import ch.poiuqwer.saitek.fip4j.demo.ScreenDemo;
import ch.poiuqwer.saitek.fip4j.impl.*;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

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
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Saitek-FIP4j.");
        Optional<DirectOutput> directOutput = Optional.empty();
        try {
            directOutput = LibraryManager.load();
            if (directOutput.isPresent()) {
                Set<Device> devices = DeviceLocator.findProFlightInstrumentPanel(directOutput.get());
                for (Device device : devices){
                    ProFlightInstrumentPanel fip = new ProFlightInstrumentPanel(directOutput.get(),device);
                    openPage(device, fip);
                    try {
                        runDemos(fip);
                    } finally {
                        closePage(device, fip);
                    }
                }
            }
        } catch (Throwable t){
            LOGGER.error("An unexpected error occurred.", t);
        } finally {
            if (directOutput.isPresent()){
                directOutput.get().deinitialize();
            }
            LOGGER.info("Quitting Saitek-FIP4j.");
        }
    }

    private static void closePage(Device device, ProFlightInstrumentPanel fip) {
        fip.getDirectOutput().DirectOutput_RemovePage(device.getPointer(), 0);
    }

    private static void openPage(Device device, ProFlightInstrumentPanel fip) {
        fip.getDirectOutput().DirectOutput_AddPage(device.getPointer(), 0, new WString("Test"), DirectOutputLibrary.FLAG_SET_AS_ACTIVE);
        fip.getDirectOutput().DirectOutput_SetLed(device.getPointer(), 0, 7, 0);
        fip.getDirectOutput().DirectOutput_SetLed(device.getPointer(), 0, 8, 0);
    }

    private static void runDemos(ProFlightInstrumentPanel fip) throws InterruptedException, IOException {
        LOGGER.info("Running demos.");
        new LedDemo(fip).run();
        new ScreenDemo(fip).run();
        new InputDemo(fip).run();
    }

}
