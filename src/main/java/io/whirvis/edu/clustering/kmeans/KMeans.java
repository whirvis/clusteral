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

import io.whirvis.edu.clustering.*;
import io.whirvis.edu.clustering.cli.ClusteringArgs;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A runner for the K-means algorithm.
 *
 * @see DataPointFile
 * @see KMeansRun
 */
@SuppressWarnings("unused")
public final class KMeans {

    private KMeans() {
        /* utility class */
    }

    /* note: rcomn = random centroid on multiple nearest */

    private static KMeansRun performRun(
            DataPointFile pointFile,
            KMeansInitMethod method,
            int runNum,
            int numClusters,
            int maxIterations,
            double convergence,
            boolean rcomn) {
        Objects.requireNonNull(pointFile, "pointFile cannot be null");
        Objects.requireNonNull(method, "method cannot be null");
        if (numClusters <= 0) {
            String msg = "numClusters must be positive";
            throw new IllegalArgumentException(msg);
        }

        PointClusters clusters = pointFile.getClusters(method, numClusters);

        double initialSse = 0;
        int numIterations = 0;
        double[] iterations = new double[maxIterations];

        double previousSse = Double.POSITIVE_INFINITY;
        for (int i = 0; i < maxIterations; i++) {
            /*
             * Attempt to calculate the SSE for this iteration. If this
             * fails due to an anomaly, simply abort the run and print
             * an appropriate error message. Furthermore, return positive
             * infinity as the calculations will no longer be valid.
             */
            double iterationSse;

            /*
             * Clear all points from each cluster before continuing. This
             * used to be done at the end of this for-loop. However, doing
             * so resulted in empty clusters anytime the algorithm did not
             * converge. This is because the points would be cleared, and
             * then the nearest points never re-grouped.
             */
            for (PointCluster cluster : clusters) {
                cluster.clearPoints();
            }

            clusters.groupPointsByNearestCentroid(rcomn);
            clusters.fixCoincidentCenters();
            iterationSse = clusters.getSumOfSquaredErrors();

            /* save initial SSE before first cluster iteration */
            if (i == 0) {
                initialSse = iterationSse;
            }

            iterations[i] = iterationSse;

            /*
             * Sanity check: The current iteration should never have an
             * SSE greater than the previous iteration. If it does, then
             * something has gone wrong and the program should stop.
             */
            if (iterationSse > previousSse) {
                throw new ClusterException("SSE should never increase");
            }

            /*
             * If the improvement is less than the convergence threshold,
             * then the algorithm has converged. There is no need for any
             * more iterations, so we should break here.
             *
             * Formula: (SSE(t – 1) – SSE(t)) / SSE(t – 1) < T
             */
            double improvement = (previousSse - iterationSse) / previousSse;
            if (improvement < convergence) {
                break;
            }

            /* update this for the next iteration */
            previousSse = iterationSse;

            /*
             * Now that all the points have been grouped to the cluster with
             * the nearest centroid, we can update the clusters' centroids
             * accordingly. We simply get the mean of the points and then set
             * that as the centroid.
             */
            for (PointCluster cluster : clusters) {
                DataPoint mean = cluster.getMean(true);
                cluster.setCentroid(mean);
            }

            numIterations += 1;
        }

        return new KMeansRun(runNum, iterations, initialSse, previousSse,
                numIterations, clusters);
    }

    /**
     * Performs a run of the K-means algorithm.
     *
     * @param pointFile     the point file to read from.
     * @param method        the initialization method to use.
     * @param numClusters   the number of clusters to create.
     * @param maxIterations the max number of iterations for the run.
     * @param convergence   the convergence threshold. This method will
     *                      stop before reaching the maximum number of
     *                      iterations if the improvement from the last
     *                      iteration is less than this value.
     * @param rcomn         when {@code true}, if there are multiple nearest
     *                      centroids to a given point, the program chooses a
     *                      random one rather than the first one it finds.
     * @return the result of this run.
     * @throws NullPointerException if {@code pointFile} or {@code method}
     *                              are {@code null}.
     */
    public static KMeansRun performRun(
            DataPointFile pointFile,
            KMeansInitMethod method,
            int numClusters,
            int maxIterations,
            double convergence,
            boolean rcomn) {
        return performRun(pointFile, method, 0,
                numClusters, maxIterations, convergence, rcomn);
    }

    /**
     * Performs multiple runs of the K-means algorithm and returns the
     * final result.
     *
     * @param pointFile     the point file to read from.
     * @param method        the initialization method to use.
     * @param numClusters   the number of clusters to create.
     * @param maxIterations the max number of iterations for the run.
     * @param convergence   the convergence threshold. This method will
     *                      stop before reaching the maximum number of
     *                      iterations if the improvement from the last
     * @param numRuns       the number of runs to perform.
     *                      iteration is less than this value.
     * @param rcomn         when {@code true}, if there are multiple nearest
     *                      centroids to a given point, the program chooses a
     *                      random one rather than the first one it finds.
     * @return the results of each run.
     * @throws NullPointerException if {@code pointFile} or {@code method}
     *                              are {@code null}.
     */
    public static KMeansRuns performRuns(
            DataPointFile pointFile,
            KMeansInitMethod method,
            int numClusters,
            int maxIterations,
            double convergence,
            int numRuns,
            boolean rcomn) {
        ArrayList<KMeansRun> runs = new ArrayList<>();
        for (int i = 0; i < numRuns; i++) {
            runs.add(performRun(pointFile, method, i,
                    numClusters, maxIterations, convergence, rcomn));
        }
        return new KMeansRuns(runs);
    }

    /**
     * Performs runs of the K-means algorithm based on the given program
     * arguments.
     *
     * @param pointFile the point file to read from.
     * @param args      the program arguments.
     * @return the results of each run.
     * @throws NullPointerException if {@code pointFile}, {@code method},
     *                              or {@code args} are {@code null}.
     */
    public static KMeansRuns perform(
            DataPointFile pointFile,
            ClusteringArgs args) {
        Objects.requireNonNull(args, "args cannot be null");
        return performRuns(pointFile, args.initMethod,
                args.numClusters, args.maxIterations,
                args.convergenceThreshold, args.numRuns,
                args.chooseRandomCentroidOnMultipleNearest);
    }

}
