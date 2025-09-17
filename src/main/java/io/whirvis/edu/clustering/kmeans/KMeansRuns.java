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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Contains runs of {@link KMeans} that were done in succession.
 *
 * @see KMeansRun
 */
@SuppressWarnings("unused")
public final class KMeansRuns implements Iterable<KMeansRun> {

    private final List<KMeansRun> runs;

    /**
     * Constructs a new {@code KMeansRuns}.
     *
     * @param runs the runs.
     */
    /* package-private */
    KMeansRuns(List<KMeansRun> runs) {
        this.runs = Collections.unmodifiableList(runs);
    }

    /**
     * Returns the number of runs.
     *
     * @return the number of runs.
     */
    public int getNumberRuns() {
        return runs.size();
    }

    /**
     * Returns the specified run.
     *
     * @param runNumber the run number, starting from zero.
     * @return the specified run.
     * @throws IndexOutOfBoundsException if {@code runNumber} is less than
     *                                   zero or greater than or equal to the
     *                                   number of runs.
     */
    public KMeansRun getRun(int runNumber) {
        return runs.get(runNumber);
    }

    /**
     * Returns the first run.
     *
     * @return the first run.
     */
    public KMeansRun getFirst() {
        return runs.get(0);
    }

    /**
     * Returns the last run.
     *
     * @return the last run.
     */
    public KMeansRun getLast() {
        return runs.get(runs.size() - 1);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<KMeansRun> iterator() {
        return runs.iterator();
    }

}
