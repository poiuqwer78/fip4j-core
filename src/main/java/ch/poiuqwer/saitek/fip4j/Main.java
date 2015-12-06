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

    public static final String PLUGIN_NAME = "Saitek-FIP4j";

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static DirectOutput directOutput = null;

    public static void main(String[] args) {

        LOGGER.info("Starting {}.", PLUGIN_NAME);
        try {
            directOutput = LibraryManager.load();
            directOutput.initialize(PLUGIN_NAME);
            try {
                Set<Device> devices = DeviceLocator.findProFlightInstrumentPanel(directOutput);
                for (Device device : devices) {
                    FIP fip = new FIP(directOutput, device);
                    directOutput.addPage(device,0,PageState.ACTIVE);
                    try {
                        runDemos(fip);
                    } finally {
                        directOutput.removePage(device,0);
                    }
                }
            } catch (Throwable t){
                LOGGER.error("Unexpected error.", t);
            } finally {
                directOutput.deinitialize();
            }
        } catch (IllegalStateException ignore){
            // already logged
        }
    }

    private static void runDemos(FIP fip) throws InterruptedException, IOException {
        LOGGER.info("Running demos.");
        new LedDemo(fip).run();
        new ScreenDemo(fip).run();
        new InputDemo(fip).run();
    }

}
