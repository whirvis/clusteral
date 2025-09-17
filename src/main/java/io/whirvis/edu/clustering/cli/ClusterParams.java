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

import io.whirvis.edu.clustering.cli.args.*;

/**
 * Parameters used by the clustering program.
 * <p>
 * The parameters are defined according to the requirements specified in
 * the document for Phase 1.
 * <p>
 * <b>Input:</b> Your program should be <u>non-interactive</u> (that is, the
 * program should <u>not</u> interact with the user by asking them explicit
 * questions) and take the following <u>command-line</u> arguments: &lt;F&gt;
 * &lt;K&gt; &lt;I&gt; &lt;T&gt; &lt;R&gt;, where
 * <p>
 * <ul>
 *  <li><i>F</i>: name of the data file</li>
 *  <li><i>K</i>: number of clusters (<u>positive</u> integer greater than one)</li>
 *  <li><i>I</i>: maximum number of iterations (<u>positive</u> integer)</li>
 *  <li><i>T</i>: convergence threshold (<u>non-negative</u> real)</li>
 *  <li><i>R</i>: number of runs (<u>positive</u> integer)</li>
 * </ul>
 *
 * @see ClusterProgram
 */
/* package-private */
final class ClusterParams {

    private ClusterParams() {
        /* utility class */
    }

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
                if (i < 1) {
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
                if (i <= 0) {
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
                if (d < 0) {
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
                if (i <= 0) {
                    String msg = "Number of runs must be positive";
                    throw new ParamException(msg);
                }
            });

    /**
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

}
