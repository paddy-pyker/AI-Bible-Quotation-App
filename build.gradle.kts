plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ai.bible"
version = "0.0.1-SNAPSHOT"

val dspVersion = "2.5"
val loggerVersion = "5.1.4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    flatDir {
        dirs("lib")
    }
    maven {
        url = uri("https://mvn.0110.be/releases")
    }
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("be.tarsos.dsp:core:$dspVersion")
    implementation("be.tarsos.dsp:jvm:$dspVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:$loggerVersion")
    implementation(":sherpa-onnx-v1.10.44-java21")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
