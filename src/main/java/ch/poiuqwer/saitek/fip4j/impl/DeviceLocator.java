package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
public class DeviceLocator {

    private static Logger LOGGER = LoggerFactory.getLogger(DeviceLocator.class);

    private static final String PLUGIN_NAME = "saitek-fip4j";

    public static Device findFlightInformationPanel(DirectOutput directOutput) {
        LOGGER.info("Searching a Saitek Flight Instrument Panel...");
        List<Pointer> devicePointers = new ArrayList<>();
        WString pluginName = new WString(PLUGIN_NAME);
        directOutput.DirectOutput_Initialize(pluginName);
        directOutput.DirectOutput_Enumerate((hDevice, pCtc) -> devicePointers.add(hDevice), null);
        if (devicePointers.size()>0) {
            LOGGER.info("Found {} device(s). Will check if they are Saitek Flight Instrument Panels.",devicePointers.size());
            for (Pointer devicePointer : devicePointers) {
                Pointer guidPointer = new Memory(16);
                directOutput.DirectOutput_GetDeviceType(devicePointer, guidPointer);
                Guid deviceGuid = Guid.fromByteArray(guidPointer.getByteArray(0, 16));
                if (Guid.FIP.equals(deviceGuid)) {
                    LOGGER.info("Saitek Flight Information Panel found: {}", Guid.FIP);
                    return new Device(devicePointer);
                }
                if (Guid.X52PRO.equals(deviceGuid)) {
                    LOGGER.info("Saitek X52 Pro found: {}", Guid.FIP);
                }
                else {
                    LOGGER.info("Unknown device found: {}", deviceGuid);
                }
            }
        }
        LOGGER.error("No Saitek Flight Information Panel could be found.");
        return new Device();
    }
}