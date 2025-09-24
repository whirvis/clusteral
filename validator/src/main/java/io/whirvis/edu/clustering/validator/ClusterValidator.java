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

import java.util.Objects;

/**
 * A calculator for cluster validation indices.
 *
 * @see InternalClusterValidator
 * @see ExternalClusterValidator
 */
public abstract class ClusterValidator {

    private final ClusterValidatorType type;
    private final String name;
    private final String abbreviation;

    /**
     * Constructs a new {@code ClusterValidator}.
     *
     * @param type         the cluster validator type.
     * @param name         the name of this validator.
     * @param abbreviation the abbreviation of this validator's name.
     * @throws NullPointerException if {@code type}, {@code name} or
     *                              {@code abbreviation} are {@code null}.
     */
    protected ClusterValidator(
            ClusterValidatorType type,
            String name,
            String abbreviation) {
        this.type = Objects.requireNonNull(
                type, "type cannot be null");
        this.name = Objects.requireNonNull(
                name, "name cannot be null");
        this.abbreviation = Objects.requireNonNull(
                abbreviation, "abbreviation cannot be null");
    }

    /**
     * Returns the validator type.
     *
     * @return the validator type.
     */
    public final ClusterValidatorType getType() {
        return this.type;
    }

    /**
     * Returns the name of this validator.
     *
     * @return the name of this validator.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Returns the abbreviation of this validator's name.
     *
     * @return the abbreviation of this validator's name.
     */
    public final String getAbbreviation() {
        return this.abbreviation;
    }

}
