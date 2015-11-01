package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class DeviceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceLocator.class);

    public static final GUID FIP = GUID.fromString("{3E083CD8-6A37-4A58-80A8-3D6A2C07513E}");
    public static final GUID X52 = GUID.fromString("{29DAD506-F93B-4F20-85FA-1E02C04FAC17}");

    public static Set<Device> findProFlightInstrumentPanel(DirectOutput directOutput) {
        LOGGER.info("Searching a Saitek Pro Flight Instrument Panel.");
        Set<Device> fipDevices= new HashSet<>();
        List<Pointer> devicePointers = new ArrayList<>();
        directOutput.DirectOutput_Enumerate((hDevice, pCtc) -> devicePointers.add(hDevice), null);

        if (devicePointers.size()>0) {
            LOGGER.info("Found {} device(s). Will check if they are Saitek Pro Flight Instrument Panels.",devicePointers.size());
            for (Pointer devicePointer : devicePointers) {
                GUID deviceTypeGUID = getTypeGuid(directOutput, devicePointer);
                if (isProFlightInstrumentPanel(deviceTypeGUID)){
                    GUID deviceGUID = getDeviceGuid(directOutput, devicePointer);
                    String serialNumber = getDeviceSerialNumber(directOutput, devicePointer);
                    Device device = new Device(devicePointer, deviceGUID, serialNumber);
                    LOGGER.info(device.toString());
                    fipDevices.add(device);
                }
            }
        }
        if (fipDevices.isEmpty()) {
            LOGGER.error("No Saitek Pro Flight Instrument Panel could be found.");
        }
        return fipDevices;
    }

    private static String getDeviceSerialNumber(DirectOutput directOutput, Pointer devicePointer) {
        Pointer serialNumberPointer = new Memory(32);
        directOutput.DirectOutput_GetSerialNumber(devicePointer,serialNumberPointer,16);
        char[] serialNumberCharArray = new char[16];
        serialNumberPointer.read(0,serialNumberCharArray,0,16);
        return String.valueOf(serialNumberCharArray).split("\0")[0];
    }

    private static GUID getTypeGuid(DirectOutput directOutput, Pointer devicePointer) {
        Pointer guidPointer = new Memory(16);
        directOutput.DirectOutput_GetDeviceType(devicePointer, guidPointer);
        return GUID.fromBinary(guidPointer.getByteArray(0, 16));
    }

    private static GUID getDeviceGuid(DirectOutput directOutput, Pointer devicePointer) {
        Pointer guidPointer = new Memory(16);
        directOutput.DirectOutput_GetDeviceInstance(devicePointer, guidPointer);
        return GUID.fromBinary(guidPointer.getByteArray(0, 16));
    }

    private static boolean isProFlightInstrumentPanel(GUID deviceTypeGUID) {
        if (FIP.equals(deviceTypeGUID)) {
            LOGGER.info("Saitek Pro Flight Instrument Panel found.");
            return true;
        } else if (X52.equals(deviceTypeGUID)) {
            LOGGER.info("Saitek X52 Pro found.");
        } else {
            LOGGER.info("Unknown device found with GUID {}.", deviceTypeGUID);
        }
        return false;
    }
}