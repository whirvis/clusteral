/*
 * the MIT License (MIT)
 *
 * Copyright (c) 2023 Trent Summerlin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * the above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.whirvis.edu.clustering.cli.args;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A parameter which takes in an {@code enum}.
 */
public class EnumParam<E extends Enum<E>> extends ProgramParam<E> {

    private final String enumName;
    private final boolean allowNull;
    private final Map<String, E> entries;

    /**
     * Constructs a new {@code EnumParam}.
     *
     * @param displayName the display name for this parameter.
     * @param clazz       the {@code enum} class.
     * @param allowNull   if {@code null} should be allowed.
     * @throws NullPointerException if {@code displayName} is {@code null}.
     * @throws ParamException       if two enumerations have the same name
     *                              after being converted to lowercase.
     */
    public EnumParam(
            String displayName,
            Class<E> clazz,
            boolean allowNull) {
        super(displayName, null);
        Objects.requireNonNull(clazz, "clazz cannot be null");

        Map<String, E> entries = new HashMap<>();
        for (E entry : clazz.getEnumConstants()) {
            String key = entry.name().toLowerCase(Locale.ROOT);
            if (entries.put(key, entry) != null) {
                throw new ParamException("Two enums cannot" +
                        "have the same name even if their casing" +
                        "is different");
            }
        }

        this.enumName = clazz.getSimpleName();
        this.allowNull = allowNull;
        this.entries = entries;
    }

    @Override
    protected E decode(String text) {
        String lowercase = text
                .toLowerCase(Locale.ROOT)
                .replace("-", "_");

        E entry = entries.get(lowercase);
        if (allowNull && lowercase.equals("null")) {
            return null;
        } else if (entry == null) {
            String msg = "Invalid input for enum " +
                    enumName + " (" + text + ")";
            throw new ParamException(msg);
        }

        return entry;
    }

}
