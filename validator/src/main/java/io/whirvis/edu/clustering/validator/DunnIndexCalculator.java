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

import io.whirvis.edu.clustering.DiameterMethod;
import io.whirvis.edu.clustering.LinkageMethod;
import io.whirvis.edu.clustering.PointCluster;
import io.whirvis.edu.clustering.PointClusters;

import java.util.Objects;

/**
 * Dunn Index implementation for {@link InternalClusterValidator}.
 * <p>
 * From page 445 of the Data Mining book given in the document for Phase 4:
 * <blockquote>
 * "The Dunn index is defined as the ratio between the minimum distance
 * between point pairs from different clusters and the maximum distance
 * between point pairs from the same cluster."
 * </blockquote>
 * The formula for Dunn Index is:
 * <pre>
 *     Dunn = a / b
 * </pre>
 * Where {@code a} is the minimum inter-cluster distance, and {@code b} is
 * the maximum intra-cluster distance (note that the code in this file refers
 * to it as the intra-cluster diameter). How we calculate the inter-cluster
 * depends on the {@link LinkageMethod} given by the caller. The same is true
 * for the intra-cluster distance, which relies on the {@link DiameterMethod}
 * given by the caller.
 * <p>
 * From page 446 of the book:
 * <blockquote>
 * "The larger the Dunn index the better the clustering because it means
 * even the closest distance between points in different clusters is much
 * larger than the farthest distance between points in the same cluster.
 * However, the Dunn index may be insensitive as the minimum inter-cluster
 * and maximum intra-cluster distances do not capture all the information
 * about a clustering."
 * </blockquote>
 */
@SuppressWarnings("unused")
public final class DunnIndexCalculator extends InternalClusterValidator {

    private final LinkageMethod linkage;
    private final DiameterMethod diameter;

    /**
     * Constructs a new {@code DunnIndexCalculator}.
     *
     * @param linkage  the linkage method to for calculation.
     * @param diameter the diameter method to for calculation.
     */
    public DunnIndexCalculator(
            LinkageMethod linkage, DiameterMethod diameter) {
        super("Dunn Index", "DI");
        this.linkage = Objects.requireNonNull(linkage,
                "linkage cannot be null");
        this.diameter = Objects.requireNonNull(diameter,
                "diameter cannot be null");
    }

    private double getMinInterClusterDist(PointClusters clusters) {
        double lowest = Double.MAX_VALUE;
        for (PointCluster outer : clusters) {
            for (PointCluster inner : clusters) {
                if (outer == inner) {
                    continue; /* do not measure against ourselves */
                }
                double dist = outer.getDistance(inner, linkage);
                if (dist < lowest) {
                    lowest = dist;
                }
            }
        }
        return lowest;
    }

    private double getMaxIntraClusterDiam(PointClusters clusters) {
        double highest = Double.MIN_VALUE;
        for (PointCluster cluster : clusters) {
            double diam = cluster.getDiameter(diameter);
            if (diam > highest) {
                highest = diam;
            }
        }
        return highest;
    }

    @Override
    protected double calculate(PointClusters clusters) {
        double minDist = this.getMinInterClusterDist(clusters);
        double maxDiam = this.getMaxIntraClusterDiam(clusters);
        return minDist / maxDiam;
    }

}
