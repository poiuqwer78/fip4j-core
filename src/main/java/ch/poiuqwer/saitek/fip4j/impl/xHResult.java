package ch.poiuqwer.saitek.fip4j.impl;

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
public enum xHResult {

    // Common HRESULT values
    S_OK("Operation successful",0x00000000),
    E_ABORT("Operation aborted",0x80004004),
    E_ACCESSDENIED("Access denied",0x80070005),
    E_FAIL("Unspecified failure",0x80004005),
    E_HANDLE("Invalid handle",0x80070006),
    E_INVALIDARG("Invalid argument",0x80070057),
    E_NOINTERFACE("No such interface",0x80004002),
    E_NOTIMPL("Not implemented",0x80004001),
    E_OUTOFMEMORY("Memory allocation failed",0x8007000E),
    E_POINTER("Invalid pointer",0x80004003),
    E_UNEXPECTED("Unexpected failure",0x8000FFFF),

    // Specific DirectOutput values
    E_PAGENOTACTIVE("Page not active",0xFF040001),
    E_BUFFERTOOSMALL("Buffer too small",0xFF04006F);

    private String description;
    private int code;

    xHResult(String description, int code) {
        this.description = description;
        this.code = code;
    }

    public static xHResult hresult(int code){
        for (xHResult result : xHResult.values()){
            if (result.getCode()==code){
                return result;
            }
        }
        return S_OK;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

}
