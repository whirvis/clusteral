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
package io.whirvis.edu.clustering;

import java.util.Objects;

/**
 * Represents an unordered pair.
 */
public final class UnorderedPair {

    private final Object first;
    private final Object second;

    /**
     * Constructs a new {@code UnorderedPair}.
     *
     * @param first  the first object.
     * @param second the second object.
     */
    public UnorderedPair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first object given at construction.
     *
     * @return the first object given at construction.
     */
    public Object getFirst() {
        return this.first;
    }

    /**
     * Returns the second object given at construction.
     *
     * @return the second object given at construction.
     */
    public Object getSecond() {
        return this.second;
    }

    @Override
    public int hashCode() {
        int firstHash = Objects.hashCode(first);
        int secondHash = Objects.hashCode(second);
        int maxHash = Math.max(firstHash, secondHash);
        int minHash = Math.min(firstHash, secondHash);
        return Objects.hash(maxHash, minHash);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true; /* we equal ourselves */
        } else if (obj == null) {
            return false; /* nothing to compare with */
        } else if (this.getClass() != obj.getClass()) {
            return false; /* child must implement */
        }

        UnorderedPair that = (UnorderedPair) obj;
        return (Objects.equals(this.first, that.first)
                && Objects.equals(this.second, that.second))
                || (Objects.equals(this.first, that.second)
                && Objects.equals(this.second, that.first));
    }

}
