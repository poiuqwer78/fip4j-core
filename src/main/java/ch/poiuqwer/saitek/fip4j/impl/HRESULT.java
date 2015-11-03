package ch.poiuqwer.saitek.fip4j.impl;

import java.util.HashMap;
import java.util.Map;

import static ch.poiuqwer.saitek.fip4j.impl.Severity.*;

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
public enum HRESULT {
    // Common HRESULT values
    S_OK("Operation successful"),
    E_ABORT("Operation aborted"),
    E_ACCESSDENIED("Access denied"),
    E_FAIL("Unspecified failure"),
    E_HANDLE("Invalid handle"),
    E_INVALIDARG("Invalid argument"),
    E_NOINTERFACE("No such interface"),
    E_NOTIMPL("Not implemented"),
    E_OUTOFMEMORY("Memory allocation failed"),
    E_POINTER("Invalid pointer"),
    E_UNEXPECTED("Unexpected failure"),

    // Specific DirectOutput values
    E_PAGENOTACTIVE("Page not active"),
    E_BUFFERTOOSMALL("Buffer too small");

    private final String description;

    HRESULT(String description) {
        this.description = description;
    }

    private static final Map<Integer, HRESULT> lookup=new HashMap<Integer, HRESULT>(){{
        put(0x00000000,S_OK);
        put(0x80004004,E_ABORT);
        put(0x80070005,E_ACCESSDENIED);
        put(0x80004005,E_FAIL);
        put(0x80070006,E_HANDLE);
        put(0x80070057,E_INVALIDARG);
        put(0x80004002,E_NOINTERFACE);
        put(0x80004001,E_NOTIMPL);
        put(0x8007000E,E_OUTOFMEMORY);
        put(0x80004003,E_POINTER);
        put(0x8000FFFF,E_UNEXPECTED);
        put(0xFF040001,E_PAGENOTACTIVE);
        put(0xFF04006F,E_BUFFERTOOSMALL);
    }};

    public static HRESULT of(int code){
        HRESULT result = lookup.get(code);
        return result!=null?result:E_UNEXPECTED;
    }

    @Override
    public String toString() {
        return "["+name()+"] "+description;
    }
}
