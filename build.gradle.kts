plugins {
    java
    application
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.4.2"
val skijaVersion = "0.143.18"

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
  

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    runtimeOnly("org.lwjgl:lwjgl::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-windows")

    implementation("io.github.humbleui:skija-windows-x64:$skijaVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("EditorApp")
}