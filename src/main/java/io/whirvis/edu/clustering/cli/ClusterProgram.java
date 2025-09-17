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
import io.whirvis.edu.clustering.kmeans.KMeans;
import io.whirvis.edu.clustering.kmeans.KMeansInitMethod;
import io.whirvis.edu.clustering.kmeans.KMeansRun;
import io.whirvis.edu.clustering.kmeans.KMeansRuns;
import io.whirvis.edu.clustering.validator.ClusterValidator;
import io.whirvis.edu.clustering.validator.ClusterValidatorType;
import io.whirvis.edu.clustering.validator.ExternalClusterValidator;
import io.whirvis.edu.clustering.validator.InternalClusterValidator;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * The main class for the cluster program.
 *
 * @see #main(String[])
 */
public final class ClusterProgram {

    private ClusterProgram() {
        /* utility class */
    }

    /**
     * The formatting to use for sum-of-squared error values.
     */
    /* package-private */
    static final DecimalFormat SSE_FORMAT = new DecimalFormat(
            "0.0000", DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * Prints out a stack trace to the specified stream with line separators
     * to distinguish it from the output that comes before and after it.
     *
     * @param throwable the stack trace to print.
     * @param out       the stream to write to.
     */
    @SuppressWarnings("SameParameterValue")
    /* package-private */
    static void printFancyStackTrace(
            Throwable throwable, PrintStream out) {
        out.println("- [ STACK TRACE ] ----------------------");
        throwable.printStackTrace(out);
        out.println("----------------------------------------");
    }

    private static void printHumanReadableResults(
            DataPointFile pointFile,
            NormalizationType normalizationType,
            KMeansInitMethod initMethod,
            ClusterValidator validator,
            KMeansRuns runs) {
        if (validator.getType() == ClusterValidatorType.EXTERNAL
                && !pointFile.areTrueClustersKnown()) {
            String msg = "Cannot calculate external cluster index"
                    + " without knowing the true clusters";
            throw new IllegalStateException(msg);
        }

        for (KMeansRun run : runs) {
            System.out.println("Run " + (run.runNum + 1));
            System.out.println("-----");

            for (int i = 0; i < run.numIterations; i++) {
                System.out.print("Iteration " + (i + 1) + ": SSE = ");
                System.out.println(SSE_FORMAT.format(run.iterations[i]));
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
                String msg = "Unexpected cluster type " + validator.getType();
                throw new RuntimeException(msg);
            }

            System.out.print(validator.getAbbreviation());
            System.out.print("(" + clusters.getClusterCount() + ") = ");
            System.out.println(SSE_FORMAT.format(index));

            System.out.println(); /* separator line */
        }

        System.out.println("Additional Notes");
        System.out.println("-----");
        System.out.println("Normalized with:  " + normalizationType);
        System.out.println("Initialized with: " + initMethod);
        System.out.println("Using validator: " + validator.getName());
        System.out.println("-----");
    }

    @SuppressWarnings("ConstantConditions")
    public static void main(String[] javaArgs) throws InterruptedException {
        ClusterProgramArgs args = ClusterProgramArgs.get(javaArgs);
        if (args == null) {
            return; /* argument parsing failed */
        }

        DataPointFile pointFile;
        try {
            pointFile = DataPointFile.open(args.dataInputFile, true);
        } catch (IOException e) {
            printFancyStackTrace(e, System.err);
            System.err.println("Error: failed to load data point file.");
            return;
        }

        /*
         * The probably isn't an issue but let the user know about it
         * anyway just in case it was a mistake on their end.
         */
        if (pointFile.areTrueClustersKnown()
                && pointFile.getTrueClusterCount() != args.numClusters) {
            System.out.print("Notice: Supplied cluster count");
            System.out.print(" (" + args.numClusters + ") does not match");
            System.out.print(" the true cluster count of the file");
            System.out.print(" (" + pointFile.getTrueClusterCount() + ").");
            System.out.println(); /* separator */

            Thread.sleep(3000);
            System.out.println("Continuing...");
            Thread.sleep(3000);
        }

        /* TODO: assign non-null values to these variables */
        OutputMode outputMode = null;
        NormalizationType normalizationType = null;
        KMeansInitMethod initMethod = null;
        ClusterValidator validator = null;

        if (outputMode == null) {
            System.err.println("Fatal: Choose an output mode.");
            System.exit(1);
        } else if (normalizationType == null) {
            System.err.println("Fatal: Choose a normalization type.");
            System.exit(1);
        } else if (initMethod == null) {
            System.err.println("Fatal: Choose an initialization method.");
            System.exit(1);
        } else if (validator == null) {
            System.err.println("Fatal: Instantiate a cluster validator.");
            System.exit(1);
        }

        pointFile.normalize(normalizationType);

        KMeansRuns runs = KMeans.perform(pointFile, initMethod, args);
        if (outputMode == OutputMode.HUMAN_READABLE) {
            printHumanReadableResults(pointFile,
                    normalizationType, initMethod, validator, runs);
        } else if (outputMode == null) {
            System.err.println("No output mode specified.");
            System.exit(1);
        } else {
            System.err.println("Unknown output mode.");
            System.exit(1);
        }
    }

}
