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
9 12 3
18.93 1.75 1.32 15.96 11.65 1.07 14.11 14.67 6.53 1.11 1190 0
19.36 2.01 1.48 15.71 13.57 1.39 16.76 14.12 0.08 4.96 1226 0
10.12 1.42 6.33 10.54 16.73 2.16 17.19 13.42 1.85 4.89 1335 0
18.65 2.65 3.24 12.54 17.55 2.92 14.79 15.47 1.78 3.50 1402 1
11.12 1.70 9.84 13.35 17.36 0.49 18.13 15.32 3.71 7.15 1171 1
12.88 3.83 6.32 17.67 14.14 0.75 12.94 14.88 4.54 3.54 1120 1
15.00 1.26 3.52 16.37 18.89 1.05 12.93 11.90 1.70 1.97 1430 2
16.04 3.51 7.55 12.82 12.83 1.97 18.35 18.53 6.16 5.38 1304 2
14.47 2.75 2.72 11.96 12.89 1.54 12.41 19.54 0.08 4.10 1216 2
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
