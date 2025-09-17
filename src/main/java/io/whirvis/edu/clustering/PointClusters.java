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

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a group of {@link PointCluster point clusters}.
 * <p>
 * This exists to ensure integrity between multiple point clusters (e.g.,
 * ensuring one point is not part of multiple clusters).
 *
 * @see DataPoint
 */
@SuppressWarnings("unused")
public final class PointClusters implements Iterable<PointCluster> {

    private final DataPointFile file;
    private final List<PointCluster> clusters;
    private final Map<DataPoint, PointCluster> owners;

    private final ReadWriteLock pointLock;
    private final ReadWriteLock clusterLock;

    /**
     * Constructs a new {@code PointClusterGroup}.
     * <p>
     * <b>Accessibility:</b> This constructor should only be called by
     * {@link DataPointFile}.
     *
     * @param file the owning cluster file.
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    public PointClusters(DataPointFile file) {
        Objects.requireNonNull(file, "file cannot be null");

        this.file = file;
        this.clusters = new ArrayList<>();
        this.owners = new HashMap<>();

        this.pointLock = new ReentrantReadWriteLock();
        this.clusterLock = new ReentrantReadWriteLock();
    }

    /**
     * Returns the file this group originates from.
     *
     * @return the file this group originates from.
     */
    public DataPointFile getFile() {
        return this.file;
    }

    /**
     * Returns the number of points in the file this group originates from.
     *
     * @return the number of points in the file this group originates from.
     * @see #getFile()
     * @see DataPointFile#getPointCount()
     */
    public int getPointCount() {
        return file.getPointCount();
    }

    /**
     * Returns if the file this group originates from has a data point.
     *
     * @param point the data point to check for.
     * @return {@code true} if {@code point} is present in the file this
     * group originates from, {@code false} otherwise.
     */
    public boolean hasPoint(DataPoint point) {
        return file.hasPoint(point);
    }

    /**
     * Returns all data points in the file this group originates from.
     * <p>
     * <b>Note:</b> The returned list is <i>unmodifiable.</i>
     * <p>
     * Since the {@link DataPoint} class implements the {@link Iterable}
     * interface, it is also possible to iterate over each data point with
     * an enhanced for loop. For example:
     * <pre>
     * for (DataPoint point : clusters.getFile()) {
     *   &#47;* ... process points ... *&#47;
     * }
     * </pre>
     *
     * @return the data points in the file this group originates from.
     */
    public List<DataPoint> getPoints() {
        return file.getPoints();
    }

    /**
     * Returns the data point at the given index in the file this group
     * originates from.
     *
     * @param index the index, starting from zero.
     * @return the data point at the given index in the file this group
     * originates from.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the point
     *                                   count of this file.
     */
    public DataPoint getPoint(int index) {
        return file.getPoint(index);
    }

    /**
     * Returns all unordered pairs of points in the file this group
     * originates from.
     *
     * @return all unordered pairs of points in the file this group
     * originates from.
     */
    public List<UnorderedPair> getUnorderedPointPairs() {
        return file.getUnorderedPointPairs();
    }

    /**
     * Returns the centroid of the file this group originates from.
     *
     * @return the centroid of the file this group originates from.
     * @see #getFile()
     * @see DataPointFile#getBaryCenter()
     * @see PointCluster#getCentroid()
     */
    public DataPoint getBaryCenter() {
        return file.getBaryCenter();
    }

    /**
     * Returns the number of clusters in this group.
     *
     * @return the number of clusters in this group.
     */
    public int getClusterCount() {
        return clusters.size();
    }

    /**
     * Returns the cluster at the specified index.
     *
     * @param index the index of the cluster.
     * @return the cluster at the specified index.
     * @throws IndexOutOfBoundsException {@code index} is negative or
     *                                   greater than or equal to the
     *                                   cluster count of this group.
     */
    public PointCluster getCluster(int index) {
        return clusters.get(index);
    }

