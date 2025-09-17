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

/**
 * Fowlkes-Mallows implementation of {@link ExternalClusterValidator}.
 * <p>
 * Calculation of the Fowlkes-Mallows measure (FM) relies on the calculation
 * of the "truth table" between a ground truth (the clusters which each
 * data point actually belongs to) and a generated cluster. The specifics
 * of the truth table itself are laid out in {@link ClusterTruthTable}.
 * <p>
 * Once the truth table is obtained, calculating the Fowlkes-Mallows measure
 * is quite straight forward. From page 437 of the Data Mining book given in
 * the document for Phase 5:
 * <blockquote>
 * "The Fowlkes–Mallows (FM) measure is defined as the geometric mean of
 * the pairwise precision and recall."
 * </blockquote>
 * In this case, the "pairwise precision" and "recall" are defined as:
 * <pre>
 * precision = TP / (TP + FP)
 * recall = TP / (TP + FN)
 * </pre>
 * <p>
 * This makes the formula for the Fowlkes-Mallows measure:
 * <pre>
 * FM = sqrt(precision * recall) = TP / sqrt((TP + FN) * (TP + FP))
 * </pre>
 * Where {@code TP} is the number true positives, {@code FN} is the number
 * of false negatives, and {@code FP} is the number of false positives.
 */
@SuppressWarnings("unused")
public final class FowlkesMallowsCalculator extends ExternalClusterValidator {

    /**
     * Constructs a new {@code FowlkesMallowsMeasureCalculator}.
     */
    public FowlkesMallowsCalculator() {
        super("Fowlkes-Mallows", "FM");
    }

    @Override
    protected double calculate(
            PointClusters truth, PointClusters generated) {
        ClusterTruthTable table = new ClusterTruthTable(truth, generated);
        double tp = table.tp, fp = table.fp, fn = table.fn;
        return tp / Math.sqrt((tp + fn) * (tp + fp));
    }

}
