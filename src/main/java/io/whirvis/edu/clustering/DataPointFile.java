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

import io.whirvis.edu.clustering.kmeans.KMeansInitMethod;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a file which contains data points.
 *
 * @see #open(File, boolean)
 */
@SuppressWarnings("unused")
public final class DataPointFile implements Iterable<DataPoint> {

    private final int pointCount;
    private final int dimensions;
    private final int trueClusterCount;
    private final List<DataPoint> points;
    private List<UnorderedPair> unorderedPointPairs;
    private final ReadWriteLock pointLock;

    private final List<Integer> indices;
    private final ReentrantLock indicesLock;
    private boolean indicesShuffleRequired;
    private int indicesShufflePos;

    private final ReadWriteLock baryCenterLock;
    private DataPoint baryCenter;

    private boolean normalized;

    /**
     * Constructs a blank {@code DataPointFile}.
     * <p>
     * This constructor is private so users cannot create empty data point
     * files. They must invoke one of the {@code open()} methods to make it
     * clear a file is being loaded.
     *
     * @param pointCount       the number of points.
     * @param dimensions       the number of dimensions in each point.
     * @param trueClusterCount the true cluster count.
     */
    private DataPointFile(
            int pointCount,
            int dimensions,
            int trueClusterCount) {
        this.pointCount = pointCount;
        this.dimensions = dimensions;
        this.trueClusterCount = trueClusterCount;
        this.points = new ArrayList<>();
        this.pointLock = new ReentrantReadWriteLock();

        /*
         * This may look like a waste of memory, but it isn't. It is used
         * by the getRandomClusters() method to quickly select both random
         * and unique centroids.
         *
         * The re-entrant lock ensures thread safety, it prevents multiple
         * threads from shuffling the indices list at the same time.
         */
        this.indices = new ArrayList<>();
        this.indicesLock = new ReentrantLock();
        this.indicesShuffleRequired = true;
        this.indicesShufflePos = 0;
        for (int i = 0; i < pointCount; i++) {
            indices.add(i);
        }

        this.baryCenterLock = new ReentrantReadWriteLock();
    }

    /**
     * Constructs a new {@code DataPointFile} from an existing file.
     * <p>
     * This constructor is private so users must invoke {@link #copy()} to
     * make it clear an instance is being copied.
     *
     * @param file the file to copy.
     */
    private DataPointFile(DataPointFile file) {
        this.pointCount = file.pointCount;
        this.dimensions = file.dimensions;
        this.trueClusterCount = file.trueClusterCount;
        this.points = new ArrayList<>();
        this.unorderedPointPairs = null;
        this.pointLock = new ReentrantReadWriteLock();

        this.pointLock.writeLock().lock();
        file.pointLock.readLock().lock();
        try {
            for (int i = 0; i < file.points.size(); i++) {
                DataPoint point = file.points.get(i);
                double[] axes = new double[point.getDimensions()];
                for (int j = 0; j < axes.length; j++) {
                    axes[j] = point.getAxis(j);
                }
                int trueClusterIndex = point.getTrueClusterIndex();
                points.add(new DataPoint(this, i, axes,
                        trueClusterIndex, pointLock));
            }
        } finally {
            file.pointLock.readLock().unlock();
            this.pointLock.writeLock().unlock();
        }

        /*
         * There is no need to copy the index shuffling from the original
         * data point file, so don't do that here.
         */
        this.indices = new ArrayList<>();
        this.indicesLock = new ReentrantLock();
        this.indicesShuffleRequired = true;
        this.indicesShufflePos = 0;
        for (int i = 0; i < pointCount; i++) {
            indices.add(i);
        }

        this.baryCenterLock = new ReentrantReadWriteLock();
        this.baryCenter = file.baryCenter;

        this.normalized = file.normalized;
    }

    /**
     * Creates a copy of this data point file.
     *
     * @return the newly created copy.
     */
    public DataPointFile copy() {
        return new DataPointFile(this);
    }

    /**
     * Returns the number of data points.
     *
     * @return the number of data points.
     */
    public int getPointCount() {
        return this.pointCount;
    }

    /**
     * Returns the dimensionality of each data point.
     * <p>
     * In this case, "dimensionality" refers to the number of axes each
     * data point in this file has. A point with an X, Y, Z axis is said
     * to have a dimensionality of three since it has three axes.
     *
     * @return the dimensionality of each data point.
     */
    public int getDimensions() {
        return this.dimensions;
    }

