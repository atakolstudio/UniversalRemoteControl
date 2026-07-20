// Top-level build file
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

/*
NOT: Bu proje yazıldığı sırada Android Gradle Plugin (AGP) 9.x ve derleyici
compileSdk = 36 hedefleri kamuya henüz stabil şekilde dağıtılmamış olabilir.
Yukarıdaki sürümler (AGP 8.6.1 / Kotlin 2.0.21) bu depoyu "bugün" sorunsuz
şekilde senkronize edip derleyebilmeniz için seçilmiştir. Yeni AGP/Kotlin
sürümleri istikrarlı olarak yayınlandığında `libs.versions.toml` (aşağıda)
üzerinden güncelleyebilirsiniz; app/build.gradle.kts içindeki compileSdk/
targetSdk değerlerini 36 olarak bıraktık, gerekirse Android Studio'nun
önerdiği SDK Platformu'nu ayrıca indirin (SDK Manager > Android 16.0).
*/
