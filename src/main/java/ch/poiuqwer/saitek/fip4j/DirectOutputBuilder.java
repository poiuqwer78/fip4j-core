package ch.poiuqwer.saitek.fip4j;

import com.google.common.eventbus.EventBus;
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
public class DirectOutputBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectOutputBuilder.class);

    private Library dll;
    private EventBus eventBus;

    public static DirectOutputBuilder createForTesting() {
        return new DirectOutputBuilder();
    }

    /**
     * The default. Load the real DLL.
     */
    public static DirectOutputBuilder createAndLoadDLL() {
        LOGGER.debug("Loading DirectOutput library.");
        System.setProperty("jna.library.path", WindowsRegistryAccessor.getLibraryPath());
        try {
            DirectOutputBuilder builder = new DirectOutputBuilder();
            NativeLibrary.getInstance(Library.JNA_LIBRARY_NAME);
            builder.dll = (Library) Native.loadLibrary(Library.JNA_LIBRARY_NAME, Library.class);
            return builder;
        } catch (Throwable t) {
            LOGGER.error("Unable to load DirectOutput.dll (running on {} - {} - {}).", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), t);
            throw t;
        }
    }

    /**
     * Allow to inject (mock) library for testing.
     *
     * @param library the mock/test implementation of the JNA Interface
     */
    public DirectOutputBuilder library(Library library) {
        this.dll = library;
        return this;
    }

    /**
     * Allows to set an user-specified instance of EventBus (e.g. AsyncEventBus)
     */
    public DirectOutputBuilder eventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    public DirectOutput build() {
        if (this.eventBus == null) {
            this.eventBus = new EventBus();
        }
        if (this.dll == null) {
            throw new IllegalStateException("A (mock) library must be specified to build DirectOutput");
        }
        return new DirectOutput(dll, eventBus);
    }

}
