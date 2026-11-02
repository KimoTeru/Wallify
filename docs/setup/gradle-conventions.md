# Gradle: convention-плагины, версии, модули

Как устроена сборка и что делать при правке `*.gradle.kts`. Относится ко всему репозиторию.

Related: `setup/commands.md`, `architecture/overview.md`.

## Правила

1. Версии и координаты библиотек — только в `gradle/libs.versions.toml`. В `build.gradle.kts` модулей — только
   `libs.*` и `projects.*`; строк вида `"group:name:1.0"` нет.
2. Общая настройка модулей — в `build-logic/convention` (плагины `wallify.*`), а не копипастой в модулях. Модуль
   подключает плагин через `alias(libs.plugins.wallify.…)`.
3. `repositories {}` в модулях запрещены: репозитории объявлены в `settings.gradle.kts`.
4. Зависимости между модулями — type-safe accessors: `projects.core.common`, `projects.core.designSystem`
   (дефис в имени модуля → camelCase).
5. Версию Kotlin, Compose Multiplatform и AGP меняют только вместе и только по просьбе пользователя.

## Convention-плагины

| Плагин | Для чего |
|---|---|
| `wallify.kmp.library` | KMP-библиотека: таргеты Android (KMP-library) и JVM, `namespace` из пути модуля без дефисов (`:core:design-system` → `org.kimoteru.wallify.core.designsystem`), `commonTest` с `kotlin-test` |
| `wallify.compose` | Compose Multiplatform и compiler plugin, базовые Compose-зависимости |
| `wallify.feature` | `kmp.library` + `compose` + serialization + Decompose, Koin и его компиляторный плагин, `common`, `model`, `domain`, `design-system`, `presentation` |
| `wallify.api.config` | Генерирует `ApiBuildConfig` (адрес и ключ бэкенда) из `local.properties` или окружения; подключён в `core:network` |
| `wallify.android.application` | Android-приложение: SDK, Compose, граф Koin |
| `wallify.desktop.application` | Desktop-приложение: JVM, окно, пакеты, граф Koin |

Оба приложения получают граф Koin из `applyAppGraph()` (`build-logic/.../AppGraph.kt`): плагин компилятора, `koin-core`,
`koin-annotations` и зависимости на все модули с привязками.

Сломанная в плагине зависимость ломает все модули сразу: правишь плагин — перечитай, кого он затрагивает.

## Новый модуль

1. Папка `core/<name>` или `feature/<name>` с `build.gradle.kts`, где подключён `wallify.kmp.library` (для фичи —
   `wallify.feature`) и только модульные зависимости.
2. `include(":core:<name>")` в `settings.gradle.kts`.
3. Исходники в `src/commonMain/kotlin/org/kimoteru/wallify/core/<name>/` (для фичи — `.../feature/<name>/`);
   платформенные — `androidMain` / `jvmMain`.
4. Корневой Koin-класс `XxxModule` (`@Module @Configuration @ComponentScan`) и модуль в списке
   `graphProjects` в `build-logic/.../AppGraph.kt`, иначе граф его не увидит (`code/di.md`).
5. Строка в графе `architecture/overview.md`, если модуль новый по смыслу.

## api и implementation

- Если тип из зависимости виден в публичном API модуля (параметр, возвращаемое значение, супертип), зависимость
  объявляется как `api`. Иначе потребитель получает `Cannot access class …`.
- Так сделано, например, для корутин в `core:common`, `core:common` в `core:domain`, `okio` в `core:cache`,
  `androidx.datastore` в `core:datastore`, Room в `core:database`, Ktor client и `core:common` в `core:network`,
  `paging-common` в `core:common`, `paging-compose` в `core:design-system`.
- Всё остальное — `implementation`.

## Compose Resources

- Ресурсы лежат в `src/commonMain/composeResources/` модуля. Если модуль генерирует публичный `Res`, класс задаётся в
  `build.gradle.kts` блоком `compose.resources { publicResClass = true; packageOfResClass = "…" }`.
- Модуль без ресурсов блок `compose.resources` не содержит (`code/resources.md`).