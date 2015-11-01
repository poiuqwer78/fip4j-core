package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.FlightInstrumentPanel;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutput;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
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
public class NightRiderDemo {

    private static Logger LOGGER = LoggerFactory.getLogger(NightRiderDemo.class);

    DirectOutput directOutput;
    Pointer device;

    public NightRiderDemo(FlightInstrumentPanel flightInstrumentPanel) {
        this.directOutput = flightInstrumentPanel.getDirectOutput();
        this.device = flightInstrumentPanel.getDevice();
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running NightRider demo.");
        try {
            directOutput.DirectOutput_AddPage(device, 0, new WString("Test"), DirectOutput.FLAG_SET_AS_ACTIVE);
            directOutput.DirectOutput_SetLed(device, 0, 7, 0);
            directOutput.DirectOutput_SetLed(device, 0, 8, 0);
            for (int j = 0; j < 3; j++) {
                Thread.sleep(200);
                for (int i = 2; i <= 6; i++) {
                    directOutput.DirectOutput_SetLed(device, 0, i, 1);
                    directOutput.DirectOutput_SetLed(device, 0, i - 1, 0);
                    Thread.sleep(50);
                }
                Thread.sleep(200);
                for (int i = 5; i >= 1; i--) {
                    directOutput.DirectOutput_SetLed(device, 0, i, 1);
                    directOutput.DirectOutput_SetLed(device, 0, i + 1, 0);
                    Thread.sleep(50);
                }
            }
        } finally {
            directOutput.DirectOutput_RemovePage(device, 0);
        }
    }

}
