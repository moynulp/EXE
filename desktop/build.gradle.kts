import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins { kotlin("jvm"); id("org.jetbrains.compose") }
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":app"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.insert-koin:koin-compose:1.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    testImplementation(kotlin("test"))
}
compose.desktop {
    application {
        mainClass = "com.zunawet.clipvault.MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8", "-Xmx256m")
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "ClipVault"
            packageVersion = "1.0.0"
            description = "Everything you copied. Forever. Searchable. — Made by Zunawet"
            vendor = "Zunawet"
            copyright = "© 2026 Zunawet"
            modules("java.desktop", "java.sql", "java.logging", "java.management", "jdk.unsupported", "java.naming")
            windows {
                iconFile.set(project.file("src/main/resources/icons/clipvault.ico"))
                menuGroup = "ClipVault"
                shortcut = true
                perUserInstall = true
                dirChooser = true
                upgradeUuid = "7f3dab41-f647-4eab-95dd-6ba8c311ed79"
            }
        }
    }
}
tasks.test { useJUnitPlatform() }
