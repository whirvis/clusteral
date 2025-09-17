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
package io.whirvis.edu.clustering.validator;

import io.whirvis.edu.clustering.DataPoint;
import io.whirvis.edu.clustering.PointClusters;
import io.whirvis.edu.clustering.UnorderedPair;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Calculator and container for the unordered pairs, true positives,
 * true negatives, false positives, and false negatives of a ground truth
 * and generated point clusters.
 *
 * @see RandStatisticCalculator
 * @see JaccardCoefficientCalculator
 * @see FowlkesMallowsCalculator
 */
public final class ClusterTruthTable {

    /**
     * All unordered pairs in the point cluster.
     */
    public final List<UnorderedPair> pairs;

    /**
     * The number of true positives in the truth table.
     * <p>
     * From page 435 of the Data Mining book given in the document for
     * Phase 5:
     * <blockquote>
     * "<i>True Positives:</i> {@code xi} and {@code xj} belong to the
     * same partition in <i>T</i>, and they are also in the same cluster
     * in <i>C</i>. This is a true positive pair because the positive event,
     * {@code Yi=Yj}, corresponds to to the ground truth, {@code yi=yj}."
     * </blockquote>
     * <p>
     * That being, a given pair of points are in the same clusters in both
     * the generated clusters and the ground truth (the clusters they truly
     * belong to).
     */
    public final double truePositives;

    /**
     * The number of true negatives in the truth table.
     * <p>
     * From page 435 of the Data Mining book given in the document for
     * Phase 5:
     * <blockquote>
     * "<i>True Negatives:</i> {@code xi} and {@code xj} neither belong to
     * the same partition in <i>T</i>, nor do they belong to the same cluster
     * in <i>C</i>. This pair is thus a true negative, that is, {@code Yi!=Yj}
     * and {@code yi!=yj.}
     * </blockquote>
     * That being, a given pair of points are in different clusters in both
     * the generated clusters and the ground truth (the clusters they truly
     * belong to).
     */
    public final double trueNegatives;

    /**
     * The number of false positives in the truth table.
     * <p>
     * From page 435 of the Data Mining book given in the document for
     * Phase 5:
     * <blockquote>
     * "<i>False Positives:</i> {@code xi} and {@code xj} do not belong to
     * the same partition in <i>T</i>, but they do belong to the same cluster
     * in <i>C</i>. This pair is a false positive because the positive event,
     * {@code Yi=Yj},is actually false, that is, it does not agree with the
     * ground-truth partitioning, which indicates that {@code yi=yj}."
     * </blockquote>
     * That being, a given pair of points are in the same clusters in the
     * generated clusters but not in the ground truth (the clusters they truly
     * belong to).
     */
    public final double falsePositives;

    /**
     * The number of false negatives in the truth table.
     * <p>
     * From page 435 of the Data Mining book given in the document for
     * Phase 5:
     * <blockquote>
     * "<i>False Negatives:</i> {@code xi} and {@code xj} belong to the
     * same partition in <i>T</i>, but they do not belong to the same cluster
     * in <i>C</i>. That is, the negative event, {@code Yi!=Yj}, does not
     * correspond to the truth, {@code yi=yj}."
     * </blockquote>
     * That being, a given pair of points are not in the same clusters in the
     * generated clusters but are in the ground truth (the clusters they truly
     * belong to).
     */
    public final double falseNegatives;

    /**
     * Aliases for the longer field names in this class.
     * <ul>
     *     <li>{@code tp}: alias for {@code truePositives}.</li>
     *     <li>{@code tn}: alias for {@code trueNegatives}.</li>
     *     <li>{@code fp}: alias for {@code falsePositives}.</li>
     *     <li>{@code fn}: alias for {@code falseNegatives}.</li>
     * </ul>
     */
    public final double tp, tn, fp, fn;

    /**
     * Constructs a new {@code ClusterTruthTable}.
     * <p>
     * This constructor invokes {@link PointClusters#getUnorderedPointPairs()}
     * on the argument given for {@code truth}. Depending on if the method was
     * already previously invoked, this constructor will be {@code O(n^2)} or
     * {@code O(n)}.
     *
     * @param truth     the true point clusters.
     * @param generated the generated point clusters.
     * @throws NullPointerException if {@code truth} or {@code generated}
     *                              are {@code null}.
     */
    public ClusterTruthTable(PointClusters truth, PointClusters generated) {
        Objects.requireNonNull(truth, "truth cannot be null");
        Objects.requireNonNull(generated, "generated cannot be null");

        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        List<UnorderedPair> pairs = truth.getUnorderedPointPairs();
        for (UnorderedPair pair : pairs) {
            DataPoint a = (DataPoint) pair.getFirst();
            int ta = truth.expectCluster(a).getIndex();
            int ga = generated.expectCluster(a).getIndex();

            DataPoint b = (DataPoint) pair.getSecond();
            int tb = truth.expectCluster(b).getIndex();
            int gb = generated.expectCluster(b).getIndex();

            truePositives += (ga == gb && ta == tb) ? 1 : 0;
            trueNegatives += (ga != gb && ta != tb) ? 1 : 0;
            falsePositives += (ga == gb && ta != tb) ? 1 : 0;
            falseNegatives += (ga != gb && ta == tb) ? 1 : 0;
        }
        this.pairs = Collections.unmodifiableList(pairs);

        this.truePositives = this.tp = truePositives;
        this.trueNegatives = this.tn = trueNegatives;
        this.falsePositives = this.fp = falsePositives;
        this.falseNegatives = this.fn = falseNegatives;
    }

}
