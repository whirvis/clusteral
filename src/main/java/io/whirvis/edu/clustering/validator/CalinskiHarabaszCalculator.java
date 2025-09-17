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

import io.whirvis.edu.clustering.DataPoint;
import io.whirvis.edu.clustering.PointCluster;
import io.whirvis.edu.clustering.PointClusters;

/**
 * Calinski-Harabasz implementation of {@link InternalClusterValidator}.
 * <p>
 * The formula for calculating this index can be found on page 453 from
 * the book given in the document for Phase 4:
 * <blockquote>
 * "The Calinski–Harabasz (CH) variance ratio criterion for a given
 * value of {@code k} is defined as follows:
 * <pre>
 * {@code CH = ((n - k) / (k - 1)) * ((tr(Sb) / (tr(Sw))}
 * </pre>
 * where {@code tr(Sw)} and {@code tr(Sb)} are the trace of {@code S}
 * (the sum of the diagonal elements) of the within cluster and between
 * cluster scatter matrices."
 * </blockquote>
 * <p>
 * We know that {@code tr(Sw)} is the intra-dispersion of the clusters.
 * Furthermore, the document for Phase 4 states:
 * <blockquote>
 * "The trace of the within-clusters scatter matrix, {@code tr(Sw)},
 * is the same as SSE, which is already calculated by k-means. Therefore,
 * you do not need to calculate {@code tr(Sw)} separately."
 * </blockquote>
 * <p>
 * This means we can simply use {@link PointClusters#getSumOfSquaredErrors()}
 * in order to calculate the intra-dispersion. However, {@code tr(Sw)} still
 * needs to be calculated.
 * <p>
 * According to the website <a href="https://pyshark.com/calinski-harabasz-index-for-k-means-clustering-evaluation-using-python/#calculate-calinski-harabasz-index">
 * PyShark</a>, {@code tr(Sb)} (or "between group SSE" as the site calls it)
 * can be found by calculating the sum of multiplying the number of points
 * in a cluster by the squared error of its centroid and the barycenter of
 * a dataset.
 */
@SuppressWarnings("unused")
public final class CalinskiHarabaszCalculator extends InternalClusterValidator {

    /**
     * Constructs a new {@code CalinskiHarabaszCalculator}.
     */
    public CalinskiHarabaszCalculator() {
        super("Calinski-Harabasz", "CH");
    }

    @Override
    protected double calculate(PointClusters clusters) {
        double totalIntraDispersion = clusters.getSumOfSquaredErrors();

        double totalInterDispersion = 0.0d;
        DataPoint barycenter = clusters.getBaryCenter();
        for (PointCluster cluster : clusters) {
            DataPoint clusterCentroid = cluster.getCentroid();
            double distance = barycenter.squaredError(clusterCentroid);
            totalInterDispersion += cluster.getPointCount() * distance;
        }

        double numPoints = clusters.getPointCount();
        double numClusters = clusters.getClusterCount();
        double ratio = (numPoints - numClusters) / (numClusters - 1.0d);

        return (totalInterDispersion / totalIntraDispersion) * ratio;
    }

}
