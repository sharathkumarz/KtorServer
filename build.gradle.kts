plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.litote.kmongo:kmongo-coroutine:4.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.2")
    implementation("io.ktor:ktor-serialization-jackson:2.3.0")

    // Jackson dependencies
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")

    // Status Pages feature
    implementation("io.ktor:ktor-server-status-pages:2.3.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}