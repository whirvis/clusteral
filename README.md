<div style="text-align: center;">
  <a href="https://commons.wikimedia.org/wiki/File:Polarlicht_2_kmeans_16_large.png">
    <img src="https://i.imgur.com/I17fXM9.png" alt="Project logo"
        title="The aurora borealis, after running k-means clustering with k = 16"/></a>
</div>

# The Project
This is the semester long project for a graduate class in
[cluster analysis](https://en.wikipedia.org/wiki/Cluster_analysis) at my
university. The code remains unchanged since it's final submission, except
for changes to protect my privacy.

ℹ️ **Note: This project uses zero dependencies by design.**

It was a project requirement that no libraries could be used, except for
the language's standard library. There is also no unit testing, as I did
not have enough time to write them. I relied on hints given by project
documents and the grade given to me by the professor to know if the code
was working correctly.

## 🧮 Building the Project

Building this project is simple, and can be done  following the
instructions below:

1. Install [Git](https://git-scm.com/) if not done so already.
2. Install the JDK. Builds of the OpenJDK can be found [here](https://adoptium.net/).
3. Open a terminal of your choice and run the following commands:

```bash
git clone https://github.com/whirvis/clusteral
cd clusteral
chmod +x ./gradlew # unix only
./gradlew build

# install to local Maven repository if desired
./gradlew publishToMavenLocal
```

## 📊 Running the Clustering Program

There is a main method in `ClusteringProgram`. It should be executable out of
the box, so long as valid arguments are given. The parameters are documented
in `ClusteringParams`. An example of valid CLI arguments would be:

```bash
java -jar clusteral.jar                                                    \
    dataset.txt       # F: name of the data file                           \
    3                 # K: number of clusters                              \
    1000              # I: maximum number of iterations                    \
    0.001             # T: convergence threshold                           \
    100               # R: number of runs                                  \
    human-readable    # O: output mode                                     \
    random-selection  # M: K-means initialization method                   \
    min-max           # N: normalization type                              \
    calinski-harabasz # V: name of the cluster validator                   \
    average           # D: diameter calculation method                     \
    single-linkage    # L: linkage method, if necessary         (optional) \
    true              # C: random centroids on multiple nearest (optional) \
    stdout            # W: where to write program output        (optional)
```

Both classes are in the `io.whirvis.edu.clustering.cli` package.