import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    kotlin("kapt") version "1.7.21"
    id("me.champeau.jmh") version "0.6.8"
}

group = "ru.itmo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework:spring-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    implementation("info.picocli:picocli-spring-boot-starter:4.7.0")

    implementation("edu.stanford.nlp:stanford-corenlp:4.5.1")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.1:models")
    implementation("org.tensorflow:tensorflow:1.4.0")
    implementation("com.h2database:h2-mvstore:2.1.214")

    // djl
    implementation(platform("ai.djl:bom:${findProperty("djlVersion")}"))
    implementation("ai.djl:api")
    implementation("ai.djl.pytorch:pytorch-engine")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jmh {
    includes.set(listOf(".*")) // include pattern (regular expression) for benchmarks to be executed
    warmupIterations.set(2) // Number of warmup iterations to do.
    iterations.set(2) // Number of measurement iterations to do.
    fork.set(2) // How many times to forks a single benchmark. Use 0 to disable forking altogether
    zip64.set(true) // is used for big archives (more than 65535 entries)
}
