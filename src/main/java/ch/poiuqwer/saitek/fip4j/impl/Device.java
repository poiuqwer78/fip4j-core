package ch.poiuqwer.saitek.fip4j.impl;

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.util.*;

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
    private final String serialNumber;
    private final Pointer pointer;

    private final List<Page> pages = new ArrayList<>();
    private Page activePage;

    public Page addPage() {
        Preconditions.checkState(connected);
        Page page = new Page(this, pages.size());
        pages.add(page);
        LibraryManager.getDirectOutput().addPage(page, PageState.ACTIVE);
        activePage = page;
        return page;
    }

    public Page getActivePage() {
        Preconditions.checkState(connected);
        return activePage;
    }

//
//    public void removePage(Page page){
//        // Todo: check if indexes change of pages if removing a page somewhere in the middle.
//        LibraryManager.getDirectOutput().removePage(page);
//        pages.remove(page.getIndex());
//    }

    private boolean connected = true;

    public boolean isConnected() {
        return connected;
    }

    private final Set<PageChangeEventHandler> pageChangeEventHandlers = new HashSet<>();
    private final Set<SoftButtonEventHandler> softButtonEventHandlers = new HashSet<>();

    public Device(Pointer pointer, String serialNumber) {
        this.pointer = pointer;
        this.serialNumber = serialNumber;
    }

    public void addPageChangeEventHandler(PageChangeEventHandler handler) {
        pageChangeEventHandlers.add(handler);
    }

    public void removePageChangeEventHandler(PageChangeEventHandler handler) {
        pageChangeEventHandlers.remove(handler);
    }

    public void addSoftButtonEventHandler(SoftButtonEventHandler handler) {
        softButtonEventHandlers.add(handler);
    }

    public void removeSoftButtonEventHandler(SoftButtonEventHandler handler) {
        softButtonEventHandlers.remove(handler);
    }

    public void firePageChangeEventHandlers(int dwPage, byte bSetActive) {
        if (dwPage == activePage.getIndex()) {
            if (bSetActive == 0) {
                activePage.deactivate();
                activePage = null;
            }
        } else {
            if (bSetActive == 1) {
                activePage = pages.get(dwPage);
                activePage.activate();
            }
        }
        for (PageChangeEventHandler handler : pageChangeEventHandlers) {
            if (bSetActive == 1) {
                handler.pageActivated(pages.get(dwPage));
            } else {
                handler.pageDeactivated(pages.get(dwPage));
            }
        }
    }

    public void fireSoftButtonEventHandlers(int dwButtons) {
        for (SoftButtonEventHandler handler : softButtonEventHandlers) {

        }
    }


    public void disconnect() {
        this.connected = false;
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
                "S/N='" + serialNumber + '\'' +
                '}';
    }
}
