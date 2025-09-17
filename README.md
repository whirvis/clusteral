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

There is a main method in `ClusterProgram`.

Before you can run it, you will need to assign non-null values to
`outputMode`, `normalizationType`, `initMethod`, and `validator`
(project documents required that they be hardcoded and not specified
via the command line). After that, it should run out of the box when
given valid arguments via the command line. The program's parameters
are described in `ClusterParams`.

Both classes are in the `io.whirvis.edu.clustering.cli` package.
