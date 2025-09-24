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

import io.whirvis.edu.clustering.DiameterMethod;
import io.whirvis.edu.clustering.LinkageMethod;
import io.whirvis.edu.clustering.NormalizationType;
import io.whirvis.edu.clustering.cli.args.ParamException;
import io.whirvis.edu.clustering.cli.args.ProgramArgs;
import io.whirvis.edu.clustering.kmeans.KMeansInitMethod;

import java.io.File;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Container for the arguments of {@link ClusteringProgram}.
 *
 * @see #get(PrintStream, String[])
 * @see ClusteringParams
 */
public final class ClusteringArgs {

    /**
     * Argument for {@link ClusteringParams#DATA_FILE}.
     */
    public final File dataInputFile;

    /**
     * Argument for {@link ClusteringParams#NUM_CLUSTERS}.
     */
    public final int numClusters;

    /**
     * Argument for {@link ClusteringParams#MAX_ITERATIONS}.
     */
    public final int maxIterations;

    /**
     * Argument for {@link ClusteringParams#CONVERGENCE_THRESHOLD}.
     */
    public final double convergenceThreshold;

    /**
     * Argument for {@link ClusteringParams#NUM_RUNS}.
     */
    public final int numRuns;

    /**
     * Argument for {@link ClusteringParams#OUTPUT_MODE}.
     */
    public final OutputMode outputMode;

    /**
     * Argument for {@link ClusteringParams#K_MEANS_INIT_METHOD}.
     */
    public final KMeansInitMethod initMethod;

    /**
     * Argument for {@link ClusteringParams#NORMALIZATION_TYPE}.
     */
    public final NormalizationType normalizationType;

    /**
     * Argument for {@link ClusteringParams#VALIDATOR_FORMULA}.
     */
    public final ClusterValidatorFormula validatorFormula;

    /**
     * Argument for {@link ClusteringParams#DIAMETER_METHOD}.
     */
    public final DiameterMethod diameterMethod;

    /**
     * Argument for {@link ClusteringParams#LINKAGE_METHOD}.
     */
    public final LinkageMethod linkageMethod;

    /**
     * Argument for {@link ClusteringParams#CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST}.
     */
    public final boolean chooseRandomCentroidOnMultipleNearest;

    /**
     * Argument for {@link ClusteringParams#OUTPUT_DESTINATION}.
     */
    public final String outputDestination;

    private ClusteringArgs(ProgramArgs args) {
        this.dataInputFile = args.get(ClusteringParams.DATA_FILE);
        this.numClusters = args.get(ClusteringParams.NUM_CLUSTERS);
        this.maxIterations = args.get(ClusteringParams.MAX_ITERATIONS);
        this.convergenceThreshold = args.get(ClusteringParams.CONVERGENCE_THRESHOLD);
        this.numRuns = args.get(ClusteringParams.NUM_RUNS);
        this.outputMode = args.get(ClusteringParams.OUTPUT_MODE);
        this.initMethod = args.get(ClusteringParams.K_MEANS_INIT_METHOD);
        this.normalizationType = args.get(ClusteringParams.NORMALIZATION_TYPE);
        this.validatorFormula = args.get(ClusteringParams.VALIDATOR_FORMULA);
        this.diameterMethod = args.get(ClusteringParams.DIAMETER_METHOD);

        /* optional argument */
        if (args.getParamCount() >= 11) {
            this.linkageMethod = args.get(
                    ClusteringParams.LINKAGE_METHOD);
        } else {
            this.linkageMethod = null;
        }

        /* optional argument */
        if (args.getParamCount() >= 12) {
            this.chooseRandomCentroidOnMultipleNearest = args.get(
                    ClusteringParams.CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST);
        } else {
            this.chooseRandomCentroidOnMultipleNearest = false;
        }

        /* optional argument */
        if (args.getParamCount() >= 13) {
            this.outputDestination = args.get(
                    ClusteringParams.OUTPUT_DESTINATION);
        } else {
            this.outputDestination = null;
        }
    }

    /**
     * Parses the given arguments into a {@link ClusteringArgs} instance.
     * <p>
     * If parsing fails, an error message will be printed to the console and
     * a value of {@code null} will be returned.
     *
     * @param err      where to send error messages to.
     * @param javaArgs the arguments to parse.
     * @return the parsed arguments, {@code null} if parsing failed.
     * @throws NullPointerException if {@code err} or {@code javaArgs}
     *                              are {@code null}.
     */
    public static ClusteringArgs get(
            PrintStream err,
            String[] javaArgs) {
        Objects.requireNonNull(err, "err cannot be null");
        Objects.requireNonNull(javaArgs, "javaArgs cannot be null");

        ProgramArgs args = new ProgramArgs();
        args.add(0, ClusteringParams.DATA_FILE);
        args.add(1, ClusteringParams.NUM_CLUSTERS);
        args.add(2, ClusteringParams.MAX_ITERATIONS);
        args.add(3, ClusteringParams.CONVERGENCE_THRESHOLD);
        args.add(4, ClusteringParams.NUM_RUNS);
        args.add(5, ClusteringParams.OUTPUT_MODE);
        args.add(6, ClusteringParams.K_MEANS_INIT_METHOD);
        args.add(7, ClusteringParams.NORMALIZATION_TYPE);
        args.add(8, ClusteringParams.VALIDATOR_FORMULA);
        args.add(9, ClusteringParams.DIAMETER_METHOD);

        if (javaArgs.length < args.getParamCount()) {
            err.println("Usage: " + args.getUsage());
            return null; /* not enough arguments to parse */
        }

        /* optional argument */
        if (javaArgs.length >= args.getParamCount() + 1) {
            args.add(10, ClusteringParams.LINKAGE_METHOD);
        }

        /* optional argument */
        if (javaArgs.length >= args.getParamCount() + 1) {
            args.add(11, ClusteringParams.CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST);
        }

        /* optional argument */
        if (javaArgs.length >= args.getParamCount() + 1) {
            args.add(12, ClusteringParams.OUTPUT_DESTINATION);
        }

        try {
            args.parse(javaArgs);
        } catch (ParamException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                ClusteringProgram.printFancyStackTrace(err, cause);
            }
            err.println("Error: " + e.getMessage());
            err.println("Parameter: " + e.getCulpritStr());
            return null; /* failed to parse arguments */
        }

        return new ClusteringArgs(args);
    }

}
