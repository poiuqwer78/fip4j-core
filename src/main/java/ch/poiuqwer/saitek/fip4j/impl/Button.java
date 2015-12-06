package ch.poiuqwer.saitek.fip4j.impl;

import com.google.common.base.Preconditions;

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
public enum Button {

    S1(0x00000020,1),
    S2(0x00000040,2),
    S3(0x00000080,3),
    S4(0x00000100,4),
    S5(0x00000200,5),
    S6(0x00000400,6),
    UP(0x00000000,7),
    DOWN(0x00000000,8);

    private final int value;
    private final int led;

    Button(int value, int led){
        this.value = value;
        this.led = led;
    }

    public static Button number(int i){
        Preconditions.checkArgument(i>=0 && i <=6);
        return Button.valueOf("S"+i);
    }

    public int getValue() {
        return value;
    }

    public int getLed() {
        return led;
    }
}
