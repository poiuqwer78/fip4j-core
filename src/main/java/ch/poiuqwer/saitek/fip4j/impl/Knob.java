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
public enum Knob {

    LEFT(0x00000010, 0x00000008),
    RIGHT(0x00000004,0x00000002);

    public final int counterClockwise;
    public final int clockwise;

    Knob(int ccw, int cw) {
        this.counterClockwise = ccw;
        this.clockwise = cw;
    }

}
