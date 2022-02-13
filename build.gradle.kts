import groovy.json.JsonSlurper
import org.jetbrains.kotlin.utils.addToStdlib.cast

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mapboxDownloadKey: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

group = "co.funpeople"
version = "0.0.1"

application {
    mainClass.set("co.funpeople.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven {
        url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        authentication { create<BasicAuthentication>("basic") }
        credentials {
            username = "mapbox"
            password = mapboxKey()
        }
    }
    google()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-compression:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
    implementation("com.arangodb:arangodb-java-driver:6.16.0")
    implementation("com.arangodb:velocypack:2.5.4")
    implementation("com.arangodb:velocypack-module-jdk8:1.1.1")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:6.2.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:6.2.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:6.2.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-core:6.2.0")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.example.ApplicationKt"))
        }
    }
}

fun mapboxKey(): String? {
    val jsonFile = file("secrets.json")
    val result = JsonSlurper().parse(jsonFile).cast<Map<String, String>>()
    return result["mapboxDownloadKey"]
}
