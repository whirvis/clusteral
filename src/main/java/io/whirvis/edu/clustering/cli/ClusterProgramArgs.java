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

import io.whirvis.edu.clustering.cli.args.ParamException;
import io.whirvis.edu.clustering.cli.args.ProgramArgs;

import java.io.File;
import java.util.Objects;

/**
 * Container for the arguments of {@link ClusterProgram}.
 *
 * @see #get(String[])
 * @see ClusterParams
 */
public final class ClusterProgramArgs {

    /**
     * Argument for {@link ClusterParams#DATA_FILE}.
     */
    public final File dataInputFile;

    /**
     * Argument for {@link ClusterParams#NUM_CLUSTERS}.
     */
    public final int numClusters;

    /**
     * Argument for {@link ClusterParams#MAX_ITERATIONS}.
     */
    public final int maxIterations;

    /**
     * Argument for {@link ClusterParams#CONVERGENCE_THRESHOLD}.
     */
    public final double convergenceThreshold;

    /**
     * Argument for {@link ClusterParams#NUM_RUNS}.
     */
    public final int numRuns;

    /**
     * Argument for {@link ClusterParams#CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST}.
     */
    public final boolean chooseRandomCentroidOnMultipleNearest;

    private ClusterProgramArgs(ProgramArgs args) {
        this.dataInputFile = args.get(ClusterParams.DATA_FILE);
        this.numClusters = args.get(ClusterParams.NUM_CLUSTERS);
        this.maxIterations = args.get(ClusterParams.MAX_ITERATIONS);
        this.convergenceThreshold = args.get(ClusterParams.CONVERGENCE_THRESHOLD);
        this.numRuns = args.get(ClusterParams.NUM_RUNS);

        /* optional argument */
        if (args.getParamCount() >= 6) {
            this.chooseRandomCentroidOnMultipleNearest = args.get(
                    ClusterParams.CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST);
        } else {
            this.chooseRandomCentroidOnMultipleNearest = false;
        }
    }

    /**
     * Parses the given arguments into a {@link ClusterProgramArgs} instance.
     * <p>
     * If parsing fails, an error message will be printed to the console and
     * a value of {@code null} will be returned.
     *
     * @param javaArgs the arguments to parse.
     * @return the parsed arguments, {@code null} if parsing failed.
     */
    public static ClusterProgramArgs get(String[] javaArgs) {
        Objects.requireNonNull(javaArgs, "javaArgs cannot be null");

        ProgramArgs args = new ProgramArgs();
        args.add(0, ClusterParams.DATA_FILE);
        args.add(1, ClusterParams.NUM_CLUSTERS);
        args.add(2, ClusterParams.MAX_ITERATIONS);
        args.add(3, ClusterParams.CONVERGENCE_THRESHOLD);
        args.add(4, ClusterParams.NUM_RUNS);

        if (javaArgs.length < args.getParamCount()) {
            System.err.println("Usage: " + args.getUsage());
            return null; /* not enough arguments to parse */
        }

        /* optional argument */
        if (javaArgs.length >= args.getParamCount() + 1) {
            args.add(5, ClusterParams.CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST);
        }

        try {
            args.parse(javaArgs);
        } catch (ParamException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                ClusterProgram.printFancyStackTrace(cause, System.err);
            }
            System.err.println("Error: " + e.getMessage());
            System.err.println("Parameter: " + e.getCulpritStr());
            return null; /* failed to parse arguments */
        }

        return new ClusterProgramArgs(args);
    }

}
