import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.9.20"
}

kotlin {
  jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
  val functionPrinterPluginId = "land.sungbin.function.printer"
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-P",
      "plugin:$functionPrinterPluginId:tag=FP",
    )
  }
}


dependencies {
  PLUGIN_CLASSPATH_CONFIGURATION_NAME(project(":function-printer-plugin"))
}

repositories {
  mavenCentral()
}

