package ch.poiuqwer.saitek.fip4j;

import java.util.Objects;

import static ch.poiuqwer.saitek.fip4j.KnobState.*;

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
public final class KnobEvent {
    private final Page page;
    private final Knob knob;
    private final KnobState state;

    KnobEvent(Page page, Knob knob, KnobState state) {
        this.page = page;
        this.knob = knob;
        this.state = state;
    }

    public Device getDevice() {
        return page.getDevice();
    }

    public Page getPage() {
        return page;
    }

    public boolean isRelevantFor(Page page) {
        return Objects.equals(this.page, page);
    }

    public Knob getKnob() {
        return knob;
    }

    public boolean isTurnedClockwise() {
        return state == TURNED_CLOCKWISE;
    }

    public boolean isTurnedCounterclockwise() {
        return state == TURNED_COUNTERCLOCKWISE;
    }
}
