import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "me.ahmadhajjar.giphy"
version = "1.0.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
                implementation("com.beust:klaxon:5.5")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    val resourcesRoot = project.file("src/jvmMain/resources")

    application {
        mainClass = "me.ahmadhajjar.giphy.MainKt"
        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "GiphyInserter"
            packageVersion = "1.0.2"
            version = "1.0.2"
            description = "Helps you search, navigate and copy giphies"
            vendor = "Ahmad Hajjar"
            macOS {
                // macOS specific options
                iconFile.set(resourcesRoot.resolve("icons/icon-big.icns"))
                bundleID = "me.ahmadhajjar.giphy"
                packageName = "Giphy Inserter"
                dockName = "Giphy Inserter"
            }
            windows {
                // Windows specific options
                iconFile.set(resourcesRoot.resolve("icons/icon-big.ico"))
                perUserInstall = true
                menuGroup = "Giphy Inserter"
                upgradeUuid = "110c6d64-6fc0-4990-9171-730870738e19"
            }
            linux {
                // Linux specific options
                iconFile.set(resourcesRoot.resolve("icons/icon-big.png"))
                packageName = "Giphy Inserter"
                debMaintainer = "contact@ahmadhajjar.me"
                menuGroup = "Giphy Inserter"
                appRelease = "1"
            }
        }
    }
}
