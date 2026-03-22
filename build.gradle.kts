plugins {
    java
}

allprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
    }
}

project(":common") {

}

project(":server") {
    dependencies {
        implementation(project(":common"))
        implementation("io.netty:netty-all:4.1.100.Final")
    }
}

project(":client") {
    dependencies {
        implementation(project(":common"))
        implementation("com.badlogicgames.gdx:gdx:1.12.1")
        implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1")
        implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop")
        implementation("io.netty:netty-all:4.1.100.Final")
    }
}