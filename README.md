# Wallify

Kotlin Multiplatform application built with Compose Multiplatform, targeting Android and Desktop (JVM).
The project is a photo browser on a modular architecture: networking, session handling, storage, a crystalline glass
design system (night and day schemes, adaptive phone / tablet / desktop navigation) and the screens: feed with search,
curated topics, random photos, photo details, photographer profile and settings.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.4 |
| UI | Compose Multiplatform + Material 3 |
| DI | Koin Annotations (compiler plugin) |
| Navigation | Decompose |
| Networking | Ktor |
| Backend | [Unsplash API](https://unsplash.com/documentation) (`https://api.unsplash.com/`) |
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
│   ├── design-system/   # Theme, tokens, UI components
│   └── resources/       # All strings, fonts and images (public Res)
├── feature/
│   ├── root/            # Navigation graph, tab navigation, error dialog, snackbars, offline banner
│   ├── splash/
│   ├── auth/            # Sign-in screen, shown until the user has a session
│   ├── feed/            # Photo feed with search and filters
│   ├── topics/          # Curated topics and their photos
│   ├── random/          # Random photos
│   ├── photo/           # Photo details: EXIF, statistics, collections, download
│   ├── profile/         # Photographer profile and the user's own studio
│   └── settings/        # Account, color scheme, service numbers
└── build-logic/         # Convention plugins
```

The module dependency graph and the rules behind it are in
[`docs/architecture/overview.md`](docs/architecture/overview.md).

## Getting Started

1. Open the project in Android Studio or IntelliJ IDEA and let Gradle sync.
2. Configure the backend (see below). The access key and the secret key are required: without them the build fails
   with a message naming the missing values.
3. Run:
   - Android: `./gradlew :androidApp:assembleDebug`
   - Desktop: `./gradlew :desktopApp:run` (hot reload: `./gradlew :desktopApp:hotRun --auto`)
   - Tests: `./gradlew allTests`

### Backend configuration

The backend address is embedded in `core:network`. The keys of your Unsplash application (see
[Registering your application](https://unsplash.com/documentation#registering-your-application)) never go into
version control. Put them in `local.properties` or the environment:

| Value | `local.properties` | Environment variable |
|---|---|---|
| Access key (required) | `api.accessKey` | `API_ACCESS_KEY` |
| Secret key (required) | `api.secretKey` | `API_SECRET_KEY` |

The build reads them and generates constants for `core:network` (Android and Desktop alike); rebuild after changing them.
A new application is in demo mode: 50 requests per hour. Follow the
[API guidelines](https://help.unsplash.com/en/articles/2511245-unsplash-api-guidelines): hotlinked image URLs,
download tracking and attribution (`docs/architecture/data.md` § Бэкенд фото).

## Documentation for Contributors and AI Agents

| File | Purpose |
|---|---|
| [`CLAUDE.md`](CLAUDE.md) | Short always-on rules for AI agents |
| [`AGENTS.md`](AGENTS.md) | Pointer for agents that read `AGENTS.md` |
| [`docs/README.md`](docs/README.md) | Index of the detailed rules: what to read for which task |

## Known Limitations

- iOS targets are not enabled.
- The API layer covers every documented endpoint: API classes and full response models (with their `toDomain()`
  mappers) in `core:network`, remote data sources and repositories in `core:data`, trimmed domain models in
  `core:model`. No screen uses it yet.
- Signing in opens the system browser and needs two redirect URIs in the application on the backend
  (`docs/setup/config-and-secrets.md` § Вход пользователя). Nothing is downloaded and no server of your own is needed.
- The secret key is built into the application. For a public release move requests that need it behind your own proxy
  (`docs/setup/config-and-secrets.md`).