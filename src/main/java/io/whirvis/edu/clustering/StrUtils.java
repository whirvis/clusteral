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
package io.whirvis.edu.clustering;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Utility methods for converting {@code Object} instances to
 * {@code String}.
 */
/* package-private */
final class StrUtils {

    private StrUtils() {
        /* utility class */
    }

    private static final String JOINER_DELIMITER = ", ";
    private static final String JOINER_SUFFIX = "]";

    private static String getPrefix(Object obj) {
        return obj.getClass().getSimpleName() + "[";
    }

    /**
     * Returns a {@link StringJoiner} to convert the specified object to
     * a string. The returned joiner shall have been constructed with the
     * following arguments:
     * <ul>
     *     <li>delimiter: {@code ", "}</li>
     *     <li>prefix: {@code obj.getClass().getSimpleName() + "["}</li>
     *     <li>suffix: {@code "]"}</li>
     * </ul>
     *
     * @param obj the object being converted to a string.
     * @return the string joiner.
     * @throws NullPointerException if {@code obj} is {@code null}.
     */
    public static StringJoiner getStrJoiner(Object obj) {
        Objects.requireNonNull(obj, "obj cannot be null");
        return new StringJoiner(JOINER_DELIMITER, getPrefix(obj),
                JOINER_SUFFIX);
    }

    /**
     * Returns a {@link StringJoiner} for an object adding to the result
     * of its super class's call to {@code toString()}. This strips the
     * suffix of the original {@code toString()} call, using the result
     * as the prefix for the new string joiner. The delimiter and suffix
     * shall be the same as before.
     * <p>
     * <b>Requirements:</b> It is assumed the super class used
     * {@link #getStrJoiner(Object)}, and that the class calling
     * this method extends from said super class. If this is not
     * the case, the result shall be undefined.
     *
     * @param prev the result of {@code super.toString()}.
     * @param obj  the object being converted to a string.
     * @return the string joiner.
     * @throws NullPointerException if {@code prev} or {@code obj}
     *                              are {@code null}.
     */
    @SuppressWarnings("unused")
    public static StringJoiner getStrJoiner(String prev, Object obj) {
        Objects.requireNonNull(prev, "prev cannot be null");
        Objects.requireNonNull(obj, "obj cannot be null");

        int endIndex = prev.length() - JOINER_SUFFIX.length();
        String prefix = prev.substring(0, endIndex);
        return new StringJoiner(JOINER_DELIMITER, prefix, JOINER_SUFFIX);
    }

}
