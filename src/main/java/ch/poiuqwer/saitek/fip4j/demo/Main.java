package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static AtomicInteger numberOfDemosRunning = new AtomicInteger(0);

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting {}.", PLUGIN_NAME);
            if (LibraryManager.loadLibrary()) {
                directOutput = LibraryManager.getDirectOutput();
                directOutput.setup(PLUGIN_NAME);
                Collection<Device> devices = directOutput.getDevices();
                directOutput.addDeviceChangeListener(new DeviceChangeListener() {
                    @Override
                    public void deviceConnected(Device device) {
                        try {
                            setupDeviceForDemos(device);
                        } catch (InterruptedException | IOException e) {
                            logUnexpectedError(e);
                        }
                    }

                    @Override
                    public void deviceDisconnected(Device device) {

                    }
                });
                for (Device device : devices) {
                    setupDeviceForDemos(device);
                }
                if (devices.isEmpty()) {
                    int seconds = 0;
                    LOGGER.info("Waiting 30 seconds for devices to be plugged in ...");
                    while (directOutput.getDevices().isEmpty() && seconds++ < 30) {
                        Thread.sleep(1000);
                    }
                }
                while (numberOfDemosRunning.get() > 0) {
                    Thread.sleep(1000);
                }
            }
        } catch (Throwable t) {
            logUnexpectedError(t);
        } finally {
            directOutput.cleanup();
        }
    }

    private static void logUnexpectedError(Throwable t) {
        LOGGER.error("Awww, unexpected error.", t);
    }

    private static void setupDeviceForDemos(Device device) throws InterruptedException, IOException {
        numberOfDemosRunning.incrementAndGet();
        Thread.sleep(1000);
        LOGGER.info("Running demos.");
        device.addPageChangeListener(new LoggingPageChangeListener());
        Page page = device.addPage();
        runDemos(page);
        numberOfDemosRunning.decrementAndGet();
    }

    private static void runDemos(Page page) throws InterruptedException, IOException {
        new LedDemo(page).run();
        new ScreenDemo(page).run();
        new InputDemo(page).run();
    }

    private static class LoggingPageChangeListener implements PageChangeListener {
        @Override
        public void pageActivated(Page page) {
            LOGGER.info("Page activated: {}", page);
        }

        @Override
        public void pageDeactivated(Page page) {
            LOGGER.info("Page deactivated: {}", page);
        }
    }
}