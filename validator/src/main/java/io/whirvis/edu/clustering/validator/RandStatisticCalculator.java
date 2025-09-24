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
 * Rand Statistic implementation of {@link ExternalClusterValidator}.
 * <p>
 * Calculation of the Rand Statistic (RS) is reliant on the calculation
 * of the "truth table" between a ground truth (the clusters which each
 * data point actually belongs to) and a generated cluster. The specifics
 * of the truth table itself are laid out in {@link ClusterTruthTable}.
 * <p>
 * Once the truth table is obtained, calculating the Rand Statistic is
 * quite straight forward. From page 437 of the Data Mining book given
 * in the document for Phase 5:
 * <blockquote>
 * "The Rand statistic measures the fraction of true positives and true
 * negatives over all point pairs."
 * </blockquote>
 * <p>
 * This makes the formula for the Rand Statistic:
 * <pre>
 *     RS = (TP + TN) / N
 * </pre>
 * Where {@code TP} is the number true positives, {@code TN} is the number
 * of true negatives, and {@code N} is the number of data point pairs in the
 * entire set.
 */
@SuppressWarnings("unused")
public final class RandStatisticCalculator extends ExternalClusterValidator {

    /**
     * Constructs a new {@code RandStatisticCalculator}.
     */
    public RandStatisticCalculator() {
        super("Rand Statistic", "RS");
    }

    @Override
    protected double calculate(
            PointClusters truth, PointClusters generated) {
        ClusterTruthTable table = new ClusterTruthTable(truth, generated);
        double tp = table.tp, tn = table.tn, n = table.pairs.size();
        return (tp + tn) / n;
    }

}
