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

import java.util.Iterator;
import java.util.Objects;

/**
 * Methods for working with Z-score data.
 *
 * @see ZScoreNormalizer
 */
public final class ZScore {

    private ZScore() {
        /* utility class */
    }

    /**
     * Calculates the mean from the given elements.
     *
     * @param elems the elements.
     * @return the mean of the elements, {@link Double#NaN} if there are
     * no elements (and therefore the mean cannot be calculated).
     * @see #getStandardDeviation(Iterable, double)
     */
    private static double getMean(Iterable<Double> elems) {
        Iterator<Double> elemsI = elems.iterator();

        int valueCount = 0;
        double total = 0.0d;
        while (elemsI.hasNext()) {
            valueCount += 1;
            total += elemsI.next();
        }

        if (valueCount <= 0) {
            return Double.NaN;
        }

        return total / valueCount;
    }

    /**
     * Calculates the standard deviation from the given elements and the
     * estimated mean.
     *
     * @param elems the elements.
     * @param mean  the estimated mean.
     * @return the standard deviation, {@link Double#NaN} if there are
     * less than two elements (and therefore the standard deviation cannot
     * be calculated).
     * @see #getMean(Iterable)
     */
    private static double getStandardDeviation(Iterable<Double> elems,
                                               double mean) {
        Iterator<Double> elemsI = elems.iterator();
        if (Double.isNaN(mean)) {
            return Double.NaN;
        }

        int valueCount = 0;
        double squaredTotals = 0.0d;
        while (elemsI.hasNext()) {
            valueCount += 1;
            double value = (elemsI.next() - mean);
            squaredTotals += value * value;
        }

        if (valueCount <= 1) {
            return Double.NaN;
        }

        return Math.sqrt(squaredTotals / (valueCount - 1));
    }

    /**
     * Creates a Z-score normalization calculator.
     *
     * @param elems the elements to use.
     * @return a calculator for the Z-score normalization.
     * @throws NullPointerException     if {@code elems} is {@code null}.
     * @throws IllegalArgumentException if {@code elems} has no elements.
     */
    public static ZScoreNormalizer getNormalizer(Iterable<Double> elems) {
        Objects.requireNonNull(elems, "elems cannot be null");

        Iterator<Double> elemsI = elems.iterator();
        if (!elemsI.hasNext()) {
            String msg = "Cannot create normalizer with no elements";
            throw new IllegalArgumentException(msg);
        }

        double mean = getMean(elems);
        double standardDeviation = getStandardDeviation(elems, mean);
        return new ZScoreNormalizer(mean, standardDeviation);
    }

}
