import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
}

group = "io.github.yahyatinani.tubeyou.buildlogic"

// Configure the build-logic plugins to target JDK 17.
// This matches the JDK used to build the project, and is not related to what is
// running on device.
java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

dependencies {
  compileOnly(deps.android.gradlePlugin)
  compileOnly(deps.kotlin.gradlePlugin)
  compileOnly(deps.ksp.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "tubeyou.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = "tubeyou.android.application.compose"
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
  }
}
