package ch.poiuqwer.saitek.fip4j;

import java.util.Objects;

import static ch.poiuqwer.saitek.fip4j.ButtonState.*;

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
public final class ButtonEvent {
    private final Page page;
    private final Button button;
    private final ButtonState state;

    ButtonEvent(Page page, Button button, ButtonState state) {
        this.page = page;
        this.button = button;
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

    public Button getButton() {
        return button;
    }

    public boolean isPressed() {
        return state == PRESSED;
    }

    public boolean isReleased() {
        return state == RELEASED;
    }

}
