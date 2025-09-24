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
import io.whirvis.edu.clustering.kmeans.KMeans;
import io.whirvis.edu.clustering.kmeans.KMeansRuns;
import io.whirvis.edu.clustering.validator.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * The main class for the cluster program.
 *
 * @see #main(String[])
 */
public final class ClusteringProgram {

    /* package-private */
    static void printFancyStackTrace(
            PrintStream err,
            Throwable throwable) {
        err.println("- [ STACK TRACE ] ----------------------");
        throwable.printStackTrace(err);
        err.println("----------------------------------------");
    }

    private static DataPointFile loadPointFile(
            PrintStream err,
            ClusteringArgs args) {
        DataPointFile pointFile;
        try {
            pointFile = DataPointFile.open(args.dataInputFile, true);
            pointFile.normalize(args.normalizationType);
        } catch (IOException e) {
            printFancyStackTrace(err, e);
            err.println("Error: Failed to load data point file.");
            return null;
        }
        return pointFile;
    }

    private static DunnIndexCalculator getDunnIndexCalculator(
            PrintStream err,
            ClusteringArgs args) {
        if (args.linkageMethod == null) {
            err.println("Error: Linkage method must be defined"
                    + " when using Dunn-Index");
            return null;
        }
        return new DunnIndexCalculator(args.linkageMethod, args.diameterMethod);
    }

    private static ClusterValidator getClusterValidator(
            PrintStream err,
            ClusteringArgs args) {
        switch (args.validatorFormula) {
            case CALINSKI_HARABASZ:
                return new CalinskiHarabaszCalculator();
            case DAVIES_BOULDIN:
                return new DaviesBouldinCalculator();
            case DUNN_INDEX:
                return getDunnIndexCalculator(err, args);
            case FOWLKES_MALLOWS:
                return new FowlkesMallowsCalculator();
            case JACCARD_COEFFICIENT:
                return new JaccardCoefficientCalculator();
            case RAND_STATISTIC:
                return new RandStatisticCalculator();
            case SILHOUETTE_WIDTH:
                return new SilhouetteWidthCalculator();
            default:
                String msg = "Unexpected case, this is a bug";
                throw new UnsupportedOperationException(msg);
        }
    }

    private static OutputStream getOutputDestination(
            PrintStream stdout,
            PrintStream stderr,
            ClusteringArgs args) {
        String name = args.outputDestination;
        if (name == null) {
            return stdout; /* default to standard out */
        }

        String lowercase = name.toLowerCase(Locale.ROOT);
        switch (lowercase) {
            case "0":
            case "stdout":
                return stdout;
            case "1":
            case "stderr":
                return stderr;
            case "2":
            case "stdin":
                stderr.println("Error: Cannot write" +
                        " program output to input stream");
                return null;
            default:
                break; /* fallthrough */
        }

        try {
            return Files.newOutputStream(Paths.get(name));
        } catch (IOException e) {
            printFancyStackTrace(stderr, e);
            stderr.println("Error: Failed to open output file.");
            return null;
        }
    }

    private static void warnOnClusterCountMismatch(
            PrintStream err,
            DataPointFile pointFile,
            ClusteringArgs args) throws InterruptedException {
        if (!pointFile.areTrueClustersKnown()) {
            return; /* no way to check for mismatch */
        } else if (pointFile.getTrueClusterCount() == args.numClusters) {
            return; /* data consistent, no warning necessary */
        }

        err.println("Notice: Supplied cluster count" +
                " (" + args.numClusters + ") does not match" +
                " the true cluster count of the file" +
                " (" + pointFile.getTrueClusterCount() + ").");
        err.println(); /* separator */

        Thread.sleep(3000);
        err.println("Continuing...");
        Thread.sleep(3000);
    }

    public static void main(String[] javaArgs) throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;

        ClusteringArgs args = ClusteringArgs.get(stderr, javaArgs);
        if (args == null) {
            return; /* argument parsing failed */
        }

        DataPointFile pointFile = loadPointFile(stderr, args);
        if (pointFile == null) {
            return; /* file loading failed */
        }

        ClusterValidator validator = getClusterValidator(stderr, args);
        if (validator == null) {
            return; /* validator instantiation failed */
        }

        OutputStream out = getOutputDestination(stdout, stderr, args);
        if (out == null) {
            return; /* failed to get output destination */
        }

        warnOnClusterCountMismatch(stderr, pointFile, args);

        KMeansRuns runs = KMeans.performRuns(pointFile,
                args.initMethod, args.numClusters, args.maxIterations,
                args.convergenceThreshold, args.numRuns,
                args.chooseRandomCentroidOnMultipleNearest);

        ClusteringResults results = new ClusteringResults(pointFile,
                args.normalizationType, validator, runs, args.initMethod);

        results.print(out);
        if (out != stdout && out != stderr) {
            out.close();
        }
    }

    private ClusteringProgram() {
        /* utility class */
    }

}
