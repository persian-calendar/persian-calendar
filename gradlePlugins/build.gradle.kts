plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.8.0")
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "io.github.persiancalendar.codegenerators"
            implementationClass = "io.github.persiancalendar.gradle.CodeGenerators"
        }
    }
}