    /**
     * Returns if this file's true clusters are known.
     *
     * @return {@code true} if this file's true clusters are known,
     * {@code false} otherwise.
     * @see #getTrueClusterCount()
     * @see #getTrueClusters()
     */
    public boolean areTrueClustersKnown() {
        return this.trueClusterCount >= 0;
    }

    /**
     * Returns this file's number of true clusters.
     *
     * @return this file's number of true clusters, {@code -1} if the
     * true clusters are not known.
     * @see #areTrueClustersKnown()
     * @see #getTrueClusters()
     */
    public int getTrueClusterCount() {
        return this.trueClusterCount;
    }

    /**
     * Returns if this file can contain a data point.
     * <p>
     * A file can contain a data point so long as its dimensionality is the
     * same as the point.
     *
     * @param point the data point to check.
     * @return {@code true} if this file can contain {@code point},
     * {@code false} otherwise.
     * @see #getDimensions()
     */
    public boolean canContain(DataPoint point) {
        return point != null && this.getDimensions() == point.getDimensions();
    }

    /**
     * Returns if this file has a data point.
     *
     * @param point the data point to check for.
     * @return {@code true} if {@code point} is present in this file,
     * {@code false} otherwise.
     */
    public boolean hasPoint(DataPoint point) {
        pointLock.readLock().lock();
        try {
            return point != null && points.contains(point);
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns all data points in this file.
     * <p>
     * <b>Note:</b> The returned list is <i>unmodifiable.</i>
     * <p>
     * Since this class implements the {@link Iterable} interface, it is
     * also possible to iterate over each data point with an enhanced for
     * loop. For example:
     * <pre>
     * for (DataPoint point : pointFile) {
     *   &#47;* ... process points ... *&#47;
     * }
     * </pre>
     *
     * @return the data points in this file.
     */
    public List<DataPoint> getPoints() {
        pointLock.readLock().lock();
        try {
            return Collections.unmodifiableList(points);
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns the data point at the given index.
     *
     * @param index the index, starting from zero.
     * @return the data point at the given index.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the point
     *                                   count of this file.
     */
    public DataPoint getPoint(int index) {
        pointLock.readLock().lock();
        try {
            return points.get(index);
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns the nearest point to a given point.
     *
     * @param point the point whose nearest to search for.
     * @return the nearest point to {@code point} in this file.
     * @throws NullPointerException if {@code point} is {@code null}.
     * @see DataPoint#getNearestPoint(DataPoint, Iterable)
     */
    public DataPoint getNearestPoint(DataPoint point) {
        pointLock.readLock().lock();
        try {
            return point.getNearestPoint(points);
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns the farthest point from a given point.
     *
     * @param point the point whose farthest to search for.
     * @return the farthest point from {@code point} in this file.
     * @throws NullPointerException if {@code point} is {@code null}.
     * @see DataPoint#getFarthestPoint(DataPoint, Iterable)
     */
    public DataPoint getFarthestPoint(DataPoint point) {
        pointLock.readLock().lock();
        try {
            return point.getFarthestPoint(points);
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Returns all unordered pairs of points in this file.
     * <p>
     * <b>Note:</b> This method can take quite some time to generate the
     * unordered pairs. It has a time complexity of {@code O(n^2)}. As such,
     * the result is cached for later calls to this method; allowing them
     * to have {@code O(1)} time complexity. As a result, the returned list
     * is <i>unmodifiable</i>.
     *
     * @return all unordered pairs of points in this file.
     */
    public List<UnorderedPair> getUnorderedPointPairs() {
        if (unorderedPointPairs != null) {
            return this.unorderedPointPairs;
        }

        pointLock.writeLock().lock();
        try {
            List<UnorderedPair> pairs = new ArrayList<>();
            for (DataPoint outer : points) {
                for (DataPoint inner : points) {
                    if (outer == inner) {
                        continue; /* don't pair with ourselves */
                    }
                    UnorderedPair pair = new UnorderedPair(outer, inner);
                    if (!pairs.contains(pair)) {
                        pairs.add(pair);
                    }
                }
            }
            this.unorderedPointPairs = Collections.unmodifiableList(pairs);
        } finally {
            pointLock.writeLock().unlock();
        }

        /* sanity check for unordered pair generation */
        double pointCount = points.size(); /* we must use doubles here */
        double expectedSize = pointCount * ((pointCount - 1.0f) / 2.0f);
        if (unorderedPointPairs.size() != Math.round(expectedSize)) {
            String msg = "Unexpected size for unordered pair list";
            throw new RuntimeException(msg);
        }

        return this.unorderedPointPairs;
    }

    /**
     * Creates a group of singleton point clusters.
     *
     * @param centroidIndices the indices of the data points which shall be
     *                        the centroid of their corresponding cluster.
     * @return the newly created point cluster group.
     * @throws NullPointerException      if {@code centroidIndices}
     *                                   is {@code null}.
     * @throws IllegalArgumentException  if {@code centroidIndices} does not
     *                                   have at least one element;
     *                                   if {@code centroidIndices} contains
     *                                   any duplicate values.
     * @throws IndexOutOfBoundsException if any of the values within the
     *                                   {@code centroidIndices} array are
     *                                   negative or greater than or equal
     *                                   to the point count of this file.
     * @see #getRandomSelectionClusters(int)
     */
    public PointClusters getClusters(int... centroidIndices) {
        Objects.requireNonNull(centroidIndices,
                "centroidIndices cannot be null");

        /*
         * It makes no sense to invoke this method without providing any
         * centroid indices. Assume this was a mistake by the user.
         */
        if (centroidIndices.length == 0) {
            String msg = "centroidIndices must have at least one element";
            throw new IllegalArgumentException(msg);
        }

        /*
         * For the sake of simplicity, we do not allow the user to create
         * multiple point clusters with the same centroid. The code below
         * checks for duplicates by adding them all to a set. If the add()
         * method returns false, it means the element was already present
         * and is therefore a duplicate.
         *
         * If necessary, this restriction will be removed in the future.
         */
        Set<Integer> seenIndices = new HashSet<>();
        for (int centroidIndex : centroidIndices) {
            if (!seenIndices.add(centroidIndex)) {
                String msg = "Cannot have duplicate indices";
                throw new IllegalArgumentException(msg);
            }
        }

        pointLock.readLock().lock();
        try {
            PointClusters group = new PointClusters(this);
            for (int centroidIndex : centroidIndices) {
                DataPoint centroid = this.getPoint(centroidIndex);
                group.addCluster(centroid);
            }
            return group;
        } finally {
            pointLock.readLock().unlock();
        }
    }

    /**
     * Validates the given number of clusters.
     * <p>
     * It makes no sense to request for no clusters. Furthermore, if
     * the requested count is greater than the point count, it will be
     * <i>impossible</i> to generate clusters with unique centroids.
     * As such, assume these were mistakes by the user.
     *
     * @param count the count to validate.
     * @throws IllegalArgumentException if {@code count} is not positive;
     *                                  if {@code count} is greater than
     *                                  the point count of this file.
     */
    private void validateClusterCount(int count) {
        if (count <= 0) {
            String msg = "count must be positive";
            throw new IllegalArgumentException(msg);
        } else if (count > pointCount) {
            String msg = "count cannot be greater than pointCount";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the true clusters of this file.
     * <p>
     * <b>Note:</b> This method generates a new instance of
     * {@link PointClusters} each time this method is called.
     *
     * @return the true clusters of this file.
     * @throws IllegalStateException if the true clusters of this file are
     *                               not known.
     * @see #areTrueClustersKnown()
     * @see #getTrueClusterCount()
     */
    public PointClusters getTrueClusters() {
        if (!this.areTrueClustersKnown()) {
            String msg = "The true cluster count is not known";
            throw new IllegalStateException(msg);
        }

        PointClusters clusters = new PointClusters(this);
        for (int i = 0; i < trueClusterCount; i++) {
            clusters.addCluster();
        }

        for (DataPoint point : points) {
            int index = point.getTrueClusterIndex();
            try {
                PointCluster cluster = clusters.getCluster(index);
                cluster.addPoint(point);
            } catch (IndexOutOfBoundsException e) {
                String msg = "The true cluster index " + index + " for point "
                        + " (" + point + ") does not exist in file";
                throw new RuntimeException(msg, e);
            }
        }

        return clusters;
    }

    /**
     * Creates a group of singleton point clusters. The centroids for these
     * clusters are selected at random from data points in the file.
     * <p>
     * This method ensures that all selected points are unique (that being,
     * no points are selected twice). If it is not possible to select all
     * unique points, an exception will be thrown.
     *
     * @param count the amount of pointer clusters to create.
     * @return the newly created point clusters group.
     * @throws IllegalArgumentException if {@code count} is not positive;
     *                                  if {@code count} is greater than
     *                                  the point count of this file.
     * @see #getClusters(int...)
     * @see #getClusters(KMeansInitMethod, int)
     */
    public PointClusters getRandomSelectionClusters(int count) {
        this.validateClusterCount(count);

        /*
         * To comply with the requirements of getClusters(), this method
         * ensures no duplicate indices are selected. This is accomplished
         * by shuffling the list of indices (instantiated at construction)
         * and then returning the values up to the specified count.
         *
         * If necessary, this restriction will be removed in the future.
         */
        indicesLock.lock();
        int[] centroidIndices;
        try {
            /*
             * To prevent needless shuffling (which can be expensive and
             * time-consuming for large data sets), this method makes sure
             * to use up the entire list before shuffling it again.
             */
            if (indicesShufflePos + count >= indices.size()) {
                this.indicesShuffleRequired = true;
                this.indicesShufflePos = 0;
            }

            if (indicesShuffleRequired) {
                Collections.shuffle(indices);
                this.indicesShuffleRequired = false;
            }

            centroidIndices = new int[count];
            for (int i = 0; i < centroidIndices.length; i++) {
                centroidIndices[i] = indices.get(indicesShufflePos);
                this.indicesShufflePos += 1;
            }
        } finally {
            indicesLock.unlock();
        }

        return this.getClusters(centroidIndices);
    }

    /**
     * Creates a group of clusters which are made up of randomly selected
     * points. After all points are added to their clusters, the centroids
     * are set to the mean of each clusters' points.
     * <p>
     * This method ensures that all selected points are unique (that being,
     * no points are selected twice). If it is not possible to select all
     * unique points, an exception will be thrown.
     *
     * @param count the amount of pointer clusters to create.
     * @return the newly created point clusters group.
     * @throws IllegalArgumentException if {@code count} is not positive;
     *                                  if {@code count} is greater than
     *                                  the point count of this file.
     * @see #getClusters(KMeansInitMethod, int)
     */
    public PointClusters getRandomPartitionClusters(int count) {
        this.validateClusterCount(count);

        PointClusters group = new PointClusters(this);
        for (int i = 0; i < count; i++) {
            group.addCluster();
        }

        Random random = new Random();
        for (DataPoint point : points) {
            int clusterIndex = random.nextInt(count);
            PointCluster cluster = group.getCluster(clusterIndex);
            cluster.addPoint(point);
        }

        for (PointCluster cluster : group) {
            DataPoint centroid = cluster.getMean(false);
            cluster.clearPoints();
            cluster.setCentroid(centroid);
        }

        return group;
    }

    /**
     * Creates a group of clusters with the centroid of every cluster
     * after the first being the data point with the greatest squared
     * Euclidean distance to the nearest previously selected centers.
     *
     * @param initialIndex the index of the data point to use as the
     *                     initial center.
     * @param count        the amount of pointer clusters to create.
     * @return the newly created point clusters group.
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *                                   greater than or equal to the point
     *                                   count of this file.
     * @throws IllegalArgumentException  if {@code count} is not positive;
     *                                   if {@code count} is greater than
     *                                   the point count of this file.
     * @see #getClusters(KMeansInitMethod, int)
     */
    public PointClusters getMaximinClusters(int initialIndex, int count) {
        this.validateClusterCount(count);

        /*
         * The first point we can use in this method is arbitrary (that
         * being, it is up to us the developer to choose it). This method
         * allows the user to specify the initial centroid as a result.
         */
        DataPoint initial = this.getPoint(initialIndex);
        PointClusters clusters = new PointClusters(this);
        clusters.addCluster(initial);

        /*
         * Now that we've chosen our initial point, we can choose the rest
         * of our points. Since we've already chosen one point, we're going
         * to set the initial value for i to 1.
         *
         * As stated in the document for phase, the next point shall be the
         * data point with the greatest distance to its nearest center. That
         * being, the point with the greatest value of: min(d(x,c1), d(x,c2),
         * ..., d(x, ci-1)).
         */
        for (int i = 1; i < count; i++) {
            /*
             * Therefore, to get the next point, we must go through every
             * data point and get its distance from all the current centers.
             * We take the minimum value of these distances, and then add
             * it to a map where the key is the distance and the value is
             * the data point.
             */
            Map<Double, DataPoint> nearestCenterDistances = new HashMap<>();
            for (DataPoint point : points) {
                List<Double> distances = new ArrayList<>();
                for (PointCluster cluster : clusters) {
                    DataPoint centroid = cluster.getCentroid();
                    distances.add(centroid.squaredError(point));
                }
                Double minDistance = MinMax.getMin(distances);
                nearestCenterDistances.put(minDistance, point);
            }

            /*
             * Once we have done this for all data points, we will take the
             * point which has the greatest distance from the nearest center
             * and add it as the next center. Note that we use a map as we
             * want the point with the greatest distance, and not the point
             * with the greatest value.
             */
            Double maxDistance = MinMax.getMax(nearestCenterDistances.keySet());
            DataPoint farthest = nearestCenterDistances.get(maxDistance);
            clusters.addCluster(farthest);
        }

        return clusters;
    }

    /**
     * Creates a group of clusters with the centroid of every cluster
     * after the first being the data point with the greatest squared
     * Euclidean distance to the nearest previously selected centers.
     * <p>
     * For this implementation of the Maximin method, the middle data
     * point is used. That being, in a data point file with 1200 points,
     * the 600th data point would be used as the first center.
     *
     * @param count the amount of pointer clusters to create.
     * @return the newly created point clusters group.
     * @throws IllegalArgumentException if {@code count} is not positive;
     *                                  if {@code count} is greater than
     *                                  the point count of this file.
     * @see #getClusters(KMeansInitMethod, int)
     */
    public PointClusters getMaximinClusters(int count) {
        int middleIndex = (points.size() - 1) / 2;
        return this.getMaximinClusters(middleIndex, count);
    }

    /**
     * Creates a cluster group with the given initialization method.
     *
     * @param method the initialization method to use.
     * @param count  the amount of pointer clusters to create.
     * @return the newly created point clusters group.
     * @throws IllegalArgumentException if {@code count} is not positive;
     *                                  if {@code count} is greater than
     *                                  the point count of this file.
     */
    public PointClusters getClusters(KMeansInitMethod method, int count) {
        switch (method) {
            case RANDOM_SELECTION:
                return this.getRandomSelectionClusters(count);
            case RANDOM_PARTITION:
                return this.getRandomPartitionClusters(count);
            case MAXIMIN:
                return this.getMaximinClusters(count);
            default:
                String msg = "This is a bug";
                throw new RuntimeException(msg);
        }
    }

    /**
     * Returns the centroid of this file.
     *
     * @return the centroid of this file.
     * @see PointCluster#getCentroid()
     */
    public DataPoint getBaryCenter() {
        baryCenterLock.readLock().lock();
        try {
            if (baryCenter != null) {
                return this.baryCenter;
            }
        } finally {
            baryCenterLock.readLock().unlock();
        }

        baryCenterLock.writeLock().lock();
        try {
            /* in case another thread calculated it first */
            if (baryCenter != null) {
                return this.baryCenter;
            }

            double[] axisTotals = new double[dimensions];
            for (DataPoint point : points) {
                for (int i = 0; i < axisTotals.length; i++) {
                    axisTotals[i] += point.getAxis(i);
                }
            }

            double[] axisMeans = new double[axisTotals.length];
            for (int i = 0; i < axisMeans.length; i++) {
                axisMeans[i] = axisTotals[i] / points.size();
            }

            this.baryCenter = new DataPoint(axisMeans);
            return this.baryCenter;
        } finally {
            baryCenterLock.writeLock().unlock();
        }
    }

    /**
     * Returns the lowest and highest values for a given axis.
     * <p>
     * The source of axis values are this file's data points.
     * <p>
     * <b>Note:</b> Unlike other methods, this does not use the read lock;
     * since the write lock is used by {@link #normalize(NormalizationType)}.
     * If this were to use the read lock, the program would stall.
     *
     * @param axis the axis whose values to compare.
     * @return the lowest and highest values for a given axis.
     * @throws IndexOutOfBoundsException if {@code axis} is negative or
     *                                   greater than or equal to the
     *                                   dimension count of this file.
     */
    private MinMaxPair<Double> getAxisMinAndMax(int axis) {
        List<Double> values = new ArrayList<>();
        for (DataPoint point : points) {
            values.add(point.getAxis(axis));
        }
        return MinMax.getMinAndMax(values);
    }

    /**
     * Returns a Z-score normalization calculator for a given axis.
     * <p>
     * The source of axis values are this file's data points.
     * <p>
     * <b>Note:</b> Unlike other methods, this does not use the read lock;
     * since the write lock is used by {@link #normalize(NormalizationType)}.
     * If this were to use the read lock, the program would stall.
     *
     * @param axis the axis whose values to compare.
     * @return the Z-score normalizer for the given axis.
     * @throws IndexOutOfBoundsException if {@code axis} is negative or
     *                                   greater than or equal to the
     *                                   dimension count of this file.
     */
    private ZScoreNormalizer getAxisZScoreNormalizer(int axis) {
        List<Double> values = new ArrayList<>();
        for (DataPoint point : points) {
            values.add(point.getAxis(axis));
        }
        return ZScore.getNormalizer(values);
    }

    /**
     * Returns if the points in this file have been normalized.
     *
     * @return {@code true} if the points in this file have been
     * normalized, {@code false} otherwise.
     */
    public boolean isNormalized() {
        pointLock.readLock().lock();
        try {
            return this.normalized;
        } finally {
            pointLock.readLock().unlock();
        }
    }

    private void normalizeWithMinMax() {
        for (int i = 0; i < dimensions; i++) {
            MinMaxPair<?> pair = this.getAxisMinAndMax(i);
            for (DataPoint point : points) {
                double value = point.getAxis(i);
                double normalized = pair.normalize(value);
                point.setAxis(i, normalized);
            }
        }
    }

    private void normalizeWithZScore() {
        for (int i = 0; i < dimensions; i++) {
            ZScoreNormalizer normalizer = this.getAxisZScoreNormalizer(i);
            for (DataPoint point : points) {
                double value = point.getAxis(i);
                double normalized = normalizer.normalize(value);
                point.setAxis(i, normalized);
            }
        }
    }

    /**
     * Normalizes every data point in this file.
     *
     * @param type determines how the data points in this file shall be
     *             normalized.
     * @throws NullPointerException  if {@code type} is {@code null}.
     * @throws IllegalStateException if the data points in this file have
     *                               already been normalized.
     */
    public void normalize(NormalizationType type) {
        Objects.requireNonNull(type, "type cannot be null");

        pointLock.writeLock().lock();
        try {
            if (normalized) {
                String msg = "Points already normalized";
                throw new IllegalStateException(msg);
            }

            switch (type) {
                case NO_OP:
                    break; /* do nothing */
                case MIN_MAX:
                    this.normalizeWithMinMax();
                    break; /* do not fall through */
                case Z_SCORE:
                    this.normalizeWithZScore();
                    break; /* do not fall through */
                default:
                    String msg = "This is a bug";
                    throw new RuntimeException(msg);
            }

            this.normalized = true;
        } finally {
            pointLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<DataPoint> iterator() {
        return points.iterator();
    }

    @Override
    public int hashCode() {
        pointLock.readLock();
        try {
            return Objects.hash(pointCount, dimensions, points);
        } finally {
            pointLock.readLock().unlock();
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

        DataPointFile that = (DataPointFile) obj;

        this.pointLock.readLock().lock();
        that.pointLock.readLock().lock();
        try {
            return this.pointCount == that.pointCount
                    && this.dimensions == that.dimensions
                    && Objects.equals(this.points, that.points);
        } finally {
            that.pointLock.readLock().unlock();
            this.pointLock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        pointLock.readLock().lock();
        try {
            return StrUtils.getStrJoiner(this)
                    .add("pointCount=" + pointCount)
                    .add("dimensions=" + dimensions)
                    .add("points=" + points)
                    .toString();
        } finally {
            pointLock.readLock().unlock();
        }
    }

    private static final String REGEX_WHITESPACE = "\\s+";
    private static final int POINT_COUNT_INDEX = 0;
    private static final int DIMENSIONS_INDEX = 1;
    private static final int TRUE_CLUSTER_COUNT_INDEX = 2;

    private static List<String> splitAndFilterOutEmptyStrings(
            String str, String regex) {
        String[] unfiltered = str.split(regex);

        /*
         * Sometimes we'll get empty strings after splitting our regex,
         * which can throw off the parser. This code takes every string
         * we received from calling split(), and then only adds in the
         * ones which still have content after being trimmed.
         */
        List<String> filtered = new ArrayList<>();
        for (String element : unfiltered) {
            String trimmedStr = element.trim();
            if (!trimmedStr.isEmpty()) {
                filtered.add(trimmedStr);
            }
        }

        return filtered;
    }

    /**
     * Reads the requested amount of integers on a line read from a
     * {@code BufferedReader}, with said line being split by the given
     * regex expression.
     *
     * @param reader the reader to read from.
     * @param regex  the regex to split the read line by.
     * @param count  the amount of expected integers.
     * @return the parsed integers.
     * @throws EOFException          if the end of the stream has been reached.
     * @throws IOException           if the amount of integers encountered
     *                               on the read line is not the exact amount
     *                               requested.
     * @throws NumberFormatException if any of the numbers encountered on the
     *                               read line are not a valid integer.
     */
    public static int[] readInts(
            BufferedReader reader,
            String regex,
            int count) throws IOException {
        String str = reader.readLine();
        if (str == null) {
            throw new EOFException();
        }

        List<String> filtered = splitAndFilterOutEmptyStrings(str, regex);

        /*
         * Ensure the exact amount of requested integers are on the read
         * line. Even if there are more than enough, it's likely something
         * is wrong with the file. If necessary, a more lenient version of
         * this method will be added in the future.
         */
        if (filtered.size() != count) {
            throw new IOException("Expected " + count + " integers"
                    + ", got " + filtered.size() + " instead");
        }

        int[] values = new int[count];
        for (int i = 0; i < values.length; i++) {
            String valueStr = filtered.get(i);
            values[i] = Integer.parseInt(valueStr);
        }

        filtered.clear(); /* garbage collection */

        return values;
    }

    /**
     * Reads the requested amount of doubles on a line read from a
     * {@code BufferedReader}, with said line being split by the given
     * regex expression.
     *
     * @param reader the reader to read from.
     * @param regex  the regex to split the read line by.
     * @param count  the amount of expected doubles.
     * @return the parsed doubles.
     * @throws EOFException          if the end of the stream has been reached.
     * @throws IOException           if the amount of doubles encountered
     *                               on the read line is not the exact amount
     *                               requested.
     * @throws NumberFormatException if any of the numbers encountered on the
     *                               read line are not a valid double.
     */
    public static double[] readDoubles(
            BufferedReader reader,
            String regex,
            int count) throws IOException {
        String str = reader.readLine();
        if (str == null) {
            throw new EOFException();
        }

        List<String> filtered = splitAndFilterOutEmptyStrings(str, regex);

        /*
         * Ensure the exact amount of requested doubles are on the read
         * line. Even if there are more than enough, it's likely something
         * is wrong with the file. If necessary, a more lenient version of
         * this method will be added in the future.
         */
        if (filtered.size() != count) {
            throw new IOException("Expected " + count + " doubles"
                    + ", got " + filtered.size() + " instead");
        }

        double[] values = new double[count];
        for (int i = 0; i < values.length; i++) {
            String valueStr = filtered.get(i);
            values[i] = Double.parseDouble(valueStr);
        }

        filtered.clear(); /* garbage collection */

        return values;
    }

    /**
     * Implementation for {@link #open(File, boolean)}.
     * <p>
     * This exists as its own function so the wrapper can utilize Java's
     * try-catch-finally system. This ensures resources are freed even if
     * an error occurs.
     *
     * @param reader          the buffer to read from.
     * @param hasTrueClusters {@code true} if there is an extra column for
     *                        each data point which indicates which cluster
     *                        it truly belongs to, {@code false} otherwise.
     * @return the loaded data point file.
     * @throws IOException if an I/O error occurs.
     */
    private static DataPointFile openViaBufferedReader(
            BufferedReader reader,
            boolean hasTrueClusters) throws IOException {
        /*
         * Before we can load the data points into the object, we must first
         * read the point specifications at the top of the file. After we do
         * this, we will validate the specifications.
         */
        int pointCount;
        int dimensions;
        int trueClusterCount = -1;
        try {
            int columns = 2 + (hasTrueClusters ? 1 : 0);
            int[] dataSpecs = readInts(reader, REGEX_WHITESPACE, columns);
            pointCount = dataSpecs[POINT_COUNT_INDEX];
            dimensions = dataSpecs[DIMENSIONS_INDEX];

            /*
             * The true cluster index is counted as one of the data's
             * dimensions. So to make our lives easier we just subtract
             * one from the dimension count if the caller indicates the
             * true cluster index is present in the file.
             */
            if (hasTrueClusters) {
                dimensions -= 1;
                trueClusterCount = dataSpecs[TRUE_CLUSTER_COUNT_INDEX];
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid data point specifications");
        } catch (EOFException e) {
            throw new IOException("Missing data point specifications");
        }

        /*
         * If the point count is negative, or the number of dimensions is
         * not positive, then no useful data can be stored in the object.
         * As such, the file should be considered malformed.
         */
        if (pointCount < 0) {
            throw new IOException("pointCount cannot be negative");
        } else if (dimensions <= 0) {
            throw new IOException("dimensions must be positive");
        }

        DataPointFile pointFile = new DataPointFile(
                pointCount, dimensions, trueClusterCount);

        /*
         * Now that we have loaded and validated the point specifications,
         * we can load all the points into the object. An error still may
         * occur here, however.
         */
        int loadedPointCount = 0;
        try {
            for (int i = 0; i < pointCount; i++) {
                int columns = dimensions + (hasTrueClusters ? 1 : 0);
                double[] data = readDoubles(reader, REGEX_WHITESPACE, columns);

                /*
                 * Also, just to be safe, ensure that the true cluster index
                 * cast to an integer is the same as the value read from the
                 * file (the index is read as a double as it comes with all
                 * the other points, which should be doubles).
                 */
                int trueClusterIndex = -1;
                if (hasTrueClusters) {
                    trueClusterIndex = (int) data[columns - 1];
                    if (data[columns - 1] != trueClusterIndex) {
                        throw new IOException("Invalid true cluster index");
                    }
                }

                double[] axes = new double[dimensions];
                System.arraycopy(data, 0, axes, 0, dimensions);

                pointFile.points.add(new DataPoint(
                        pointFile, i, axes, trueClusterIndex,
                        pointFile.pointLock));

                loadedPointCount += 1;
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid data points", e);
        } catch (EOFException e) {
            throw new IOException("Expected " + pointCount + " points"
                    + ", got" + loadedPointCount + " instead");
        }

        return pointFile;
    }

    /**
     * Opens a file and loads it into a {@code DataPointFile}.
     * <p>
     * <b>Requirements:</b> These files must have at least one line, with
     * the first line being the number of data points and their number of
     * axes. Each line after must contain points specified as real numbers,
     * (interpreted as {@code double}), with each number being separated
     * by whitespace.
     * <p>
     * <b>Examples</b>
     * <p>
     * An example of a valid file can be seen below.
     * <pre>
     *     5 3
     *     5.1 3.5 1.4
     *     4.9 3 1.4
     *     4.7 3.2 1.3
     *     4.6 3.1 1.5
     *     5 3.6 1.4
     * </pre>
     * As before, the first line contains the number of points ({@code 5})
     * and also the number of axes for each point ({@code 3}). Each line
     * afterward contains the values for each axis which are also separated
     * by whitespace.
     *
     * @param file            the file to open.
     * @param hasTrueClusters {@code true} if there is an extra column for
     *                        each data point which indicates which cluster
     *                        it truly belongs to, {@code false} otherwise.
     * @return the loaded data point file.
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @see #getPoints()
     * @see #getPointCount()
     * @see #getPoint(int)
     */
    public static DataPointFile open(
            File file,
            boolean hasTrueClusters) throws IOException {
        try (FileReader reader = new FileReader(file);
             BufferedReader buffered = new BufferedReader(reader)) {
            return openViaBufferedReader(buffered, hasTrueClusters);
        }
    }

    /**
     * Opens a file and loads it into a {@code DataPointFile}.
     * <p>
     * <b>Requirements:</b> These files must have at least one line, with
     * the first line being the number of data points and their number of
     * axes. Each line after must contain points specified as real numbers,
     * (interpreted as {@code double}), with each number being separated
     * by whitespace.
     * <p>
     * <b>Examples</b>
     * <p>
     * An example of a valid file can be seen below.
     * <pre>
     *     5 3
     *     5.1 3.5 1.4
     *     4.9 3 1.4
     *     4.7 3.2 1.3
     *     4.6 3.1 1.5
     *     5 3.6 1.4
     * </pre>
     * As before, the first line contains the number of points ({@code 5})
     * and also the number of axes for each point ({@code 3}). Each line
     * afterward contains the values for each axis which are also separated
     * by whitespace.
     *
     * @param path            the path of the file to open.
     * @param hasTrueClusters {@code true} if there is an extra column for
     *                        each data point which indicates which cluster
     *                        it truly belongs to, {@code false} otherwise.
     * @return the loaded data point file.
     * @throws NullPointerException if {@code file} is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @see #getPoints()
     * @see #getPointCount()
     * @see #getPoint(int)
     */
    public static DataPointFile open(
            String path,
            boolean hasTrueClusters) throws IOException {
        return open(new File(path), hasTrueClusters);
    }

}
