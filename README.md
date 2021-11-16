# Sync-Note
**SyncNote** is a note-taking app built with Kotlin, where notes sync across all of the user’s devices so that their thoughts 
or notes are always with them.

### Status: 🚧 In progress 🚧

# Installation
<a href="https://play.google.com/store/apps/details?id=com.notesync.notese"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height=60px /></a>

# Features
* Organize notes by Date and Title
* Dark and Light Mode support
* Secured backup notes to SD storage
* Keep works on your phone, tablet, computer, and Android wearables. Everything you add **syncs** across all of your devices so your thoughts are always with you.
* Restore the deleted notes if done by mistake
* Support multiple notes deletion
* Grid View
* Search notes quickly and effortlessly

## :camera_flash: Screenshots

<img src="/screenshots/screen_1.png" width="260">&emsp;<img src="/screenshots/screen_2.png"
width="260">&emsp;<img src="/screenshots/screen_3.png" width="260">&emsp;<img src="/screenshots/screen_4.png" width="260">&emsp;<img src="/screenshots/screen_5.png" width="260">
<br>

## Android Concepts Used Here:
* Navigation Components
* Unit Testing, Firestore Testing and Espresso Testing
* Pagination
* RecyclerView layout state management persistence
* WorkManager
* Dagger2 Dependency Injection with *Custom Scopes*
* Coroutines
* Flows and Channels
* SQLite on Android with Room Persistence library
* SearchView with debounce 
* Material Dialogs
* Sealed Class for state management and UI management
* Single Source of Truth Principal
* Message Handling system using **STACK** data structure
* Fragment Transistion Animations
* Collapsing Toolbar Layout
* ViewModels 
* Handle Configuration Changes
* Handle Process Death issues

## TODO
* Migrate from Dagger to [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* Refactor message handling system to a Queue.
* Migrate from Shared Preferences to [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).
* Migrate from Koltin Synthetics to [ViewBinding](https://developer.android.com/topic/libraries/view-binding).
* Write Unit Test cases.

## 🏗️️ Built with

| What                        | How                                                                                                                                                                             |
|:----------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 🎭 User Interface (Android) | [Layouts](https://developer.android.com/guide/topics/ui/declaring-layout)                                                                                                                |
| 🏗 Architecture             | [Clean](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)                                                                                           |
| 💉 DI (Android)             | [Dagger](https://developer.android.com/training/dependency-injection/dagger-android)                                                                                                |
| 🌊 Async                    | [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/) |
| 🌐 Networking               | [Retrofit](https://square.github.io/retrofit/)                                                                                                                                  |
| 📄 Parsing                  | [KotlinX](https://kotlinlang.org/docs/serialization.html)                                                                                                                       |                                                                                                

👤 **Rahul**

* Email: agarwalsrahul8955@gmail.com

## License

```
Copyright 2021 Rahul Agarwal

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

