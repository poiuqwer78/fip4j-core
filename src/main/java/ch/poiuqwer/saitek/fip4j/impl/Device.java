package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

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
public class Device {
    private final GUID guid;
    private final String serialNumber;
    private final Pointer pointer;

    public Device(Pointer pointer, GUID guid, String serialNumber) {
        this.pointer = pointer;
        this.guid = guid;
        this.serialNumber = serialNumber;
    }

    public GUID getGuid(){
        return guid;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Pointer getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        return "Device{" +
                "GUID=" + guid +
                ", S/N='" + serialNumber + '\'' +
                '}';
    }
}
