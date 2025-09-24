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
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a cluster of data points.
 * <p>
 * All point clusters are part of a {@link PointClusters group}. This is to
 * ensure data integrity (e.g., ensuring one point is not present in multiple
 * clusters).
 *
 * @see DataPoint
 * @see #setCentroid(DataPoint)
 * @see #hasCoincidenceCenter()
 */
@SuppressWarnings("unused")
public final class PointCluster implements Iterable<DataPoint> {

    private static final Lock POINT_FORMAT_LOCK = new ReentrantLock();
    private static final DecimalFormat POINT_FORMAT = new DecimalFormat(
            "#.#", DecimalFormatSymbols.getInstance(Locale.US));

    private final PointClusters group;
    private final int index;
    private DataPoint centroid;
    private final List<DataPoint> points;
    private DataPoint previousMean;

    private final ReadWriteLock sharedPointLock;
    private final ReadWriteLock thisClusterLock;

    /**
     * Constructs a new {@code PointCluster}.
     * <p>
     * <b>Accessibility:</b> This constructor should only be called by
     * {@link PointClusters}.
     *
     * @param group     the cluster group.
     * @param index     the index of this cluster. This should correspond to
     *                  the index in {@link PointClusters#getCluster(int)}.
     * @param pointLock the read-write lock to use when working with data
     *                  points to ensure thread safety. This lock should be
     *                  shared among other clusters in the same group. This
     *                  is to ensure multiple clusters cannot modify their
     *                  points simultaneously.
     * @throws NullPointerException if {@code group} or {@code pointLock}
     *                              are {@code null}.
     */
    /* package-private */
    PointCluster(
            PointClusters group,
            int index,
            ReadWriteLock pointLock) {
        Objects.requireNonNull(group, "group cannot be null");
        Objects.requireNonNull(pointLock, "pointLock cannot be null");

        this.group = group;
        this.index = index;
        this.points = new ArrayList<>();

        this.sharedPointLock = pointLock;
        this.thisClusterLock = new ReentrantReadWriteLock();
    }

    /**
     * Returns the group this cluster is a part of.
     *
     * @return the group this cluster is a part of.
     * @see #getIndex()
     */
    public PointClusters getGroup() {
        return this.group;
    }

    /**
     * Returns the index of this cluster in its cluster group.
     *
     * @return the index of this cluster in its cluster group.
     * @see PointClusters#getCluster(int)
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the centroid of this cluster.
     *
     * @return the centroid of this cluster, may be {@code null} for no
     * centroid.
     */
    public DataPoint getCentroid() {
        thisClusterLock.readLock().lock();
        try {
            return this.centroid;
        } finally {
            thisClusterLock.readLock().unlock();
        }
    }

    /**
     * Sets the centroid of this cluster.
     *
     * @param point the centroid, may be {@code null} for no centroid.
     * @throws IllegalArgumentException if the file of this cluster's group
     *                                  cannot contain {@code point}.
     * @see #addPoint(DataPoint)
     */
    public void setCentroid(DataPoint point) {
        if (point != null && !group.getFile().canContain(point)) {
            String msg = "point incompatible with file";
            throw new IllegalArgumentException(msg);
        }

        thisClusterLock.writeLock().lock();
        try {
            this.centroid = point;
        } finally {
            thisClusterLock.writeLock().unlock();
        }
    }

    /**
     * Returns the number of data points.
     *
     * @return the number of data points.
     */
    public int getPointCount() {
        return points.size();
    }

