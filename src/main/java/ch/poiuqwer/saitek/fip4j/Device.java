package ch.poiuqwer.saitek.fip4j;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static ch.poiuqwer.saitek.fip4j.ButtonState.PRESSED;
import static ch.poiuqwer.saitek.fip4j.ButtonState.RELEASED;
import static ch.poiuqwer.saitek.fip4j.PageState.ACTIVE;
import static ch.poiuqwer.saitek.fip4j.PageState.INACTIVE;

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
public final class Device {
    private static final Logger LOGGER = LoggerFactory.getLogger(Device.class);
    private final String serialNumber;
    private final Pointer pointer;
    private final DirectOutput directOutput;
    private final EventBus eventBus;

    private boolean connected = true;

    private final List<Page> pages = new ArrayList<>();
    private Page activePage;

    Device(DirectOutput directOutput, Pointer pointer, String serialNumber) {
        this.directOutput = directOutput;
        this.eventBus = directOutput.getEventBus();
        this.pointer = pointer;
        this.serialNumber = serialNumber;
    }

    public DirectOutput getDirectOutput() {
        return directOutput;
    }

    public boolean isConnected() {
        return connected;
    }

    void setActivePage(Page activePage) {
        this.activePage = activePage;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Page addPage() {
        Preconditions.checkState(connected);
        Page page = new Page(this, pages.size());
        pages.add(page);
        directOutput.addPage(page, ACTIVE);
        activePage = page;
        return page;
    }

    public void removePage(Page page) {
        directOutput.removePage(page);
        page.kill();
    }

    public List<Page> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public Page getActivePage() {
        Preconditions.checkState(connected);
        return activePage;
    }

    void disconnect() {
        this.connected = false;
        pages.forEach(Page::deactivate);
    }

    Pointer getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        return "Device{" +
                "S/N='" + serialNumber + '\'' +
                '}';
    }
}
