# Great Films Android App

An Android application for exploring movies using [The Movie DB API](https://www.themoviedb.org/documentation/api)
<br/>Developed for an educational project part of the [Android Developer Nanodegree Program](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801) at Udacity.
<br/>Project name: *Popular Movies*

## Implementation

[Stage 1 Implementation Guide](https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true)<br/>
[Stage 2 Implementation Guide](https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.7sxo8jefdfll)

**This app leverages Android Architecture Components from Android Jetpack.**<br/>
Room: *for accessing the locally maintained "favorite movies" SQLite database.*<br/>
LiveData: *for notifying database changes to the UI and avoiding uneccessary database querying.*<br/>
ViewModel: *for managing the UI data through lifecycle changes.*<br/>

## Install
**Step 1: clone repo**
```bash
git clone https://github.com/wceballos/GreatFilms.git
```
**Step 2: API key**<br/>
Copy/paste a [The Movie DB](https://www.themoviedb.org/) API key into MainActivity.java

**Step 3: run**<br/>
Compile in Android Studio and run.
