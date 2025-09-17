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
package io.whirvis.edu.clustering.validator;

import io.whirvis.edu.clustering.PointClusters;

import java.util.Objects;

/**
 * Used in determining the quality of generated clusters.
 */
public abstract class ExternalClusterValidator
        extends ClusterValidator {

    /**
     * Constructs a new {@code ExternalClusterIndexValidator}.
     *
     * @param name         the name of this validator.
     * @param abbreviation the abbreviation of this validator's name.
     * @throws NullPointerException if {@code name} or  {@code abbreviation}
     *                              are {@code null}.
     */
    public ExternalClusterValidator(
            String name, String abbreviation) {
        super(ClusterValidatorType.EXTERNAL, name, abbreviation);
    }

    /**
     * Calculates the index for a given set of clusters.
     *
     * @param truth     the true clusters.
     * @param generated the generated clusters.
     * @return the calculated index.
     * @throws NullPointerException     if {@code truth} or {@code generated}
     *                                  are {@code null}.
     * @throws IllegalArgumentException if {@code truth} or {@code generated}
     *                                  have less than two clusters.
     */
    public final double calculateIndex(
            PointClusters truth, PointClusters generated) {
        Objects.requireNonNull(generated, "clusters cannot be null");
        if (truth.getClusterCount() < 2
                || generated.getClusterCount() < 2) {
            String msg = "There must be at least two clusters in each set";
            throw new IllegalArgumentException(msg);
        }
        return this.calculate(truth, generated);
    }

    /**
     * Implementation for
     * {@link #calculateIndex(PointClusters, PointClusters)}.
     *
     * @param truth     the true clusters.
     * @param generated the generated clusters.
     * @return the calculated index.
     */
    protected abstract double calculate(
            PointClusters truth, PointClusters generated);

}
