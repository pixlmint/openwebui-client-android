# OpenWebUI-eInk

OpenWebUI-eInk is an Android application designed to provide a client interface for the Open-WebUI project. It's optimized for e-ink devices, offering a clean, simple, and responsive user experience.

## Project Structure

The project follows a standard Android application structure:

-   `app/src/main/java/com/example/openwebuieink`: The core source code of the application.
    -   `MainActivity.kt`: The main entry point of the application.
    -   `network/`: Contains the networking layer, responsible for communication with the Open-WebUI API.
    -   `db/`: Manages the local database for storing chats and other data.
    -   `ui/`:  Holds all the Composable UI components and ViewModels.
    -   `data/`:  Contains data models for the application.
-   `app/src/main/res/`:  Includes all application resources, such as layouts, drawables, and values.
-   `app/build.gradle.kts`: The build script for the application module.

## Features

-   **Chat Interface**:  A simple and intuitive chat interface for interacting with the Open-WebUI.
-   **Model Selection**:  Allows users to select from a list of available models.
-   **Chat History**:  Saves chat history for easy access.
-   **Settings**:  Provides options to configure the application.
-   **E-Ink Optimization**: The UI is designed to be responsive and easy to use on e-ink screens.

## How to Build

1.  Clone the repository: `git clone https://github.com/your-username/OpenWebUI-eInk.git`
2.  Open the project in Android Studio.
3.  Create a `local.properties` file in the root directory and add the following line:
    `sdk.dir=/path/to/your/android/sdk`
4.  Build the project using Gradle.

## Dependencies

The project uses the following key dependencies:

-   **Jetpack Compose**: For building the user interface.
-   **Coroutines**: For managing asynchronous operations.
-   **Retrofit**: For making network requests to the Open-WebUI API.
-   **Room**: For local database storage.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.
