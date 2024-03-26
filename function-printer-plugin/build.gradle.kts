plugins {
  kotlin("jvm")
  kotlin("kapt")
}

kotlin {
  jvmToolchain(17)
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
  compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
  kapt("com.google.auto.service:auto-service:1.0.1")
}