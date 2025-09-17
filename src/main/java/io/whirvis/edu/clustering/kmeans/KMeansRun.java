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

import io.whirvis.edu.clustering.PointClusters;

/**
 * Contains the results of a {@link KMeans} run.
 *
 * @see KMeansRuns
 */
public final class KMeansRun {

    /**
     * The run number, starting from zero.
     */
    public final int runNum;

    /**
     * The SSEs for each iteration.
     * <p>
     * <b>Note:</b> "SSE" means "sum-of-squared errors".
     */
    public final double[] iterations;

    /**
     * The initial sum-of-squared errors value.
     */
    public final double initialSse;

    /**
     * The final sum-of-squared errors value.
     */
    public final double finalSse;

    /**
     * The number of iterations before convergence.
     */
    public final int numIterations;

    /**
     * The clusters that formed as a result.
     */
    public final PointClusters clusters;

    /**
     * Constructs a new {@code ClusterRunResult}.
     *
     * @param runNum        the run number, starting from zero.
     * @param iterations    the SSEs for each iteration.
     * @param initialSse    the initial sum-of-squared errors.
     * @param finalSse      the final sum-of-squared errors.
     * @param numIterations the number of iterations before convergence.
     * @param clusters      the clusters that formed as a result.
     */
    /* package-private */
    KMeansRun(
            int runNum,
            double[] iterations,
            double initialSse,
            double finalSse,
            int numIterations,
            PointClusters clusters) {
        this.runNum = runNum;
        this.iterations = iterations;
        this.initialSse = initialSse;
        this.finalSse = finalSse;
        this.numIterations = numIterations;
        this.clusters = clusters;
    }

}
