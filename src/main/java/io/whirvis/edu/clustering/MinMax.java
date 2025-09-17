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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Methods for working with min-max data.
 *
 * @see MinMaxPair
 */
@SuppressWarnings("unused")
public final class MinMax {

    private MinMax() {
        /* utility class */
    }

    /**
     * Returns the element with the lowest value.
     *
     * @param elems the elements to compare.
     * @param <T>   the element type.
     * @return the element with the lowest value.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     * @see #getMinAndMax(Iterable)
     */
    public static <T extends Comparable<T>> T getMin(Iterable<T> elems) {
        Objects.requireNonNull(elems, "elems cannot be null");

        Iterator<T> elemsI = elems.iterator();
        if (!elemsI.hasNext()) {
            String msg = "cannot find min with no elements";
            throw new IllegalArgumentException(msg);
        }

        T lowest = elemsI.next();
        while (elemsI.hasNext()) {
            T current = elemsI.next();
            if (current.compareTo(lowest) < 0) {
                lowest = current;
            }
        }
        return lowest;
    }

    /**
     * Returns the element with the lowest value.
     *
     * @param elems the elements to compare.
     * @param <T>   the element type.
     * @return the element with the lowest value.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     * @see #getMinAndMax(Iterable)
     */
    @SafeVarargs
    public static <T extends Comparable<T>> T getMin(T... elems) {
        return getMin(Arrays.asList(elems));
    }

    /**
     * Returns the element with the highest value.
     *
     * @param elems the elements to compare.
     * @param <T>   the element type.
     * @return the element with the highest value.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     * @see #getMinAndMax(Iterable)
     */
    public static <T extends Comparable<T>> T getMax(Iterable<T> elems) {
        Objects.requireNonNull(elems, "elems cannot be null");

        Iterator<T> elemsI = elems.iterator();
        if (!elemsI.hasNext()) {
            String msg = "cannot find max with no elements";
            throw new IllegalArgumentException(msg);
        }

        T highest = elemsI.next();
        while (elemsI.hasNext()) {
            T current = elemsI.next();
            if (current.compareTo(highest) > 0) {
                highest = current;
            }
        }
        return highest;
    }

    /**
     * Returns the element with the highest value.
     *
     * @param elems the elements to compare.
     * @param <T>   the element type.
     * @return the element with the highest value.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     * @see #getMinAndMax(Iterable)
     */
    @SafeVarargs
    public static <T extends Comparable<T>> T getMax(T... elems) {
        return getMax(Arrays.asList(elems));
    }

    /**
     * Returns the elements with the lowest and highest values.
     *
     * @param elems the elements to compare.
     * @param <T>   the element type.
     * @return the elements with the lowest and highest values.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     */
    public static <T extends Comparable<T>> MinMaxPair<T> getMinAndMax(
            Iterable<T> elems) {
        Objects.requireNonNull(elems, "elems cannot be null");

        Iterator<T> elemsI = elems.iterator();
        if (!elemsI.hasNext()) {
            String msg = "cannot find min or max with no elements";
            throw new IllegalArgumentException(msg);
        }
        T first = elemsI.next();

        T lowest = first, highest = first;
        while (elemsI.hasNext()) {
            T current = elemsI.next();
            if (current.compareTo(lowest) < 0) {
                lowest = current;
            } else if (current.compareTo(highest) > 0) {
                highest = current;
            }
        }
        return new MinMaxPair<>(lowest, highest);
    }

}
