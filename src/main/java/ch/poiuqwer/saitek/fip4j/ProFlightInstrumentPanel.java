package ch.poiuqwer.saitek.fip4j;

import ch.poiuqwer.saitek.fip4j.impl.Device;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutput;
import ch.poiuqwer.saitek.fip4j.impl.DirectOutputLibrary;
import com.sun.jna.Pointer;

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
public class ProFlightInstrumentPanel {

    private DirectOutputLibrary directOutput;
    private Pointer device;

    public ProFlightInstrumentPanel(DirectOutput directOutput, Device device) {
        this.directOutput = directOutput.dll;
        this.device = device.getPointer();
    }

    public Pointer getDevice() {
        return device;
    }

    public DirectOutputLibrary getDirectOutput() {
        return directOutput;
    }
}
