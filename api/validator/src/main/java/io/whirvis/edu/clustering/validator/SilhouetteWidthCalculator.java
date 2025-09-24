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
 * Silhouette Width implementation of {@link InternalClusterValidator}.
 * <p>
 * Calculation of the silhouette width (SW) is reliant on the calculation
 * of the <i>silhouette coefficient.</i> From page 446 of the Data Mining
 * book given in the document for Phase 4:
 * <blockquote>
 * "The silhouette coefficient is a measure of both cohesion and separation
 * of clusters, and is based on the difference between the average distance
 * to points in the closest cluster and to points in the same cluster."
 * </blockquote>
 * <p>
 * The formula for the coefficient is:
 * <pre>
 *     Si = (b - a) / max(a, b)
 * </pre>
 * Where {@code a} is the mean distance from a point to all <i>other</i>
 * points in its cluster; and {@code b} is the mean of the distances from
 * a point to all points in the cluster closest to the point. However,
 * this is not enough to calculate the silhouette width.
 * <p>
 * To calculate the silhouette width, we simply take the coefficients for
 * every point in every cluster and add them up. After that, we divide the
 * totalled up value by the number of points to get the silhouette width.
 * <p>
 * <b>Note:</b> The above formula has been simplified from the book as it
 * uses many symbols which JavaDocs cannot contain. Furthermore, the closest
 * cluster to a point is defined as the cluster whose centroid is nearest to
 * the point.
 */
@SuppressWarnings("unused")
public final class SilhouetteWidthCalculator extends InternalClusterValidator {

    /**
     * Constructs a new {@code SilhouetteWidthCalculator}.
     */
    public SilhouetteWidthCalculator() {
        super("Silhouette Width", "SW");
    }

    @Override
    protected double calculate(PointClusters clusters) {
        double total = 0;
        int pointsCounted = 0;
        for (PointCluster cluster : clusters) {
            for (DataPoint point : cluster) {
                PointCluster nearest = clusters.getNearestClusterByCentroid(
                        point, false, true);

                double clusterCo = cluster.getMeanDistance(point);
                double nearestCo = nearest.getMeanDistance(point);
                double compactness = Math.max(nearestCo, clusterCo);
                if (Double.isNaN(compactness)) {
                    continue; /* anomaly, ignore this */
                }

                total += ((nearestCo - clusterCo) / compactness);
                pointsCounted += 1;
            }
        }
        return total / pointsCounted;
    }

}
