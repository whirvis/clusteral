plugins {
    id("application")
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(project(":cli:args"))
    implementation(project(":api"))
    implementation(project(":api:kmeans"))
    implementation(project(":api:validator"))
}

application {
    mainClass = "io.whirvis.edu.clustering.cli.ClusteringProgram"
}

tasks.withType<JavaExec> {
    workingDir = rootProject.projectDir

    args = listOf(
        "dataset.txt"      , /* F: name of the data file                */
        "3"                 , /* K: number of clusters                   */
        "1000"              , /* I: maximum number of iterations         */
        "0.001"             , /* T: convergence threshold                */
        "100"               , /* R: number of runs                       */
        "random-selection"  , /* M: K-means initialization method        */
        "min-max"           , /* N: normalization type                   */
        "calinski-harabasz" , /* V: name of the cluster validator        */
        "average"           , /* D: diameter calculation method          */
        "single-linkage"    , /* L: linkage method, if necessary         */
        "true"              , /* C: random centroids on multiple nearest */
        "stdout"            , /* W: where to write program output        */
    )
}

tasks.shadowJar {
    archiveBaseName = "clusteral"
    archiveClassifier.unset()
}
