package ch.poiuqwer.saitek.fip4j;

import static ch.poiuqwer.saitek.fip4j.DeviceState.*;

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
public final class DeviceEvent {
    private final Device device;
    private final DeviceState state;

    DeviceEvent(Device device, DeviceState state) {
        this.device = device;
        this.state = state;
    }

    public Device getDevice() {
        return device;
    }

    public boolean isConnected() {
        return state == CONNECTED;
    }

    public boolean isDisconnected() {
        return state == DISCONNECTED;
    }
}
