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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a data point in a {@link DataPointFile}.
 * <p>
 * This is more-or-less a container for a {@code double[]}.
 * <p>
 * Since arrays are a primitive type, they can be a hassle to work
 * with (checking for equality, printing as a string, etc.) This type
 * deals with that by wrapping around the array and implementing some
 * of {@link Object}'s built-in methods.
 *
 * @see #squaredError(DataPoint)
 * @see DataPointFile#getPoint(int)
 */
@SuppressWarnings("unused")
public final class DataPoint
        implements Iterable<Double>, Comparable<DataPoint> {

    private final DataPointFile file;
    private final int index;
    private final double[] axes;
    private final int trueClusterIndex;
    private final ReadWriteLock sharedPointLock;
    private final boolean free;

    /**
     * Constructs a new {@code DataPoint}.
     * <p>
     * <b>Accessibility:</b> This constructor should only be called by
     * {@link DataPointFile}.
     *
     * @param file             the original file.
     * @param index            the index of this point.
     * @param axes             the axes of this point.
     * @param trueClusterIndex the index of the cluster this point truly
     *                         belongs to, {@code -1} if the tru cluster
     *                         is not known.
     * @param pointLock        the read-write lock to use when working with
     *                         data points to ensure thread safety. This lock
     *                         should be shared among other points in the
     *                         same file.
     * @throws NullPointerException     if {@code file}, {@code axes} or
     *                                  {@code pointLock} are {@code null}.
     * @throws IllegalArgumentException if the dimensionality of the axes
     *                                  is not equal to the dimensionality
     *                                  of {@code file}.
     */
    /* package-private */
    DataPoint(
            DataPointFile file,
            int index,
            double[] axes,
            int trueClusterIndex,
            ReadWriteLock pointLock) {
        Objects.requireNonNull(file, "file cannot be null");
        Objects.requireNonNull(axes, "axes cannot be null");
        Objects.requireNonNull(pointLock, "pointLock cannot be null");
        if (file.getDimensions() != axes.length) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        this.file = file;
        this.index = index;
        this.axes = axes;
        this.trueClusterIndex = trueClusterIndex;
        this.sharedPointLock = pointLock;
        this.free = false;
    }

    /**
     * Constructs a new {@code DataPoint} which belongs to no file and
     * has no index.
     *
     * @param axes the axes of this point.
     * @throws NullPointerException if {@code axes} is {@code null}.
     */
    public DataPoint(double... axes) {
        this.file = null;
        this.index = -1;
        this.axes = axes;
        this.trueClusterIndex = -1;
        this.sharedPointLock = new ReentrantReadWriteLock();
        this.free = true;
    }

    /**
     * Returns the file this data point originates from.
     *
     * @return the file this data point originates from, {@code null} if
     * this data point is a free point.
     * @see #getIndex()
     * @see #isFree()
     */
    public DataPointFile getFile() {
        return this.file;
    }

    /**
     * Returns the index of this data point.
     *
     * @return the index of this data point, {@code -1} if this data point
     * is a free point.
     * @see #getFile()
     * @see #isFree()
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the dimensionality of this data point.
     * <p>
     * In this case, "dimensionality" refers to the number of axes each
     * data point in this file has. A point with an X, Y, Z axis is said
     * to have a dimensionality of three since it has three axes.
     *
     * @return the dimensionality of this data point.
     */
    public int getDimensions() {
        return axes.length;
    }

    /**
     * Returns the value for an axis in this data point.
     *
     * @param index the index of the axis to retrieve.
     * @return the value for the axis at {@code index}.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the axis
     *                                   count.
     */
    public double getAxis(int index) {
        sharedPointLock.readLock().lock();
        try {
            return this.axes[index];
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Sets the value for an axis in this data point.
     * <p>
     * <b>Accessibility:</b> This method should only be called by
     * {@link DataPointFile}.
     *
     * @param index the index of the axis to update.
     * @param value the new value for the axis.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the axis
     *                                   count.
     */
    /* package-private */
    void setAxis(int index, double value) {
        sharedPointLock.writeLock().lock();
        try {
            this.axes[index] = value;
        } finally {
            sharedPointLock.writeLock().unlock();
        }
    }

    /**
     * Returns if this point's true cluster is known.
     *
     * @return {@code true} if this point's true cluster is known,
     * {@code false} otherwise.
     */
    public boolean isTrueClusterKnown() {
        return this.trueClusterIndex >= 0;
    }

    /**
     * Returns the index of this point's true cluster.
     *
     * @return the index of this point's true cluster, {@code -1} if the
     * true cluster is not known.
     * @see #isTrueClusterKnown()
     */
    public int getTrueClusterIndex() {
        return this.trueClusterIndex;
    }

    /**
     * Multiplies this data point by the given scalar.
     * <p>
     * <b>Note:</b> The resulting data point is a <i>free point</i>,
     * regardless if this data point is itself a free point or not.
     *
     * @param scalar the scalar to multiply by.
     * @return the resulting data point.
     */
    public DataPoint multiplyBy(double scalar) {
        double[] result = new double[axes.length];
        for (int i = 0; i < axes.length; i++) {
            result[i] = axes[i] * scalar;
        }
        return new DataPoint(result);
    }

    /**
     * Divides this data point by the given scalar.
     * <p>
     * <b>Note:</b> The resulting data point is a <i>free point</i>,
     * regardless if this data point is itself a free point or not.
     *
     * @param scalar the scalar to divide by.
     * @return the resulting data point.
     */
    public DataPoint divideBy(double scalar) {
        double[] result = new double[axes.length];
        for (int i = 0; i < axes.length; i++) {
            result[i] = axes[i] / scalar;
        }
        return new DataPoint(result);
    }

    /**
     * Returns if this data point is a free point.
     * <p>
     * A data point is considered to be free when it has no file.
     * Free points are particularly useful for centroids.
     *
     * @return {@code true} if this data point is a free point,
     * {@code false} otherwise.
     */
    public boolean isFree() {
        return this.free;
    }

    /**
     * Calculates the Euclidean distance between this point and the
     * given point.
     * <p>
     * <b>Note:</b> This is <i>not</i> the same as calculating the distance
     * between two points, which involves the square root function. This is
     * intended for use in calculating the SSE (Sum-of-Squared Error) when
     * working with clustered data points.
     *
     * @param point the point to compare to.
     * @return the Euclidean distance between this point and {@code point}.
     * @throws NullPointerException     if {@code point} is {@code null}.
     * @throws IllegalArgumentException if the dimensionality of this point
     *                                  is not the same as {@code point}.
     */
    @SuppressWarnings("ExtractMethodRecommender")
    public double squaredError(DataPoint point) {
        Objects.requireNonNull(point, "point cannot be null");
        if (point == this) {
            return 0.0d; /* no need to calculate */
        } else if (point.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        this.sharedPointLock.readLock().lock();
        point.sharedPointLock.readLock().lock();
        try {
            double error = 0.0d;
            for (int i = 0; i < axes.length; i++) {
                /*
                 * If either axis has a value of NaN, then do not use it
                 * in calculating the distance between the two points. This
                 * usually occurs after normalization in which one of the
                 * axes could not be properly normalized due to an anomaly.
                 */
                if (Double.isNaN(point.axes[i])) {
                    continue; /* cannot calculate distance */
                } else if (Double.isNaN(this.axes[i])) {
                    continue; /* cannot calculate distance */
                }
                double dist = point.axes[i] - this.axes[i];
                error += dist * dist;
            }
            return error;
        } finally {
            point.sharedPointLock.readLock().unlock();
            this.sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns the nearest point to this point.
     *
     * @param points to the points to search through.
     * @return the nearest point to this point.
     * @throws NullPointerException if{@code points} is {@code null}.
     * @see #getNearestPoint(DataPoint, Iterable)
     */
    public DataPoint getNearestPoint(Iterable<DataPoint> points) {
        return getNearestPoint(this, points);
    }

    /**
     * Returns the farthest point from this point.
     *
     * @param points to the points to search through.
     * @return the farthest point from this point.
     * @throws NullPointerException if {@code points} is {@code null}.
     * @see #getFarthestPoint(DataPoint, Iterable)
     */
    public DataPoint getFarthestPoint(Iterable<DataPoint> points) {
        return getFarthestPoint(this, points);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Double> iterator() {
        return Arrays.stream(axes).iterator();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(DataPoint point) {
        Objects.requireNonNull(point, "point cannot be null");
        if (point == this) {
            return 0; /* no need to calculate */
        } else if (point.getDimensions() != this.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        this.sharedPointLock.readLock().lock();
        point.sharedPointLock.readLock().lock();
        try {
            /*
             * This comparison method uses lexicographic ordering.
             *
             * This type of ordering uses the first axis in this point in
             * which both points do not have an identical value. This axis
             * is used to determine if this point is greater than or less
             * than the other.
             */
            for (int i = 0; i < axes.length; i++) {
                double thisAxis = this.axes[i];
                double pointAxis = point.axes[i];
                if (thisAxis != pointAxis) {
                    return thisAxis > pointAxis ? +1 : -1;
                }
            }
            return 0; /* no difference */
        } finally {
            point.sharedPointLock.readLock().unlock();
            this.sharedPointLock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        sharedPointLock.readLock().lock();
        try {
            return Objects.hash(index,
                    Arrays.hashCode(axes),
                    trueClusterIndex);
        } finally {
            sharedPointLock.readLock().unlock();
        }
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

        DataPoint that = (DataPoint) obj;

        this.sharedPointLock.readLock().lock();
        that.sharedPointLock.readLock().lock();
        try {
            return this.index == that.index
                    && Arrays.equals(this.axes, that.axes)
                    && this.trueClusterIndex == that.trueClusterIndex;
        } finally {
            that.sharedPointLock.readLock().unlock();
            this.sharedPointLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        sharedPointLock.readLock().lock();
        try {
            StringBuilder pointsLine = new StringBuilder();
            for (int i = 0; i < axes.length; i++) {
                pointsLine.append(POINT_FORMAT.format(axes[i]));
                pointsLine.append(i + 1 < axes.length ? " " : "");
            }
            if (this.isTrueClusterKnown()) {
                pointsLine.append(axes.length > 0 ? " " : "");
                pointsLine.append(trueClusterIndex);
            }
            return pointsLine.toString();
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    private static final DecimalFormat POINT_FORMAT = new DecimalFormat(
            "#.#", DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * Returns the nearest point to a given point.
     *
     * @param point  the point whose nearest to search for.
     * @param points to the points to search through.
     * @return the nearest point to {@code point}.
     * @throws NullPointerException if {@code point} or {@code points}
     *                              are {@code null}.
     * @see #getNearestPoint(Iterable)
     */
    public static DataPoint getNearestPoint(
            DataPoint point, Iterable<DataPoint> points) {
        Objects.requireNonNull(point, "point cannot be null");
        Objects.requireNonNull(points, "points cannot be null");

        DataPoint nearestPoint = null;
        double lowestError = Double.POSITIVE_INFINITY;

        for (DataPoint candidate : points) {
            double error = point.squaredError(candidate);
            if (error < lowestError) {
                lowestError = error;
                nearestPoint = candidate;
            }
        }

        return nearestPoint;
    }

    /**
     * Returns the farthest point from a given point.
     *
     * @param point  the point whose farthest to search for.
     * @param points to the points to search through.
     * @return the farthest point from {@code point}.
     * @throws NullPointerException if {@code point} or {@code points}
     *                              are {@code null}.
     * @see #getFarthestPoint(Iterable)
     */
    public static DataPoint getFarthestPoint(
            DataPoint point, Iterable<DataPoint> points) {
        Objects.requireNonNull(point, "point cannot be null");
        Objects.requireNonNull(points, "points cannot be null");

        DataPoint farthestPoint = null;
        double greatestError = Double.NEGATIVE_INFINITY;

        for (DataPoint candidate : points) {
            double error = point.squaredError(candidate);
            if (error > greatestError) {
                greatestError = error;
                farthestPoint = candidate;
            }
        }

        return farthestPoint;
    }

    /**
     * Adds together the specified points.
     *
     * @param points the points to add together.
     * @return the resulting data point.
     * @throws NullPointerException if {@code points} or any of its
     *                              elements are {@code null}.
     */
    public static DataPoint add(Iterable<DataPoint> points) {
        Objects.requireNonNull(points, "points cannot be null");

        Iterator<DataPoint> pointsI = points.iterator();
        if (!pointsI.hasNext()) {
            throw new IllegalArgumentException("No points provided");
        }

        DataPoint firstPoint = pointsI.next();
        int dimensions = firstPoint.getDimensions();

        double[] axes = new double[dimensions];
        for (DataPoint point : points) {
            Objects.requireNonNull(point, "point cannot be null");
            if (point.getDimensions() != axes.length) {
                throw new IllegalArgumentException("Dimension mismatch");
            }
            for (int i = 0; i < axes.length; i++) {
                axes[i] += point.getAxis(i);
            }
        }

        return new DataPoint(axes);
    }

    /**
     * Adds together the specified points.
     *
     * @param points the points to add together.
     * @return the resulting data point.
     */
    public static DataPoint add(DataPoint... points) {
        return add(Arrays.asList(points));
    }

}
