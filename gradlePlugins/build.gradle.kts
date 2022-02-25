plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "io.github.persiancalendar.codegenerators"
            implementationClass = "io.github.persiancalendar.gradle.CodeGenerators"
        }
    }
}
