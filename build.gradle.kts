plugins {
    id ("org.jetbrains.kotlin.jvm").version("1.3.20")
    id ("com.adcubum.tool.versioning-plugin").version("1.0.1")
    id ("com.adcubum.gradle.canopus-gradle-publish").version("0.0.2")
}

group = "com.adcubum.library"

repositories {
    mavenLocal()
    maven("http://artifact.devres.internal.adcubum.com/artifactory/adcubum-repo/")
}

adcubumpublish {
    mavenReleasePublishUrl = "http://artifact.devres.internal.adcubum.com/artifactory/adcubum-library-releases/"
    mavenSnapshotPublishUrl = "http://artifact.devres.internal.adcubum.com/artifactory/adcubum-library-snapshots/"
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.beust:klaxon:3.0.1")

    testImplementation("org.assertj:assertj-core:3.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.1")
}


tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}