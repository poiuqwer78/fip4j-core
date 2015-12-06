package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.FIP;
import ch.poiuqwer.saitek.fip4j.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.poiuqwer.saitek.fip4j.impl.Button.*;

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
public class LedDemo {

    private static Logger LOGGER = LoggerFactory.getLogger(LedDemo.class);

    DirectOutput directOutput;
    Device device;
    Page page;

    public LedDemo(FIP fip) {
        this.directOutput = fip.getDirectOutput();
        this.device = fip.getDevice();
        this.page = fip.getPage();
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running LED demo.");
        for (int i = 0; i < 1; i++) {
            directOutput.activateButton(device, page, UP);
            directOutput.deactivateButton(device,page, DOWN);
            Thread.sleep(200);
            directOutput.activateButton(device, page, DOWN);
            directOutput.deactivateButton(device,page, UP);
            Thread.sleep(200);
        }
        directOutput.deactivateButton(device, page, UP);
        directOutput.deactivateButton(device, page, DOWN);
        for (int j = 0; j < 1; j++) {
            Thread.sleep(200);
            for (int i = 2; i <= 6; i++) {
                directOutput.activateButton(device, page, S(i));
                directOutput.deactivateButton(device, page, S(i-1));
                Thread.sleep(50);
            }
            Thread.sleep(200);
            for (int i = 5; i >= 1; i--) {
                directOutput.activateButton(device, page, S(i));
                directOutput.deactivateButton(device, page, S(i+1));
                Thread.sleep(50);
            }
        }
        Thread.sleep(200);
        directOutput.deactivateButton(device, page, S1);
    }

}
