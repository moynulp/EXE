plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("app.cash.sqldelight") version "2.0.2" apply false
}
tasks.register("run") { dependsOn(":desktop:run") }
tasks.register("packageMsi") { dependsOn(":desktop:packageMsi") }
tasks.register("packageExe") { dependsOn(":desktop:packageExe") }
tasks.register("check") { dependsOn(":app:check", ":desktop:check") }
