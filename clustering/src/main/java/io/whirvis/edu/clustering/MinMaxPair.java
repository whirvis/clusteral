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

/**
 * A container for a minimum and maximum value.
 *
 * @param <T> the type to contain.
 * @see MinMax#getMinAndMax(Iterable)
 */
@SuppressWarnings("unused")
public final class MinMaxPair<T extends Comparable<T>> {

    private final T min;
    private final T max;

    /**
     * Constructs a new {@code MinMax} pair.
     * <p>
     * <b>Accessibility:</b> This constructor should only be called by
     * {@link StrUtils}.
     *
     * @param min the minimum value.
     * @param max the maximum value.
     * @throws NullPointerException if {@code min} or {@code max}
     *                              are {@code null}.
     */
    /* package-private */
    MinMaxPair(T min, T max) {
        this.min = Objects.requireNonNull(min, "min cannot be null");
        this.max = Objects.requireNonNull(max, "max cannot be null");
    }

    /**
     * Returns the minimum value in this pair.
     *
     * @return the minimum value in this pair.
     */
    public T getMin() {
        return this.min;
    }

    /**
     * Returns the maximum value in this pair.
     *
     * @return the maximum value in this pair.
     */
    public T getMax() {
        return this.max;
    }

    /**
     * Normalizes a value to a scale of {@code 0.0} to {@code 1.0} based on
     * the minimum and maximum values of this pair.
     * <p>
     * <b>Note:</b> This method only works for pairs which contain numerical
     * values (i.e., instances of {@code Number}).
     *
     * @param value the value to normalize.
     * @return the normalized value, {@link Double#NaN} if the minimum and
     * maximum values of this pair are identical.
     * @throws UnsupportedOperationException if the contained type is not an
     *                                       instance of {@code Number}.
     * @see #normalize(double, double, double)
     * @see NormalizationType#MIN_MAX
     */
    public double normalize(double value) {
        if (!(min instanceof Number)) {
            String msg = "Can only normalize numerical pairs";
            throw new UnsupportedOperationException(msg);
        }

        double dMin = ((Number) min).doubleValue();
        double dMax = ((Number) max).doubleValue();

        double scale = (dMax - dMin);
        if (scale == 0.0d) {
            return Double.NaN;
        }

        return (value - dMin) / scale;
    }

    /**
     * Shorthand for {@link #normalize(double)} which converts the original
     * {@code 0.0} to {@code 1.0} scale to the desired scale.
     *
     * @param value the value to normalize.
     * @param low   the low end of the scale.
     * @param high  the high end of the scale.
     * @return the normalized value.
     * @throws IllegalArgumentException      if {@code low} does not have a
     *                                       value less than {@code high}.
     * @throws UnsupportedOperationException if the contained type is not an
     *                                       instance of {@code Number};
     *                                       if the minimum and maximum values
     *                                       of this pair are identical.
     */
    public double normalize(double value, double low, double high) {
        if (low >= high) {
            String msg = "low must be less than high";
            throw new IllegalArgumentException(msg);
        }
        double normalized = this.normalize(value);
        return ((high - low) * normalized) + low;
    }

}
