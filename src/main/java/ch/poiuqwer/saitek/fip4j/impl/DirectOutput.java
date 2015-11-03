package ch.poiuqwer.saitek.fip4j.impl;

import com.sun.jna.WString;
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
public class DirectOutput {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectOutput.class);

    public final DirectOutputLibrary dll;

    public DirectOutput(DirectOutputLibrary dll) {
        this.dll = dll;
    }

    public HRESULT call(int code) {
        HRESULT result = HRESULT.of(code);
        switch (result){
            case S_OK:
                if (LOGGER.isDebugEnabled()) {
                    // Expensive operation, only perform if logging really happens on this level.
                    String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                    LOGGER.debug("Call '{}' {}", methodName, result);
                }
                break;
            default:
                String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
                LOGGER.error("Call '{}' {}",methodName,result);
        }
        return result;
    }

    //// Wrappers for low-level calls ////

    public HRESULT initialize(String pluginName){
        return call(dll.DirectOutput_Initialize(new WString(pluginName)));
    }

    public HRESULT deinitialize() {
        return call(dll.DirectOutput_Deinitialize());
    }


}
