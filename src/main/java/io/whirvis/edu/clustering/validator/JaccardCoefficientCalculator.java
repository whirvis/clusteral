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
 * Jaccard Coefficient implementation of {@link ExternalClusterValidator}.
 * <p>
 * Calculation of the Jaccard Coefficient (JC) relies on the calculation
 * of the "truth table" between a ground truth (the clusters which each
 * data point actually belongs to) and a generated cluster. The specifics
 * of the truth table itself are laid out in {@link ClusterTruthTable}.
 * <p>
 * Once the truth table is obtained, calculating the Jaccard Coefficient
 * is quite straight forward. From page 436 of the Data Mining book given
 * in the document for Phase 5:
 * <blockquote>
 * "The Jaccard Coefficient measures the fraction of true positive point
 * pairs, but after ignoring the true negatives."
 * </blockquote>
 * <p>
 * This makes the formula for the Jaccard Coefficient:
 * <pre>
 * JC = TP / (TP + FN + FP)
 * </pre>
 * Where {@code TP} is the number true positives, {@code FN} is the number
 * of false negatives, and {@code FP} is the number of false positives.
 */
@SuppressWarnings("unused")
public final class JaccardCoefficientCalculator extends ExternalClusterValidator {

    /**
     * Constructs a new {@code JaccardIndexCalculator}.
     */
    public JaccardCoefficientCalculator() {
        super("Jaccard Coefficient", "JC");
    }

    @Override
    protected double calculate(
            PointClusters truth, PointClusters generated) {
        ClusterTruthTable table = new ClusterTruthTable(truth, generated);
        double tp = table.tp, fp = table.fp, fn = table.fn;
        return tp / (tp + fn + fp);
    }

}