    /**
     * Returns all clusters in this group.
     * <p>
     * <b>Note:</b> The returned list is <i>unmodifiable.</i>
     * <p>
     * Since this class implements the {@link Iterable} interface, it is
     * also possible to iterate over each point cluster with an enhanced
     * for loop. For example:
     * <pre>
     * for (PointCluster cluster : clusters) {
     *   &#47;* ... process clusters ... *&#47;
     * }
     * </pre>
     *
     * @return all clusters in this group.
     */
    public List<PointCluster> getClusters() {
        return Collections.unmodifiableList(clusters);
    }

    /**
     * Returns if this group has a cluster.
     *
     * @param cluster the cluster to check for.
     * @return {@code true} if {@code cluster} is part of this group,
     * {@code false} otherwise.
     */
    public boolean hasCluster(PointCluster cluster) {
        if (cluster == null) {
            return false;
        }

        clusterLock.readLock().lock();
        try {
            return clusters.contains(cluster);
        } finally {
            clusterLock.readLock().unlock();
        }
    }

    /**
     * Adds a cluster to this group.
     *
     * @return the newly added cluster.
     */
    public PointCluster addCluster() {
        clusterLock.writeLock().lock();
        try {
            PointCluster cluster = new PointCluster(
                    this, clusters.size(), pointLock);
            clusters.add(cluster);
            return cluster;
        } finally {
            clusterLock.writeLock().unlock();
        }
    }

    /**
     * Adds a cluster to this group.
     *
     * @param centroid the initial centroid for this cluster. This will
     *                 be added as a point to the cluster before being
     *                 set as the centroid.
     * @return the newly added cluster.
     * @throws NullPointerException  if {@code centroid} is {@code null}.
     * @throws IllegalStateException if {@code centroid} is already present
     *                               in another cluster within this group.
     */
    @SuppressWarnings("UnusedReturnValue")
    public PointCluster addCluster(DataPoint centroid) {
        Objects.requireNonNull(centroid, "centroid cannot be null");

        clusterLock.writeLock().lock();
        try {
            PointCluster cluster = this.addCluster();
            cluster.addPoint(centroid);
            cluster.setCentroid(centroid);
            return cluster;
        } finally {
            clusterLock.writeLock().unlock();
        }
    }

    /**
     * Removes a cluster from this group.
     * <p>
     * All points currently part of the cluster will be removed from the
     * cluster. If the cluster is not a part of this group, this method is
     * a no-op.
     *
     * @param cluster the cluster to remove.
     */
    public void removeCluster(PointCluster cluster) {
        if (cluster == null) {
            return;
        }
        clusterLock.writeLock().lock();
        try {
            if (clusters.contains(cluster)) {
                cluster.forEach(this::clearOwner);
                clusters.remove(cluster);
            }
        } finally {
            clusterLock.writeLock().unlock();
        }
    }

