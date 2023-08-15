import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import io.github.yahyatinani.tubeyou.configureKotlinAndroid
import io.github.yahyatinani.tubeyou.deps
import io.github.yahyatinani.tubeyou.disableUnnecessaryAndroidTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.library")
        apply("org.jetbrains.kotlin.android")
      }

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = 34
//        configureFlavors(this)
//        configureGradleManagedDevices(this)
      }
      extensions.configure<LibraryAndroidComponentsExtension> {
//        configurePrintApksTask(this)
        disableUnnecessaryAndroidTests(target)
      }
      dependencies {
        add("api", deps.findLibrary("y-core").get())
        add("androidTestImplementation", kotlin("test"))
        add("testImplementation", kotlin("test"))
      }
    }
  }
}
