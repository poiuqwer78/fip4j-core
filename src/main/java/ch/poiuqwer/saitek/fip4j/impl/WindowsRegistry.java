package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.platform.win32.Advapi32Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

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
public class WindowsRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsRegistry.class);
    private static final String REG_KEY = "SOFTWARE\\Saitek\\DirectOutput";
    private static final String REG_VALUE = "DirectOutput";
    private static final String DEFAULT_LOCATION = "C:\\Program Files\\Saitek\\DirectOutput";

    public static String getLibraryPath(){
        LOGGER.debug("Reading library location from windows registry.");
        try{
            String regValue = Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, REG_KEY, REG_VALUE);
            File path = new File(regValue);
            if (path.exists()){
                LOGGER.info("Library location is {}.", path);
                return path.getParent();
            } else {
                LOGGER.error("Location {} is specified in registry, but it does not exist. Will use default location {}.", regValue, DEFAULT_LOCATION);
            }
        } catch (Throwable t){
            LOGGER.error("Unexpected error while reading library location. Will use default location {}.", DEFAULT_LOCATION , t);
        }
        return DEFAULT_LOCATION;
    }
}
