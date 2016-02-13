package ch.poiuqwer.saitek.fip4j;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
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
@SuppressWarnings("unused")
public class LibraryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryManager.class);

    private static DirectOutput directOutput;

    /**
     * Allow to inject (mock) library for testing
     *
     * @param library the mock/test implementation of the JNA Interface
     */
    public static void setLibrary(Library library) {
        if (directOutput != null) {
            directOutput.cleanup();
        }
        directOutput = new DirectOutput(library);
    }

    /**
     * Get direct output library.
     *
     * If the directOutput library has been initialized using setLibraryForTesting(), this call returns the previously set library.
     *
     * @return the library to interact with Saitek FIPs
     */
    public static DirectOutput getDirectOutput() {
        return directOutput;
    }

    public static boolean loadLibrary() {
        LOGGER.debug("Loading DirectOutput library.");
        System.setProperty("jna.library.path", WindowsRegistryAccessor.getLibraryPath());
        try {
            NativeLibrary.getInstance(Library.JNA_LIBRARY_NAME);
            Library library = (Library) Native.loadLibrary(Library.JNA_LIBRARY_NAME, Library.class);
            directOutput = new DirectOutput(library);
            return true;
        } catch (Throwable t) {
            LOGGER.error("Unable to load DirectOutput.dll (running on {} - {} - {}).", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), t);
        }
        return false;
    }
}
