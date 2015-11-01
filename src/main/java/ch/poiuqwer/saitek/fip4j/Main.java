package ch.poiuqwer.saitek.fip4j;

import ch.poiuqwer.saitek.fip4j.demo.NightRiderDemo;
import ch.poiuqwer.saitek.fip4j.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
        Optional<DirectOutput> directOutput = Optional.empty();
        try {
            directOutput = Library.load();
            if (directOutput.isPresent()) {
                Device device = DeviceLocator.findFlightInformationPanel(directOutput.get());
                if (device.isPresent()){
                    FlightInstrumentPanel fip = new FlightInstrumentPanel(directOutput.get(),device);
                    runDemos(fip);
                }
            }
        } catch (Throwable t){
            LOGGER.error("An unexpected error occurred:", t);
        } finally {
            if (directOutput.isPresent()){
                directOutput.get().DirectOutput_Deinitialize();
            }
            LOGGER.info("Bye bye.");
        }
    }

    private static void runDemos(FlightInstrumentPanel fip) throws InterruptedException {
        LOGGER.info("So, let's run some demo applications.");
        new NightRiderDemo(fip).run();
    }

}
