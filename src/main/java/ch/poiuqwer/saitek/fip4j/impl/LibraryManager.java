package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.WString;
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
public class LibraryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryManager.class);
    public static final String PLUGIN_NAME = "Saitek-FIP4j";

    public static Optional<DirectOutput> load(){
        LOGGER.info("Loading DirectOutput library.");
        System.setProperty("jna.library.path", WindowsRegistry.getLibraryPath());
        try {
            DirectOutput directOutput = loadLibrary();
            initializeLibrary(directOutput);
            return Optional.of(directOutput);
        } catch (Throwable t) {
            LOGGER.error("Unable to load DirectOutput.dll (running on {} - {} - {}).", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), t);
        }
        return Optional.empty();
    }

    public static void unload(DirectOutput directOutput){
        LOGGER.info("Cleaning up DirectOutput library.");
        directOutput.DirectOutput_Deinitialize();
    }

    private static void initializeLibrary(DirectOutput directOutput) {
        LOGGER.info("Initializing DirectOutput library.");
        WString pluginName = new WString(PLUGIN_NAME);
        directOutput.DirectOutput_Initialize(pluginName);
    }

    private static DirectOutput loadLibrary() {
        NativeLibrary.getInstance(DirectOutput.JNA_LIBRARY_NAME);
        DirectOutput directOutput = (DirectOutput) Native.loadLibrary(DirectOutput.JNA_LIBRARY_NAME, DirectOutput.class);
        LOGGER.info("DirectOutput library loaded successfully.");
        return directOutput;
    }
}
