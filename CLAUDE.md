# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OpenWebUI-eInk is an Android client for the Open-WebUI project, optimized for e-ink devices. The app provides a simple chat interface with streaming responses, model selection, chat history, and connection profile management.

## Build and Development Commands

### Building
```bash
# Build the project
./gradlew build

# Build release APK
./gradlew assembleRelease

# Install debug build on connected device
./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.example.openwebuieink.ExampleUnitTest
```

### Other Commands
```bash
# Clean build artifacts
./gradlew clean

# Check for lint issues
./gradlew lint
```

## Architecture

### Core Components

**ViewModel → Service → Repository → API Pattern**
- `ChatViewModel`: Manages chat UI state and delegates to `ChatService`
- `MainViewModel`: Manages app-level state (models, chats, connection profiles)
- `ChatService`: Orchestrates chat flow including message streaming, chat creation/updates, and title generation
- `ChatRepository`: Handles all HTTP communication with Open-WebUI backend via Retrofit
- `OpenWebuiApi`: Retrofit interface defining API endpoints

### Chat Flow Architecture

The chat message flow follows a multi-step process handled by `ChatService.sendMessage()`:

1. **User Message**: Create and add user message to chat
2. **Chat Creation/Update**: Create new chat if needed, or update existing chat with user message
3. **Streaming Response**: Stream assistant response from backend via SSE (Server-Sent Events)
4. **Title Generation**: Auto-generate chat title if it's a new chat
5. **Completion**: Mark chat as completed on backend

Key considerations:
- Streaming responses emit chat updates every 2 seconds during streaming
- Each message has a unique ID and maintains parent-child relationships via `parentId` and `childrenIds`
- Chat history is a tree structure stored in `History.messages` map
- The `currentId` in History tracks the current leaf node in the conversation tree

### Data Models

**Connection Profiles**: Stored in Room database (`ConnectionProfile` entity), allowing users to switch between different Open-WebUI servers.

**Chat Objects**: Backend DTOs include both full `Chat` and `MinimalChat` representations:
- `Chat`: Full chat object with expandable `History` containing all message tree structure
- `MinimalChat`: Compact version returned by backend, converted to `Chat` via `toChat()` extension
- Messages maintain tree structure via `parentId`/`childrenIds` for branching conversations

### Network Layer

- Uses Retrofit with kotlinx-serialization for JSON parsing
- OkHttp interceptors for logging, error handling, and authentication
- Streaming chat completions via `ResponseBody` reading SSE format
- Extended timeouts (180s) for long-running streaming requests
- `@InternalSerializationApi` annotation required for several DTOs due to custom serialization

### UI Layer

- Built with Jetpack Compose and Material 3
- Navigation via Jetpack Navigation Compose
- Modal drawer for chat history and connection profile switching
- Single-activity architecture with composable screens