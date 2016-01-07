package ch.poiuqwer.saitek.fip4j;

import ch.poiuqwer.saitek.fip4j.demo.InputDemo;
import ch.poiuqwer.saitek.fip4j.demo.LedDemo;
import ch.poiuqwer.saitek.fip4j.demo.ScreenDemo;
import ch.poiuqwer.saitek.fip4j.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

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
        try {
            LOGGER.info("Starting {}.", PLUGIN_NAME);
            if (LibraryManager.loadDirectOutput()) {
                directOutput = LibraryManager.getDirectOutput();
                directOutput.setup(PLUGIN_NAME);
                Collection<Device> devices = directOutput.getDevices();
                for (Device device : devices) {
                    device.addPageChangeEventHandler(new PageChangeEventHandler() {
                        @Override
                        public void pageActivated(Page page) {
                            LOGGER.info("Page activated: {}", page);
                        }

                        @Override
                        public void pageDeactivated(Page page) {
                            LOGGER.info("Page deactivated: {}", page);
                        }
                    });
                    runDemos(device.addPage());
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Awww, unexpected error.", t);
        } finally {
            directOutput.cleanup();
        }
    }

    private static void runDemos(Page page) throws InterruptedException, IOException {
        LOGGER.info("Running demos.");
        new LedDemo(page).run();
        new ScreenDemo(page).run();
        new InputDemo(page).run();
    }

}
