import org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenExec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

// One optimization pass keeps local/CI builds practical for the full astronomy
// engine. Retain Kotlin's required Wasm features and exception boundary handling.
tasks.withType<BinaryenExec>().configureEach {
    binaryenArguments.set(listOf(
        "--enable-gc", "--enable-reference-types", "--enable-exception-handling",
        "--enable-bulk-memory", "--enable-nontrapping-float-to-int",
        "--no-inline=kotlin.wasm.internal.throwValue",
        "--no-inline=kotlin.wasm.internal.getKotlinException",
        "--no-inline=kotlin.wasm.internal.jsToKotlinStringAdapter", "-O1",
    ))
}

kotlin {
    wasmJs {
        outputModuleName = "persian-calendar"
        browser {
            commonWebpackConfig { outputFileName = "persian-calendar.js" }
        }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}
