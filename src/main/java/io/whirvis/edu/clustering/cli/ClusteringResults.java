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
package io.whirvis.edu.clustering.cli;

import io.whirvis.edu.clustering.DataPointFile;
import io.whirvis.edu.clustering.NormalizationType;
import io.whirvis.edu.clustering.PointClusters;
import io.whirvis.edu.clustering.kmeans.KMeansInitMethod;
import io.whirvis.edu.clustering.kmeans.KMeansRun;
import io.whirvis.edu.clustering.kmeans.KMeansRuns;
import io.whirvis.edu.clustering.validator.ClusterValidator;
import io.whirvis.edu.clustering.validator.ClusterValidatorType;
import io.whirvis.edu.clustering.validator.ExternalClusterValidator;
import io.whirvis.edu.clustering.validator.InternalClusterValidator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contains the results of running {@link ClusteringProgram}.
 *
 * @see #print(OutputStream, OutputMode)
 */
public final class ClusteringResults {

    private static final Lock SSE_FORMAT_LOCK = new ReentrantLock();
    private static final DecimalFormat SSE_FORMAT = new DecimalFormat(
            "0.0000", DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * The data point file.
     */
    public final DataPointFile pointFile;

    /**
     * The normalization type.
     */
    public final NormalizationType normalizationType;

    /**
     * The cluster validator.
     */
    public final ClusterValidator validator;

    /**
     * The K-means runs.
     */
    public final KMeansRuns runs;

    /**
     * The K-means initialization method.
     */
    public final KMeansInitMethod initMethod;

    /**
     * Constructs a new {@code ClusteringResults}.
     *
     * @param pointFile         the data point file.
     * @param normalizationType the normalization type.
     * @param validator         the cluster validator.
     * @param runs              the K-means runs.
     * @param initMethod        the K-means initialization method.
     * @throws NullPointerException if {@code pointFile},
     *                              {@code normalizationType},
     *                              {@code validator}, {@code runs} or
     *                              {@code initMethod} are {@code null}.
     */
    public ClusteringResults(
            DataPointFile pointFile,
            NormalizationType normalizationType,
            ClusterValidator validator,
            KMeansRuns runs,
            KMeansInitMethod initMethod) {
        Objects.requireNonNull(pointFile,
                "pointFile cannot be null");
        Objects.requireNonNull(normalizationType,
                "normalizationType cannot be null");
        Objects.requireNonNull(validator,
                "validator cannot be null");
        Objects.requireNonNull(initMethod,
                "initMethod cannot be null");
        Objects.requireNonNull(runs,
                "runs cannot be null");

        this.pointFile = pointFile;
        this.normalizationType = normalizationType;
        this.validator = validator;
        this.initMethod = initMethod;
        this.runs = runs;
    }

    /* TODO: implement */
    @SuppressWarnings("unused")
    private void printCsvResults(OutputStream out) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void printHumanReadableResults(KMeansRun run, PrintStream ps) {
        ps.println("Run " + (run.runNum + 1));
        ps.println("-----");

        for (int i = 0; i < run.numIterations; i++) {
            ps.println("Iteration " + (i + 1) + ": SSE = "
                    + formatSse(run.iterations[i]));
        }

        PointClusters clusters = run.clusters;

        double index;
        if (validator.getType() == ClusterValidatorType.INTERNAL) {
            InternalClusterValidator internalValidator =
                    (InternalClusterValidator) validator;
            index = internalValidator.calculateIndex(clusters);
        } else if (validator.getType() == ClusterValidatorType.EXTERNAL) {
            ExternalClusterValidator externalValidator =
                    (ExternalClusterValidator) validator;
            PointClusters truth = pointFile.getTrueClusters();
            index = externalValidator.calculateIndex(truth, clusters);
        } else {
            String msg = "Unexpected branch, this is a bug";
            throw new UnsupportedOperationException(msg);
        }

        ps.println(validator.getAbbreviation()
                + " (" + clusters.getClusterCount() + ") = "
                + formatSse(index));

        ps.println(); /* separator line */
    }

    private void printHumanReadableResults(OutputStream out) {
        if (validator.getType() == ClusterValidatorType.EXTERNAL
                && !pointFile.areTrueClustersKnown()) {
            throw new IllegalStateException("Cannot calculate the"
                    + " external cluster index without knowing the true"
                    + " number of clusters");
        }

        PrintStream ps = new PrintStream(out);

        for (KMeansRun run : runs) {
            printHumanReadableResults(run, ps);
        }

        ps.println("Additional Notes\n"
                + "-----\n"
                + "Normalized with:  " + normalizationType + "\n"
                + "Initialized with: " + initMethod + "\n"
                + "Using validator:  " + validator.getName() + "\n"
                + "-----");
    }

    /**
     * Prints the results of the clustering program.
     *
     * @param out  where to write the output.
     * @param mode how the output should be formatted.
     * @throws NullPointerException If {@code out} or {@code outputMode}
     *                              are {@code null}.
     */
    public void print(OutputStream out, OutputMode mode) {
        Objects.requireNonNull(out, "out cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
        switch (mode) {
            case CSV:
                printCsvResults(out);
                break;
            case HUMAN_READABLE:
                printHumanReadableResults(out);
                break;
            default:
                String msg = "Unexpected case, this is a bug";
                throw new UnsupportedOperationException(msg);
        }
    }

    private static String formatSse(double sse) {
        SSE_FORMAT_LOCK.lock();
        try {
            return SSE_FORMAT.format(sse);
        } finally {
            SSE_FORMAT_LOCK.unlock();
        }
    }

}
