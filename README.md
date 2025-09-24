<div style="text-align: center;">
  <a href="https://commons.wikimedia.org/wiki/File:Polarlicht_2_kmeans_16_large.png">
    <img src="https://i.imgur.com/I17fXM9.png" alt="Project logo"
        title="The aurora borealis, after running k-means clustering with k = 16"/></a>
</div>

# The Project

This is the semester long project for a graduate class in
[cluster analysis](https://en.wikipedia.org/wiki/Cluster_analysis) at my
university. The code remains mostly unchanged since its final submission.
However, some changes have been made to protect my privacy and improve the
code quality.

ℹ️ **Note: This project uses zero dependencies by design.**

It was a project requirement that no libraries could be used, except for
the language's standard library. There is also no unit testing, as I did
not have enough time to write them. I relied on hints given by project
documents and the grade given to me by the professor to know if the code
was working correctly.

## 🧮 Building the Project

Building this project is simple. Just follow these steps:

1. Install [Git](https://git-scm.com/) if not done so already. 
2. Install the JDK. Builds of the OpenJDK can be found [here](https://adoptium.net/).
3. Open a terminal of your choice and run the commands below.

```bash
git clone https://github.com/whirvis/clusteral
cd clusteral
chmod +x ./gradlew # unix only
./gradlew build

# install to local Maven repository if desired
./gradlew publishToMavenLocal
```

## 📋 Dataset Format

Before running the clustering program, you will need to have a compatible
dataset file. The program expects each file to have a header line, and then
subsequent lines for each data point. An example is listed below.

```txt
250 7 5
0.63 0.47 0.48 0.51 0.82 0.84 0
0.23 0.48 0.48 0.59 0.88 0.89 0
0.34 0.49 0.48 0.58 0.85 0.80 0
0.43 0.40 0.48 0.58 0.75 0.78 0
0.46 0.61 0.48 0.48 0.86 0.87 0
0.27 0.35 0.48 0.51 0.77 0.79 0
0.52 0.39 0.48 0.65 0.71 0.73 0
0.29 0.47 0.48 0.71 0.65 0.69 0
0.55 0.47 0.48 0.57 0.78 0.80 0
0.12 0.67 0.48 0.74 0.58 0.63 0
(240 more points...)
```

Note the contents of the first line. The first number is how many data
points there are, the second number is how many dimensions each point has,
and the third number is the number of true clusters. **Keep in mind that
the true cluster is also one of the axes.** It is always the last axis of
each point.

## 📊 Running the Clustering Program

There is a main method in `ClusteringProgram`. It should be executable out
of the box with valid arguments. Parameters are listed in `ClusteringParams`.
An example of valid CLI arguments are given below.

```bash
java -jar clusteral.jar                                                    \
    dataset.txt       # F: name of the data file                           \
    3                 # K: number of clusters                              \
    1000              # I: maximum number of iterations                    \
    0.001             # T: convergence threshold                           \
    100               # R: number of runs                                  \
    random-selection  # M: K-means initialization method                   \
    min-max           # N: normalization type                              \
    calinski-harabasz # V: name of the cluster validator                   \
    average           # D: diameter calculation method                     \
    single-linkage    # L: linkage method, if necessary         (optional) \
    true              # C: random centroids on multiple nearest (optional) \
    stdout            # W: where to write program output        (optional)
```

Both classes are in the `io.whirvis.edu.clustering.cli` package.