    /**
     * Returns if this cluster has a data point.
     *
     * @param point the data point to check for.
     * @return {@code true} if {@code point} is present in this cluster,
     * {@code false} otherwise.
     */
    public boolean hasPoint(DataPoint point) {
        sharedPointLock.readLock().lock();
        try {
            return point != null && points.contains(point);
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns all data points in this cluster.
     * <p>
     * <b>Note:</b> The returned list is <i>unmodifiable.</i>
     * <p>
     * Since this class implements the {@link Iterable} interface, it is
     * also possible to iterate over each data point with an enhanced for
     * loop. For example:
     * <pre>
     * for (DataPoint point : cluster) {
     *   &#47;* ... process points ... *&#47;
     * }
     * </pre>
     *
     * @return the data points in this cluster.
     */
    public List<DataPoint> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /**
     * Returns the data point at the given index.
     *
     * @param index the index, starting from zero.
     * @return the data point at the given index.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the point
     *                                   count of this cluster.
     */
    public DataPoint getPoint(int index) {
        sharedPointLock.readLock().lock();
        try {
            return points.get(index);
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Adds a data point to this cluster.
     * <p>
     * <b>Note:</b> For a point to be added, it <i>must not</i> be present
     * in another cluster within the group.
     *
     * @param point the data point to add.
     * @throws NullPointerException     if {@code point} is {@code null}.
     * @throws IllegalArgumentException if {@code point} does not originate
     *                                  from the cluster group's file.
     * @throws IllegalStateException    if {@code point} currently belongs to
     *                                  another cluster; if this cluster has
     *                                  been removed from its group.
     * @see #setCentroid(DataPoint)
     */
    public void addPoint(DataPoint point) {
        Objects.requireNonNull(point, "point cannot be null");

        sharedPointLock.writeLock().lock();
        try {
            if (!points.contains(point)) {
                group.assignOwner(point, this);
                points.add(point);
            }
        } finally {
            sharedPointLock.writeLock().unlock();
        }
    }

    /**
     * Removes a data point from this cluster.
     * <p>
     * If {@code point} is not present, this method is a no-op.
     *
     * @param point the data point to remove.
     */
    public void removePoint(DataPoint point) {
        if (point == null) {
            return;
        }

        sharedPointLock.writeLock().lock();
        try {
            if (points.contains(point)) {
                points.remove(point);
                group.clearOwner(point);
            }
        } finally {
            sharedPointLock.writeLock().unlock();
        }
    }

    /**
     * Removes all data points from this cluster.
     * <p>
     * If the cluster has no points, this method is a no-op.
     */
    public void clearPoints() {
        sharedPointLock.writeLock().lock();
        try {
            Iterator<DataPoint> pointsI = points.iterator();
            while (pointsI.hasNext()) {
                DataPoint point = pointsI.next();
                pointsI.remove();
                group.clearOwner(point);
            }
        } finally {
            sharedPointLock.writeLock().unlock();
        }
    }

    /**
     * Creates a <i>free</i> data point which is the mean of all points
     * in this cluster.
     *
     * @param usePreviousIfEmpty {@code true} if the previous mean should
     *                           be returned if the cluster is currently
     *                           empty, {@code false} otherwise.
     * @return the mean data point of this cluster, or the previous mean
     * data point if the cluster is empty and {@code usePreviousIfEmpty}
     * is {@code true}.
     * @throws IllegalStateException if the cluster is currently empty and
     *                               {@code usePreviousIfEmpty} is false or
     *                               there is no previous mean.
     */
    public DataPoint getMean(boolean usePreviousIfEmpty) {
        sharedPointLock.readLock().lock();
        try {
            if (points.isEmpty()) {
                if (!usePreviousIfEmpty) {
                    String msg = "Cluster is empty";
                    throw new IllegalStateException(msg);
                } else if (previousMean == null) {
                    String msg = "No previous mean";
                    throw new IllegalStateException(msg);
                }
                return this.previousMean;
            }

            DataPoint total = DataPoint.add(points);
            this.previousMean = total.divideBy(points.size());
            return this.previousMean;
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns the mean distance from a given point to all the points
     * in this cluster.
     * <p>
     * <b>Note:</b> If the given point instance is in this cluster, it
     * shall be excluded from calculations. However, other instances with
     * the same coordinates will <i>not</i> be excluded.
     *
     * @param point the point to measure.
     * @return the mean distance from {@code point} to all the points
     * in this cluster.
     */
    public double getMeanDistance(DataPoint point) {
        sharedPointLock.readLock().lock();
        try {
            int totalSize = 0;
            double totalSse = 0.0d;
            for (DataPoint element : points) {
                if (element == point) {
                    continue;
                }
                totalSse += point.squaredError(element);
                totalSize += 1;
            }
            return totalSse / totalSize;
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns the compactness of this cluster.
     * <p>
     * The compactness of a cluster is defined as the average distance
     * between the centroid and all of its points.
     *
     * @return the compactness of this cluster.
     */
    public double getCompactness() {
        return this.getMeanDistance(centroid);
    }

    private double getSingleLinkageDistance(
            PointCluster cluster) {
        double lowest = Double.MAX_VALUE;
        for (DataPoint ourPoint : points) {
            for (DataPoint theirPoint : cluster) {
                double dist = ourPoint.squaredError(theirPoint);
                if (dist < lowest) {
                    lowest = dist;
                }
            }
        }
        return lowest;
    }

    private double getCompleteLinkageDistance(
            PointCluster cluster) {
        double greatest = Double.MIN_VALUE;
        for (DataPoint ourPoint : points) {
            for (DataPoint theirPoint : cluster) {
                double dist = ourPoint.squaredError(theirPoint);
                if (dist > greatest) {
                    greatest = dist;
                }
            }
        }
        return greatest;
    }

    private double getAverageLinkageDistance(
            PointCluster cluster) {
        double total = 0.0d;
        for (DataPoint ourPoint : points) {
            for (DataPoint theirPoint : cluster) {
                double dist = ourPoint.squaredError(theirPoint);
                total += dist;
            }
        }
        int observations = this.getPointCount()
                * cluster.getPointCount();
        return total / observations;
    }

    private double getCentroidLinkageDistance(
            PointCluster cluster) {
        DataPoint theirCentroid = cluster.getCentroid();
        return centroid.squaredError(theirCentroid);
    }

    private double getAverageCentroidsLinkageDistance(
            PointCluster cluster) {
        double total = 0.0d;
        DataPoint ourCentroid = this.getCentroid();
        for (DataPoint theirPoint : cluster) {
            total += theirPoint.squaredError(ourCentroid);
        }
        DataPoint theirCentroid = cluster.getCentroid();
        for (DataPoint ourPoint : points) {
            total += ourPoint.squaredError(theirCentroid);
        }
        int observations = this.getPointCount()
                * cluster.getPointCount();
        return total / observations;
    }

    /**
     * Returns the distance between this cluster and another cluster based
     * on the given method.
     *
     * @param cluster the cluster to measure against.
     * @param linkage the linkage method to use in calculation.
     * @return the calculated distance.
     * @throws NullPointerException if {@code cluster} or {@code linkage}
     *                              are {@code null}.
     */
    public double getDistance(
            PointCluster cluster, LinkageMethod linkage) {
        Objects.requireNonNull(cluster, "cluster cannot be null");
        Objects.requireNonNull(linkage, "linkage cannot be null");
        if (cluster == this) {
            return 0.0d; /* nothing to compute */
        }

        sharedPointLock.readLock().lock();
        try {
            switch (linkage) {
                case SINGLE_LINKAGE:
                    return this.getSingleLinkageDistance(cluster);
                case COMPLETE_LINKAGE:
                    return this.getCompleteLinkageDistance(cluster);
                case AVERAGE_LINKAGE:
                    return this.getAverageLinkageDistance(cluster);
                case CENTROID_LINKAGE:
                    return this.getCentroidLinkageDistance(cluster);
                case AVERAGE_CENTROIDS_LINKAGE:
                    return this.getAverageCentroidsLinkageDistance(cluster);
                default:
                    String msg = "Unexpected case, this is a bug";
                    throw new UnsupportedOperationException(msg);
            }
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    private double getCompleteDiameter() {
        double greatest = Double.MIN_VALUE;
        for (DataPoint outer : points) {
            for (DataPoint inner : points) {
                double dist = outer.squaredError(inner);
                if (dist > greatest) {
                    greatest = dist;
                }
            }
        }
        return greatest;
    }

    private double getAverageDiameter() {
        double total = 0.0d;
        for (DataPoint outer : points) {
            for (DataPoint inner : points) {
                double dist = outer.squaredError(inner);
                total += dist;
            }
        }
        int observations = this.getPointCount()
                * (this.getPointCount() - 1);
        return total / observations;
    }

    private double getCentroidDiameter() {
        double total = 0.0d;
        for (DataPoint point : points) {
            double dist = point.squaredError(centroid);
            total += dist;
        }
        int observations = this.getPointCount();
        return 2 * (total / observations);
    }

    /**
     * Returns the diameter of this cluster based on the given method.
     *
     * @param diameter the method to use in calculation.
     * @return the calculated diameter, {@code 0.0d} if this cluster is empty
     * (which is usually an anomaly).
     * @throws NullPointerException  if {@code diameter} is {@code null}.
     * @throws IllegalStateException if this cluster has no points.
     */
    public double getDiameter(DiameterMethod diameter) {
        Objects.requireNonNull(diameter, "diameter cannot be null");
        if (this.isEmpty()) {
            return 0.0d;
        }

        sharedPointLock.readLock().lock();
        try {
            switch (diameter) {
                case COMPLETE:
                    return this.getCompleteDiameter();
                case AVERAGE:
                    return this.getAverageDiameter();
                case CENTROID:
                    return this.getCentroidDiameter();
                default:
                    String msg = "Unexpected case, this is a bug";
                    throw new UnsupportedOperationException(msg);
            }
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns the dispersion of this cluster.
     * <p>
     * The dispersion of a cluster is the square root of the average
     * distance of all its points from the mean.
     *
     * @return the dispersion of this cluster, {@code 0.0d} if this cluster
     * is empty (which is usually an anomaly).
     * of an anomaly.
     * @throws IllegalStateException if this cluster has no points.
     * @see #getMean(boolean)
     */
    public double getDispersion() {
        if (this.isEmpty()) {
            return 0.0d;
        }

        sharedPointLock.readLock().lock();
        try {
            DataPoint mean = this.getMean(false);

            double totalVariance = 0.0d;
            for (DataPoint point : points) {
                totalVariance += point.squaredError(mean);
            }

            double average = totalVariance / points.size();
            return Math.sqrt(average);
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns the sum-of-squared errors for this cluster.
     * <p>
     * The sum-of-squared errors is the squared error for each point and
     * the centroid of this cluster added together.
     *
     * @return the sum-of-squared errors for this cluster.
     * @throws IllegalStateException if this cluster has no centroid.
     * @see #setCentroid(DataPoint)
     * @see DataPoint#squaredError(DataPoint)
     */
    public double getSumOfSquaredErrors() {
        sharedPointLock.readLock().lock();
        thisClusterLock.readLock().lock();
        try {
            if (centroid == null) {
                String msg = "centroid must be set";
                throw new IllegalStateException(msg);
            }

            double sumOfSquaredErrors = 0.0d;
            for (DataPoint point : points) {
                sumOfSquaredErrors += centroid.squaredError(point);
            }
            return sumOfSquaredErrors;
        } finally {
            thisClusterLock.readLock().unlock();
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns if this cluster has a coincidence center.
     * <p>
     * A point cluster is considered to have a coincidence center iff
     * it has only one point which happens to be its centroid.
     *
     * @return {@code true} if this cluster has a coincidence center,
     * {@code false} otherwise.
     * @see #isEmpty()
     */
    public boolean hasCoincidenceCenter() {
        sharedPointLock.readLock().lock();
        try {
            return points.size() == 1 && this.hasPoint(centroid);
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    /**
     * Returns if this cluster is empty (has no points).
     *
     * @return {@code true} if this cluster is empty, {@code false}
     * otherwise.
     * @see #hasCoincidenceCenter()
     */
    public boolean isEmpty() {
        sharedPointLock.readLock().lock();
        try {
            return points.isEmpty();
        } finally {
            sharedPointLock.readLock().unlock();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<DataPoint> iterator() {
        return points.iterator();
    }

    @Override
    public int hashCode() {
        sharedPointLock.readLock().lock();
        thisClusterLock.readLock().lock();
        try {
            return Objects.hash(group, points, centroid);
        } finally {
            thisClusterLock.readLock().unlock();
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

        sharedPointLock.readLock().lock();
        thisClusterLock.readLock().lock();
        try {
            PointCluster that = (PointCluster) obj;
            return Objects.equals(this.group, that.group)
                    && Objects.equals(this.centroid, that.centroid)
                    && Objects.equals(this.points, that.points);
        } finally {
            thisClusterLock.readLock().unlock();
            sharedPointLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        sharedPointLock.readLock().lock();
        thisClusterLock.readLock().lock();
        try {
            return StrUtils.getStrJoiner(this)
                    .add("group=" + group)
                    .add("centroid=" + centroid)
                    .add("points=" + points)
                    .toString();
        } finally {
            thisClusterLock.readLock().unlock();
            sharedPointLock.readLock().unlock();
        }
    }

    private static String formatPoint(double number) {
        POINT_FORMAT_LOCK.lock();
        try {
            return POINT_FORMAT.format(number);
        } finally {
            POINT_FORMAT_LOCK.unlock();
        }
    }

}
