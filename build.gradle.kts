plugins {
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "sword.logic.compiler.Main"
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.carlos-sancho-ramirez:lib-java-collections:1.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
}