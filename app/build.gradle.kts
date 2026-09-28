plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("net.sourceforge.tess4j:tess4j:5.8.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    testImplementation(kotlin("test"))
}
sqldelight { databases { create("ClipVaultDatabase") { packageName.set("com.zunawet.clipvault.data.local.db") } } }
tasks.test { useJUnitPlatform() }
