package ch.poiuqwer.saitek.fip4j.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

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
class GUID {
    private static final String GUID_PATTERN = "\\{[A-F0-9]{8}(?:-[A-F0-9]{4}){3}-[A-F0-9]{12}\\}";
    private static final Predicate<String> syntax = Pattern.compile(GUID_PATTERN).asPredicate();

    private final UUID uuid;

    /** Create a GUID from its string representation.
     * @param formatted in the form of e.g. {29DAD506-F93B-4F20-85FA-1E02C04FAC17}
     */
    public static GUID fromString(String formatted){
        checkArgument(syntax.test(formatted));
        formatted=formatted.substring(1,37).toLowerCase();
        return new GUID(UUID.fromString(formatted));
    }

    /**
     * Create a GUID from a byte array
     * @param bytes byte array of length 16 representing the GUID
     */
    public static GUID fromBinary(byte[] bytes){
        checkArgument(bytes.length == 16);
        ByteBuffer source = ByteBuffer.wrap(bytes);
        ByteBuffer target = ByteBuffer.allocate(16).
                order(ByteOrder.LITTLE_ENDIAN).
                putInt(source.getInt()).
                putShort(source.getShort()).
                putShort(source.getShort()).
                order(ByteOrder.BIG_ENDIAN).
                putLong(source.getLong());
        target.rewind();
        return new GUID(new UUID(target.getLong(), target.getLong()));
    }

    private GUID(UUID uuid){
        this.uuid=uuid;
    }

    public String toString(){
        return "{"+uuid.toString().toUpperCase()+"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GUID GUID = (ch.poiuqwer.saitek.fip4j.impl.GUID) o;
        return Objects.equals(uuid, GUID.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
