import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.31")
    id("com.github.johnrengelman.shadow").version("4.0.2")
    id("com.gradle.build-scan").version("2.1")
    application
}

val kotlinVersion = "1.3.31"
val tornadoFxVersion = "1.7.17"

repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

// compile bytecode to java 8 (default is java 6)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compile("no.tornado:tornadofx:$tornadoFxVersion")

    compile("com.google.code.gson:gson:2.8.5")

    compile("com.github.cretz.kastree:kastree-ast-common:0.4.0")
    compile("com.github.cretz.kastree:kastree-ast-jvm:0.4.0")
    compile("com.github.cretz.kastree:kastree-ast-psi:0.4.0")

    compile("org.jetbrains.kotlin:kotlin-reflect:1.3.30")

    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.1.1")
    
    testCompile("io.github.microutils:kotlin-logging:1.4.4")

    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")

    testCompile("org.testfx:testfx-core:4.0.13-alpha")
    testCompile("org.testfx:testfx-junit:4.0.13-alpha")

    testCompile("org.mockito:mockito-core:1.10.19")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0") {
        exclude("exclude group: 'org.mockito'")
    }

    // Use Monocle for headless testfx
    testCompile("org.testfx:openjfx-monocle:8u76-b04")
}

application {
    mainClassName = "com.github.hd.tornadofxsuite.app.TornadoFXSuite"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
