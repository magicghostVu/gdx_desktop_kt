import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // https://mvnrepository.com/artifact/com.badlogicgames.gdx/gdx
    implementation("com.badlogicgames.gdx:gdx:1.11.0")

    // https://mvnrepository.com/artifact/com.badlogicgames.gdx/gdx-box2d
    implementation("com.badlogicgames.gdx:gdx-box2d:1.11.0")

    // https://mvnrepository.com/artifact/com.badlogicgames.gdx/gdx-box2d-platform
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:1.11.0:natives-desktop")



    // https://mvnrepository.com/artifact/com.badlogicgames.gdx/gdx-backend-lwjgl3
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.11.0")


    // https://mvnrepository.com/artifact/com.badlogicgames.gdx/gdx-platform
    implementation("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-desktop")



}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}