package ch.poiuqwer.saitek.fip4j.demo;

import ch.poiuqwer.saitek.fip4j.DirectOutput;
import ch.poiuqwer.saitek.fip4j.LibraryManager;
import ch.poiuqwer.saitek.fip4j.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.poiuqwer.saitek.fip4j.Button.*;
import static ch.poiuqwer.saitek.fip4j.LedState.OFF;
import static ch.poiuqwer.saitek.fip4j.LedState.ON;

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

    Page page;

    public LedDemo(Page page) {
        this.page = page;
    }

    public void run() throws InterruptedException {
        LOGGER.info("Running LED demo.");
        for (int i = 0; i < 1; i++) {
            page.setLed(UP, ON);
            page.setLed(DOWN, OFF);
            Thread.sleep(200);
            page.setLed(DOWN, ON);
            page.setLed(UP, OFF);
            Thread.sleep(200);
        }
        page.setLed(UP, OFF);
        page.setLed(DOWN, OFF);
        for (int j = 0; j < 1; j++) {
            Thread.sleep(200);
            for (int i = 2; i <= 6; i++) {
                page.setLed(S(i), ON);
                page.setLed(S(i - 1), OFF);
                Thread.sleep(50);
            }
            Thread.sleep(200);
            for (int i = 5; i >= 1; i--) {
                page.setLed(S(i), ON);
                page.setLed(S(i + 1), OFF);
                Thread.sleep(50);
            }
        }
        Thread.sleep(200);
        page.setLed(S1, OFF);
    }

}
