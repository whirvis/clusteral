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
package io.whirvis.edu.clustering.kmeans;

import io.whirvis.edu.clustering.DataPointFile;

/**
 * Describes how a group of clusters should be initialized before the
 * K-means algorithm is run.
 */
public enum KMeansInitMethod {

    /**
     * The random selection method.
     * <p>
     * As described in the document for Phase 2:
     * <blockquote>
     * "Select the initial cluster centers uniformly at random from
     * the data points."
     * </blockquote>
     *
     * @see DataPointFile#getRandomSelectionClusters(int)
     */
    RANDOM_SELECTION("Random Selection"),

    /**
     * The random partition method.
     * <p>
     * As described in the document for Phase 3:
     * <blockquote>
     * "Starting from empty clusters, first assign each point to a
     * cluster selected uniformly at random and then take the centroids
     * of these initial clusters as the initial centers."
     * </blockquote>
     *
     * @see DataPointFile#getRandomPartitionClusters(int)
     */
    RANDOM_PARTITION("Random Partition"),

    /**
     * The maximin method.
     * <p>
     * As described in the document for Phase 3:
     * <blockquote>
     * Choose the first center arbitrarily from the data points and choose
     * the remaining {@code (K − 1)} centers successively as follows:
     * <p>
     * In iteration <i>i</i> {@code (i = 2, 3, ..., K)}, the <i>i</i>-th
     * center is chosen to be the data point with the greatest squared
     * Euclidean distance to the nearest previously selected {@code (i − 1)}
     * centers.
     * </blockquote>
     *
     * @see DataPointFile#getMaximinClusters(int)
     */
    MAXIMIN("Maximin");

    private final String name;

    KMeansInitMethod(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