    /**
     * Returns the cluster in which a data point resides.
     * <p>
     * <b>Note:</b> By design, one data point cannot belong to multiple
     * clusters at a time.
     *
     * @param point the point to query.
     * @return the owning cluster, {@code null} if none.
     */
    public PointCluster getCluster(DataPoint point) {
        pointLock.readLock().lock();
        clusterLock.readLock().lock();
        try {
            return point != null ? owners.get(point) : null;
        } finally {
            clusterLock.readLock().unlock();
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns the cluster in which a data point resides, and throws an
     * exception if it does not reside in one.
     * <p>
     * <b>Note:</b> By design, one data point cannot belong to multiple
     * clusters at a time.
     *
     * @param point the point to query.
     * @return the owning cluster.
     * @throws NullPointerException  if {@code point} is {@code null}.
     * @throws IllegalStateException if {@code point} currently does not
     *                               reside in a cluster (has no owner).
     */
    public PointCluster expectCluster(DataPoint point) {
        Objects.requireNonNull(point, "point cannot be null");
        pointLock.readLock().lock();
        clusterLock.readLock().lock();
        try {
            PointCluster owner = owners.get(point);
            if (owner == null) {
                String msg = "point currently has no owner";
                throw new IllegalStateException(msg);
            }
            return owner;
        } finally {
            clusterLock.readLock().unlock();
            pointLock.readLock().unlock();
        }
    }

    /**
     * Assigns a data point to a cluster.
     * <p>
     * This <i>must</i> be invoked by {@link PointCluster} before adding
     * a data point to ensure the point is not simultaneously part of two
     * clusters.
     * <p>
     * <b>Accessibility:</b> This method should only be called by
     * {@link PointCluster}.
     *
     * @param point   the data point to assign.
     * @param cluster the cluster to assign {@code point} to.
     * @throws NullPointerException     if {@code point} or {@code cluster}
     *                                  are {@code null}.
     * @throws IllegalArgumentException if {@code point} does not originate
     *                                  from the file.
     * @throws IllegalStateException    if {@code point} currently belongs to
     *                                  another cluster; if {@code cluster} is
     *                                  not part of this group.
     * @see #clearOwner(DataPoint)
     */
    /* package-private */
    void assignOwner(DataPoint point, PointCluster cluster) {
        Objects.requireNonNull(point, "point cannot be null");
        Objects.requireNonNull(cluster, "cluster cannot be null");

        pointLock.writeLock().lock();
        clusterLock.writeLock().lock();
        try {
            if (!file.hasPoint(point)) {
                String msg = "point not part of the file";
                throw new IllegalArgumentException(msg);
            }

            if (!clusters.contains(cluster)) {
                String msg = "cluster not a part of this group";
                throw new IllegalStateException(msg);
            }

            PointCluster owner = owners.get(point);
            if (owner != null && owner != cluster) {
                String msg = "point already part of another cluster";
                throw new IllegalStateException(msg);
            }

            owners.put(point, cluster);
        } finally {
            clusterLock.writeLock().unlock();
            pointLock.writeLock().unlock();
        }
    }

    /**
     * Clears a data point from a cluster.
     * <p>
     * This <i>must</i> be invoked by {@link PointCluster} after removing
     * a data point so the point can be added to another cluster later on.
     * <p>
     * <b>Accessibility:</b> This method should only be called by
     * {@link PointCluster}.
     *
     * @param point the point to clear.
     * @see #assignOwner(DataPoint, PointCluster)
     */
    /* package-private */
    void clearOwner(DataPoint point) {
        pointLock.writeLock().lock();
        clusterLock.writeLock().lock();
        try {
            owners.remove(point);
        } finally {
            clusterLock.writeLock().unlock();
            pointLock.writeLock().unlock();
        }
    }

    private List<PointCluster> findMultipleNearestClusters(
            DataPoint point, double lowestError) {
        List<PointCluster> nearestClusters = new ArrayList<>();
        for (PointCluster cluster : clusters) {
            DataPoint centroid = cluster.getCentroid();
            if (centroid == null) {
                continue; /* nothing to compare */
            }

            double error = point.squaredError(centroid);
            if (error == lowestError) {
                nearestClusters.add(cluster);
            }
        }
        return nearestClusters;
    }

    /**
     * Returns the nearest cluster to a point by its centroid.
     *
     * @param point            the point to find the nearest cluster to.
     * @param randomOnMultiple {@code true} if a random cluster should be
     *                         returned when multiple nearest clusters are
     *                         found, {@code false} otherwise.
     * @param excludeOwner     {@code true} if the owner of the data point
     *                         should be excluded from distance calculations,
     *                         {@code false} otherwise.
     * @return the nearest cluster, {@code null} if there are no clusters
     * with a centroid in this cluster group.
     */
    public PointCluster getNearestClusterByCentroid(
            DataPoint point,
            boolean randomOnMultiple,
            boolean excludeOwner) {
        PointCluster nearestCluster = null;
        double lowestError = Double.POSITIVE_INFINITY;

        PointCluster owner = this.getCluster(point);

        for (PointCluster cluster : clusters) {
            DataPoint centroid = cluster.getCentroid();
            if (centroid == null) {
                continue; /* nothing to compare with */
            } else if (excludeOwner && cluster == owner) {
                continue; /* ignore this cluster by request */
            }

            double error = point.squaredError(centroid);
            if (error < lowestError) {
                lowestError = error;
                nearestCluster = cluster;
            }
        }

        if (randomOnMultiple) {
            Random random = new Random();
            List<PointCluster> nearestClusters =
                    this.findMultipleNearestClusters(point, lowestError);
            int chosenIndex = random.nextInt(nearestClusters.size());
            nearestCluster = nearestClusters.get(chosenIndex);
        }

        return nearestCluster;
    }

    /**
     * Adds all currently unassociated data points to their nearest cluster
     * based on the cluster's centroid.
     *
     * @param randomOnMultipleNearest when {@code true}, if there are multiple
     *                                nearest centroids to a given point, the
     *                                program will choose a random one rather
     *                                than the first one it finds.
     * @throws IllegalStateException if no clusters in this group have been
     *                               assigned a centroid.
     * @see #fixCoincidentCenters()
     */
    public void groupPointsByNearestCentroid(
            boolean randomOnMultipleNearest) {
        List<DataPoint> ownedPoints = new ArrayList<>();
        for (PointCluster cluster : clusters) {
            ownedPoints.addAll(cluster.getPoints());
        }

        for (DataPoint point : file) {
            if (ownedPoints.contains(point)) {
                continue; /* already assigned to a cluster */
            }
            PointCluster nearest = this.getNearestClusterByCentroid(
                    point, randomOnMultipleNearest, false);
            if (nearest == null) {
                String msg = "No clusters have a centroid";
                throw new IllegalStateException(msg);
            }
            nearest.addPoint(point);
        }
    }

    private void fixIfCoincidenceCenter(PointCluster cluster) {
        if (!cluster.hasCoincidenceCenter()) {
            return; /* nothing to fix */
        }

        DataPoint centroid = cluster.getCentroid();
        DataPoint farthest = file.getFarthestPoint(centroid);

        /*
         * This probably won't happen, but it shouldn't happen either.
         * If it does, something has gone wrong in the setup preceding
         * this function call.
         */
        PointCluster owner = this.getCluster(farthest);
        if (owner == null) {
            throw new PointWithNoOwnerException();
        }

        /*
         * Before fixing this coincidence, we must first make sure that
         * the farthest point away is not also the centroid of some other
         * cluster. If it is, we can't remove it, or it will prevent the
         * K-means algorithm from properly functioning.
         */
        if (farthest.equals(owner.getCentroid())) {
            throw new UnfixableCoincidenceException();
        }

        /*
         * Now that we've verified everything, we can finalize fixing this
         * coincidence center. We must first remove the farthest point from
         * its current cluster. Then, we can add it to the cluster we are
         * fixing and set it as the centroid.
         */
        owner.removePoint(farthest);
        cluster.addPoint(farthest);
        cluster.setCentroid(farthest);
    }

    /**
     * Fixes all clusters in this group which have a coincident center.
     * <p>
     * Coincident centers are fixed according the protocol specified in
     * the document for Phase 2.
     * <blockquote>
     * "If you notice a singleton cluster (that is, a cluster whose sole
     * member is its center) at the end of any iteration, you can remedy
     * this problem by locating the point that contributes the most to the
     * error of its cluster and making this point to be the center of the
     * singleton cluster."
     * </blockquote>
     *
     * @throws UnfixableCoincidenceException if a cluster with a coincident
     *                                       center could not be resolved.
     * @see #groupPointsByNearestCentroid(boolean)
     */
    public void fixCoincidentCenters() {
        for (PointCluster cluster : clusters) {
            this.fixIfCoincidenceCenter(cluster);
        }
    }

    /**
     * Returns the total sum-of-squared errors for every cluster in this
     * group added together.
     *
     * @return the total sum-of-squared errors for every cluster in this
     * group added together.
     * @throws IllegalStateException if one of the clusters has no centroid.
     * @see PointCluster#setCentroid(DataPoint)
     * @see PointCluster#getSumOfSquaredErrors()
     * @see DataPoint#squaredError(DataPoint)
     */
    public double getSumOfSquaredErrors() {
        clusterLock.readLock().lock();
        try {
            double totalSse = 0.0d;
            for (PointCluster cluster : clusters) {
                totalSse += cluster.getSumOfSquaredErrors();
            }
            return totalSse;
        } finally {
            clusterLock.readLock().unlock();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<PointCluster> iterator() {
        return clusters.iterator();
    }

}
