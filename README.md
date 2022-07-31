# Mutekt
(Pronunciation: _**/mjuːˈteɪt/**_, 'k' is silent).   
Generates mutable models from immutable model definitions. It's based on Kotlin's Symbol Processor (KSP) which 
generates compile-time safe code from annotation. _Made with ❤️ for Kotliners_.

[![Build](https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml/badge.svg)](https://github.com/PatilShreyas/mutekt/actions/workflows/build.yml)
[![Release](https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml/badge.svg)](https://github.com/PatilShreyas/mutekt/actions/workflows/release.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.shreyaspatil.mutekt/mutekt-codegen?label=Maven%20Central&logo=android&style=flat-square)](https://search.maven.org/artifact/dev.shreyaspatil.mutekt)
[![GitHub](https://img.shields.io/github/license/PatilShreyas/mutekt?label=License)](LICENSE)

[![Github Followers](https://img.shields.io/github/followers/PatilShreyas?label=Follow&style=social)](https://github.com/PatilShreyas)
[![GitHub stars](https://img.shields.io/github/stars/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/PatilShreyas/mutekt?style=social)](https://github.com/PatilShreyas/mutekt/watchers)
[![Twitter Follow](https://img.shields.io/twitter/follow/imShreyasPatil?label=Follow&style=social)](https://twitter.com/imShreyasPatil)

## Motivation

// TODO

## Usage

You can check [`/example`](/example) directory which includes example application for demonstration.

### 1. Gradle setup

#### 1.1 Enable KSP in module

In order to support code generation at compile time, [enable KSP support in the module](https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project).

```groovy
plugins {
    id 'com.google.devtools.ksp' version '1.7.10-1.0.6'
}
```

#### 1.2 Add dependencies

In `build.gradle` of app module, include this dependency

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.shreyaspatil.mutekt:mutekt-core:$mutektVersion")
    ksp("dev.shreyaspatil.mutekt:mutekt-codegen:$mutektVersion")
    
    // Include kotlin coroutine to support usage of StateFlow 
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
```

_You can find the latest version and changelogs in the [releases](https://github.com/PatilShreyas/mutekt/releases)_.

#### 1.3 Include generated classes in sources

> **Warning**   
> In order to make IDE aware of generated code, it's important to include KSP generated sources in the project source sets.

Include generated sources as follows:

<details open>
  <summary><b>Gradle (Groovy)</b></summary>

```groovy
kotlin {
    sourceSets {
        main.kotlin.srcDirs += 'build/generated/ksp/main/kotlin'
        test.kotlin.srcDirs += 'build/generated/ksp/test/kotlin'
    }
}
```

</details>

<details>
  <summary><b>Gradle (KTS)</b></summary>

```kotlin
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
```

</details>

<details>
  <summary><b>Android (Gradle - Groovy)</b></summary>

```groovy
android {
    applicationVariants.all { variant ->
        kotlin.sourceSets {
            def name = variant.name
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
```
</details>

<details>
  <summary><b>Android (Gradle - KTS)</b></summary>

```kotlin
android {
    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}
```
</details>

### 2. Apply annotation

Declare a state model as an `interface` and apply `@GenerateMutableModel` annotation to it.

Example:

```kotlin
@GenerateMutableModel
interface NotesState {
    val isLoading: Boolean
    val notes: List<String>
    val error: String?
}
// You can also apply annotation `@Immutable` if using for Jetpack Compose UI model.
```

> **Note**
> **Checklist for applying annotation**
>- [x] Interface must have ***public*** visibility.
>- [x] All members properties should have ***public*** visibility.


Once done, **🔨Build project** and mutable model will be generated for the immutable definition by KSP.

### 3. Use generated mutable model

Once project is built and models are generated, the mutable model can be created with the factory function: `Mutable__()`.  
_For example, if interface name is `ExampleState` then method name for creating mutable model will be 
`MutableExampleState()` and will have parameters in it which are declared as public properties in the interface._

To get immutable instance with reactive state updates, use method `asStateFlow()` which returns instance of [`StateFlow<>`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/).
Whenever any field of Mutable model is updated with new value, this StateFlow gets updated with new immutable state value.

***Refer to the following example for complete usage***

```kotlin
class NotesViewModel: ViewModel() {

    /**
     * Instance of mutable model [MutableNotesState] which is generated with Mutekt.
     */
    private val _state = MutableNotesState(isLoading = false, notes = emptyList(), error = null)

    /**
     * Immutable (read-only) StateFlow of a [NotesState].
     */
    val state: StateFlow<NotesState> = _state.asStateFlow()

    fun loadNotes() {
        _state.isLoading = true

        try {
            _state.notes = getNotes()
        } catch (e: Throwable) {
            _state.error = e.message ?: "Error occurred"
        }
        _state.isLoading = false
    }
}
```

In this example, only ViewModel is allowed to mutate the state i.e. manage the state for UI. `StateFlow<NotesState>` is 
exposed to the UI layer which means UI won't be able to ***directly*** manipulate the state.

---

## 👨‍💻 Development

Clone this repository and import in IntelliJ IDEA (_any edition_) or Android Studio.

### Module details

- `mutekt-core`: Contain core annotation and interface for mutekt
- `mutekt-codegen`: Includes sources for generating mutekt code with KSP
- `example`: Example application which demonstrates usage of this library.

### Verify build

- To verify whether project building or not: `./gradlew build`.
- To verify code formatting: `./gradlew spotlessCheck`.
- To reformat code with Spotless: `./gradlew spotlessApply`.

## 🙋‍♂️ Contribute 

Read [contribution guidelines](CONTRIBUTING.md) for more information regarding contribution.

## 💬 Discuss

Have any questions, doubts or want to present your opinions, views? You're always welcome. You can [start discussions](https://github.com/PatilShreyas/mutekt/discussions).

## 📝 License

```
Copyright 2022 Shreyas Patil

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```