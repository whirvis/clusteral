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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a formula used for cluster validation.
 *
 * @see ClusterValidatorParam
 */
public enum ClusterValidatorFormula {

    CALINSKI_HARABASZ(
            "calinski-harabasz",
            "calinski_harabasz",
            "ch"
    ),

    DAVIES_BOULDIN(
            "davies-bouldin",
            "davies_bouldin",
            "db"
    ),

    DUNN_INDEX(
            "dunn-index",
            "dunn_index",
            "dunn",
            "di",
            "d"
    ),

    FOWLKES_MALLOWS(
            "fowlkes-mallows",
            "fowlkes_mallows",
            "fm"
    ),

    JACCARD_COEFFICIENT(
            "jaccard-coefficient",
            "jaccard_coefficient",
            "jaccard",
            "jc",
            "j"
    ),

    RAND_STATISTIC(
            "rand-statistic",
            "rand_statistic",
            "rand",
            "rs",
            "r"
    ),

    SILHOUETTE_WIDTH(
            "silhouette-width",
            "silhouette_width",
            "sw"
    );

    /*
     * Java doesn't allow enum constructors to access static fields of
     * their class. Putting them in a private inner class allows us to
     * work around that restriction.
     */
    private static class NameMap {
        private static final Map<String, ClusterValidatorFormula>
                ENTRIES = new HashMap<>();
    }

    ClusterValidatorFormula(String... names) {
        for (String name : names) {
            String lowercase = name.toLowerCase(Locale.ROOT);
            if (NameMap.ENTRIES.put(lowercase, this) != null) {
                String msg = "Two formulas cannot share a name";
                throw new InstantiationError(msg);
            }
        }
    }

    /**
     * Finds a formula from the given name.
     *
     * @param name the formula name, case-insensitive.
     * @return the cluster validator formula with the given name.
     * @throws NullPointerException     if {@code name} is {@code null}.
     * @throws IllegalArgumentException if there is no cluster validator
     *                                  formula with the given name.
     */
    public static ClusterValidatorFormula fromName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        String lowercase = name.toLowerCase(Locale.ROOT);

        ClusterValidatorFormula formula = NameMap.ENTRIES.get(lowercase);
        if (formula == null) {
            String msg = "No formula by the name \"" + name + "\"";
            throw new IllegalArgumentException(msg);
        }

        return formula;
    }

}
