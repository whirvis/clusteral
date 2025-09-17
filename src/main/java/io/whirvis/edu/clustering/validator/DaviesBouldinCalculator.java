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

import io.whirvis.edu.clustering.PointCluster;
import io.whirvis.edu.clustering.PointClusters;

/**
 * Davies-Bouldin implementation for {@link InternalClusterValidator}.
 * <p>
 * Unlike other index calculators, this calculator seeks to minimize the
 * calculated index as opposed to maximizing it. From page 446 of the Data
 * Mining book given in the document for Phase 4:
 * <blockquote>
 * The smaller the DB value the better the clustering, as it means that
 * the clusters are well separated (i.e., the distance between cluster
 * means is large), and each cluster is well represented by its mean (i.e.,
 * has a small spread).
 * </blockquote>
 * <p>
 * Before we calculate Davies-Bouldin, we must first know how to calculate
 * the measure for a pair of clusters. The formula for this is:
 * <pre>
 *     DBij = (Di - Dj) / (Mi - Mj)
 * </pre>
 * Where {@code D} is the dispersion or spread of points around the cluster
 * mean; and {@code M} is simply the cluster mean. Note that {@code i} and
 * {@code j} refer to the clusters. We can get the mean for a cluster using
 * {@link PointCluster#getMean(boolean)} and the dispersion for a cluster
 * with {@link PointCluster#getDispersion()}.
 * <p>
 * Knowing this, we now go through every cluster {@code i} and find cluster
 * {@code j} which produces the maximum result for {@code DBij}. We compute
 * this for all clusters and add up the results. We then divide the total by
 * the number of clusters to get the final result.
 */
@SuppressWarnings("unused")
public final class DaviesBouldinCalculator extends InternalClusterValidator {

    /**
     * Constructs a new {@code DaviesBouldinCalculator}.
     */
    public DaviesBouldinCalculator() {
        super("Davies-Bouldin", "DB");
    }

    @Override
    protected double calculate(PointClusters clusters) {
        double total = 0.0d;
        for (PointCluster outer : clusters) {
            double outerDispersion = outer.getDispersion();
            double outerCompactness = outer.getCompactness();

            double max = Double.MIN_VALUE;
            for (PointCluster inner : clusters) {
                if (outer == inner) {
                    continue; /* do not measure against ourselves */
                }

                double innerDispersion = inner.getDispersion();
                double innerCompactness = inner.getCompactness();

                double finalDispersion = outerDispersion + innerDispersion;
                double finalCompactness = outerCompactness - innerCompactness;
                double result = finalDispersion / finalCompactness;

                if (result > max) {
                    max = result;
                }
            }
            total += max;
        }
        return total / clusters.getClusterCount();
    }

}
