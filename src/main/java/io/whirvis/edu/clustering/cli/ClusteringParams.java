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
import io.whirvis.edu.clustering.cli.args.*;
import io.whirvis.edu.clustering.kmeans.KMeansInitMethod;

/**
 * Parameters used by the clustering program.
 * <p>
 * The parameters are defined according to the requirements specified in
 * the document for Phase 1.
 * <p>
 * <b>Input:</b> Your program should be <u>non-interactive</u> (that is, the
 * program should <u>not</u> interact with the user by asking them explicit
 * questions) and take the following <u>command-line</u> arguments: &lt;F&gt;
 * &lt;K&gt; &lt;I&gt; &lt;T&gt; &lt;R&gt;, &lt;O&gt;, &lt;M&gt;, &lt;N&gt;,
 * &lt;D&gt;, &lt;W&gt;, where
 * <p>
 * <ul>
 *  <li><i>F</i>: name of the data file</li>
 *  <li><i>K</i>: number of clusters (<u>positive</u> integer greater than one)</li>
 *  <li><i>I</i>: maximum number of iterations (<u>positive</u> integer)</li>
 *  <li><i>T</i>: convergence threshold (<u>non-negative</u> real)</li>
 *  <li><i>R</i>: number of runs (<u>positive</u> integer)</li>
 *  <li><i>O</i>: output mode (human readable or CSV)</li>
 *  <li><i>M</i>: K-means initialization method</li>
 *  <li><i>N</i>: normalization type</li>
 *  <li><i>V</i>: name of the cluster validator</li>
 *  <li><i>D</i>: diameter calculation method</li>
 *  <li><i>L</i>: linkage method, if necessary (optional)</li>
 *  <li><i>C</i>: random centroid on multiple nearest (optional)</li>
 *  <li><i>W</i>: where to write program output (optional)</li>
 * </ul>
 *
 * @see ClusteringArgs
 * @see ClusteringProgram
 */
/* package-private */
final class ClusteringParams {

    private static final int MIN_NUM_CLUSTERS = 1;
    private static final int MIN_MAX_ITERATIONS = 1;
    private static final double MIN_CONVERGENCE_THRESHOLD = 0.0d;
    private static final int MIN_NUM_RUNS = 1;

    /**
     * The parameter for &lt;F&gt;, as described in the document.
     */
    /* package-private */
    static final FileParam DATA_FILE =
            new FileParam("data file", true);

    /**
     * The parameter for &lt;K&gt;, as described in the document.
     */
    /* package-private */
    static final IntParam NUM_CLUSTERS =
            new IntParam("number of clusters", i -> {
                if (i < MIN_NUM_CLUSTERS) {
                    String msg = "Number of clusters must be greater than one";
                    throw new ParamException(msg);
                }
            });

    /**
     * The parameter for &lt;I&gt;, as described in the document.
     */
    /* package-private */
    static final IntParam MAX_ITERATIONS =
            new IntParam("max iterations", i -> {
                if (i < MIN_MAX_ITERATIONS) {
                    String msg = "Max iterations must be positive";
                    throw new ParamException(msg);
                }
            });

    /**
     * The parameter for &lt;T&gt;, as described in the document.
     */
    /* package-private */
    static final DoubleParam CONVERGENCE_THRESHOLD =
            new DoubleParam("convergence threshold", d -> {
                if (d < MIN_CONVERGENCE_THRESHOLD) {
                    String msg = "Convergence threshold must be non-negative";
                    throw new ParamException(msg);
                }
            });

    /**
     * The parameter for &lt;R&gt;, as described in the document.
     */
    /* package-private */
    static final IntParam NUM_RUNS =
            new IntParam("number of runs", i -> {
                if (i < MIN_NUM_RUNS) {
                    String msg = "Number of runs must be positive";
                    throw new ParamException(msg);
                }
            });

    /**
     * The parameter for &lt;O&gt;, as described in the document.
     */
    /* package-private */
    static final EnumParam<OutputMode> OUTPUT_MODE =
            new EnumParam<>("output mode",
                    OutputMode.class, false);

    /**
     * The parameter for &lt;M&gt;, as described in the document.
     */
    /* package-private */
    static final EnumParam<KMeansInitMethod> K_MEANS_INIT_METHOD =
            new EnumParam<>("k-means initialization method",
                    KMeansInitMethod.class, false);

    /**
     * The parameter for &lt;N&gt;, as described in the document.
     */
    /* package-private */
    static final EnumParam<NormalizationType> NORMALIZATION_TYPE =
            new EnumParam<>("normalization type",
                    NormalizationType.class, false);

    /**
     * The parameter for &lt;V&gt;, as described in the document.
     */
    /* package-private */
    static final ClusterValidatorParam VALIDATOR_FORMULA =
            new ClusterValidatorParam("cluster validator");

    /**
     * The parameter for &lt;D&gt;, as described in the document.
     */
    /* package-private */
    static final EnumParam<DiameterMethod> DIAMETER_METHOD =
            new EnumParam<>("diameter calculation method",
                    DiameterMethod.class, false);

    /**
     * The parameter for &lt;L&gt;, as described in the document.
     */
    /* package-private */
    static final EnumParam<LinkageMethod> LINKAGE_METHOD =
            new EnumParam<>("linkage method",
                    LinkageMethod.class, true);

    /**
     * The parameter for &lt;L&gt;, as described in the document.
     * <p>
     * An extra parameter described as a requirement for graduate students
     * in Phase 2:
     * <blockquote>
     * "Consider the case where a data point has more than one nearest center.
     * Most implementations assign such a point to the nearest center with the
     * smallest index. Why do you think such a scheme has become a convention
     * in the literature? If, instead, you assign such a point to a nearest
     * center selected uniformly at random, does the algorithm still converge?
     * If not, why not? <b>Add this random assignment strategy to your program
     * as an option</b> and explain its theoretical convergence behavior in a
     * comment at the top of your main source file."
     * </blockquote>
     */
    /* package-private */
    static final BooleanParam CHOOSE_RANDOM_CENTROID_ON_MULTIPLE_NEAREST =
            new BooleanParam("choose random centroid on multiple nearest");

    /**
     * The parameter for &lt;W&gt;, as described in the document.
     */
    /* package-private */
    static final StringParam OUTPUT_DESTINATION =
            new StringParam("output destination");

    private ClusteringParams() {
        /* utility class */
    }

}
