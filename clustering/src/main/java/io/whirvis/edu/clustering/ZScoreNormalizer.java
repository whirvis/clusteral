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

/**
 * Calculator for Z-score normalization.
 *
 * @see ZScore#getNormalizer(Iterable)
 */
@SuppressWarnings("unused")
public final class ZScoreNormalizer {

    private final double mean;
    private final double standardDeviation;

    /**
     * Constructs a new {@code ZScoreNormalizer}.
     * <p>
     * <b>Accessibility:</b> This constructor should only be called by
     * {@link StrUtils}.
     *
     * @param mean              the mean of the values.
     * @param standardDeviation the standard deviation.
     */
    /* package-private */
    ZScoreNormalizer(double mean, double standardDeviation) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Returns the mean of the values for this pair.
     *
     * @return the mean of the values for this pair.
     */
    public double getMean() {
        return this.mean;
    }

    /**
     * Returns the standard deviation of this pair.
     *
     * @return the standard deviation of this pair.
     */
    public double getStandardDeviation() {
        return this.standardDeviation;
    }

    /**
     * Normalizes a given value based on the mean and standard deviation
     * supplied at construction.
     *
     * @param value the value to normalize.
     * @return the normalized value.
     * @see #normalize(double, double, double)
     * @see NormalizationType#Z_SCORE
     */
    public double normalize(double value) {
        /*
         * If standardDeviation has a value of NaN, then Java will simply
         * return NaN as a result of division. As such, there is no need to
         * check if the value is NaN beforehand.
         */
        return (value - mean) / standardDeviation;
    }

    /**
     * Shorthand for {@link #normalize(double)} which converts the original
     * {@code 0.0} to {@code 1.0} scale to the desired scale.
     *
     * @param value the value to normalize.
     * @param low   the low end of the scale.
     * @param high  the high end of the scale.
     * @return the normalized value.
     * @throws IllegalArgumentException if {@code low} does not have a
     *                                  value less than {@code high}.
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
