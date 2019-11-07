import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("email.haemmerle.baseplugin").version("0.0.5")
    kotlin("jvm") version "1.3.50"
}

group = "email.haemmerle.appkonfig"
description = "RESTful HTTP Client Library"

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

`email-haemmerle-base`{
    username = "mhmmerle"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.beust:klaxon:3.0.1")

    testImplementation("org.assertj:assertj-core:3.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.1")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}