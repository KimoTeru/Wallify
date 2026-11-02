# Wallify

Kotlin Multiplatform application built with Compose Multiplatform, targeting Android and Desktop (JVM).
The project is an empty but fully configured base: modular architecture, networking, session handling, storage,
design system and an app shell (root graph, splash, home). Feature code is added on top.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.4 |
| UI | Compose Multiplatform + Material 3 |
| DI | Koin Annotations (compiler plugin) |
| Navigation | Decompose |
| Networking | Ktor |
| Database | Room (multiplatform) + bundled SQLite |
| Settings | DataStore Preferences |
| Images | Coil 3 |
| Blur | Haze |
| Adaptive UI | Material 3 Adaptive |
| Pagination | AndroidX Paging (multiplatform) |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle convention plugins (`build-logic`) |

Library versions live in `gradle/libs.versions.toml`.

## Project Structure

```
Wallify/
├── androidApp/          # Android entry point
├── desktopApp/          # Desktop entry point
├── core/
│   ├── common/          # Response, AppDispatchers, pagination, utilities
│   ├── model/           # Pure @Serializable models
│   ├── domain/          # Use cases, repository interfaces
│   ├── data/            # Repository implementations, session, file downloader
│   ├── network/         # Ktor client, request helpers, network monitor
│   ├── database/        # Room
│   ├── datastore/       # DataStore
│   ├── cache/           # Memory and disk cache
│   ├── platform/        # expect/actual platform services
│   ├── presentation/    # Base screen classes, shared UI blocks with logic
│   └── design-system/   # Theme, tokens, UI components
├── feature/
│   ├── root/            # Navigation graph, error dialog, snackbars, offline banner
│   ├── splash/
│   └── home/
└── build-logic/         # Convention plugins
```

The module dependency graph and the rules behind it are in
[`docs/architecture/overview.md`](docs/architecture/overview.md).

## Getting Started

1. Open the project in Android Studio or IntelliJ IDEA and let Gradle sync.
2. Optionally configure the backend (see below). Without it the app runs, but requests fail.
3. Run:
   - Android: `./gradlew :androidApp:assembleDebug`
   - Desktop: `./gradlew :desktopApp:run` (hot reload: `./gradlew :desktopApp:hotRun --auto`)
   - Tests: `./gradlew allTests`

### Backend configuration

Credentials never go into version control. Put them in `local.properties` or the environment:

| Value | `local.properties` | Environment variable |
|---|---|---|
| Backend URL | `api.baseUrl` | `API_BASE_URL` |
| Access key | `api.accessKey` | `API_ACCESS_KEY` |

The build reads them and generates constants for `core:network` (Android and Desktop alike); rebuild after changing them.

## Documentation for Contributors and AI Agents

| File | Purpose |
|---|---|
| [`CLAUDE.md`](CLAUDE.md) | Short always-on rules for AI agents |
| [`AGENTS.md`](AGENTS.md) | Pointer for agents that read `AGENTS.md` |
| [`docs/README.md`](docs/README.md) | Index of the detailed rules: what to read for which task |

## Known Limitations

- iOS targets are not enabled.
- The repository contains only generic data infrastructure (settings, key-value cache, session); there are no API
  repositories yet.