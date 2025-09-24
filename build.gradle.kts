plugins {
    id("java")
}

group = "io.whirvis"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

subprojects {
    apply(plugin = "java")
}
