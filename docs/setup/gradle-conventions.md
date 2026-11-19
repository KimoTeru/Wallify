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
6. Release-сборка Android идёт через R8 (`isMinifyEnabled`, `isShrinkResources` в `wallify.android.application`):
   без него Compose заметно медленнее. Плавность и рывки меряем только на release. Свои правила R8 — в
   `androidApp/proguard-rules.pro` и только под то, о чём сообщил R8 (`missing_rules.txt`) или что упало на release:
   библиотеки приносят свои. Правило узкое (класс и член, а не `-keep class lib.** { *; }`): широкий `-keep`
   выключает оптимизацию библиотеки. Перед новым правилом смотрим, нет ли его уже в jar библиотеки
   (`META-INF/proguard`, `META-INF/com.android.tools`).
   Приложение нигде не публикуется, поэтому release подписан debug-ключом (`wallify.android.application`): APK
   ставится как есть, своего keystore и паролей в сборке нет. Поверх APK, собранного на другой машине (там свой
   `~/.android/debug.keystore`), обновление не встанет — сначала удалить старое.
7. Desktop собирается через ProGuard задачами `…Release…` (`packageReleaseDmg`, `runRelease`); правила —
   `desktopApp/proguard-rules.pro` (`wallify.desktop.application`), имена не обфусцируем. Правила библиотек здесь
   применяются не гарантированно, поэтому нужные (ServiceLoader, JNI, сериализация, protobuf DataStore) скопированы
   в файл из jar текущих версий: обновили библиотеку — сверяем её правила. Имя пакета — `ProjectConfig.APP_NAME`
   (так приложение называется в системе), идентификатор macOS — `bundleID = APPLICATION_ID`. На Windows пункт в «Пуске» даёт `menu = true`, а
   обновление поверх старой версии — `upgradeUuid`, выведенный из `APPLICATION_ID` (`UUID.nameUUIDFromBytes`):
   сменится id — Windows перестанет считать новую версию обновлением. На Linux пункт в
   меню приложений даёт `shortcut = true`. Пакеты (DMG, MSI, DEB) не
   подписываются: подпись macOS и Windows требует платных сертификатов, а приложение не публикуется. DMG, собранный на
   этом же Mac, открывается без предупреждений; скачанный (из CI) macOS блокирует при первом запуске — «Системные
   настройки → Конфиденциальность и безопасность → Всё равно открыть» или `xattr -dr com.apple.quarantine` на `.app`.

## Convention-плагины

| Плагин | Для чего |
|---|---|
| `wallify.kmp.library` | KMP-библиотека: таргеты Android (KMP-library) и JVM, `namespace` из пути модуля без дефисов (`:core:design-system` → `org.kimoteru.wallify.core.designsystem`), `commonTest` с `kotlin-test` |
| `wallify.compose` | Compose Multiplatform и compiler plugin, базовые Compose-зависимости |
| `wallify.feature` | `kmp.library` + `compose` + serialization + Decompose, Koin и его компиляторный плагин, `common`, `model`, `domain`, `design-system`, `resources`, `presentation` |
| `wallify.api.config` | Генерирует `ApiBuildConfig` (два обязательных ключа бэкенда `API_ACCESS_KEY`, `API_SECRET_KEY`) из `local.properties` или окружения; без ключей падает уже Gradle Sync; подключён в `core:network` |
| `wallify.android.application` | Android-приложение: SDK, Compose, граф Koin |
| `wallify.desktop.application` | Desktop-приложение: JVM, окно, пакеты, граф Koin |

Оба приложения получают граф Koin из `applyAppGraph()` (`build-logic/.../AppGraph.kt`): плагин компилятора, `koin-core`,
`koin-annotations` и зависимости на все модули с привязками.

Код `build-logic/convention/src/main/kotlin` разложен по пакетам `org.kimoteru.wallify.buildlogic.*`:

| Папка | Что лежит |
|---|---|
| `plugin/` | Сами convention-плагины (`*ConventionPlugin`); их классы указаны в `convention/build.gradle.kts` |
| `task/` | Gradle-задачи (`GenerateApiConfigTask`) |
| `config/` | Константы сборки (`ProjectConfig`, `PluginIds`) |
| `extension/` | Расширения над `Project` и `DependencyHandler` (каталог версий, `localSecret`, `implementation`) |
| `graph/` | Граф модулей приложений (`AppGraph.kt`, `graphProjects`) |

Новый плагин — в `plugin/`, регистрация в `gradlePlugin { … }` с полным именем класса.

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
- Так сделано, например, для корутин в `core:common`, `core:common` в `core:domain`, `okio` в `core:storage`,
  `androidx.datastore` в `core:datastore`, Room, `room-paging`, `paging-common` и `core:model` в `core:database`
  (в DAO `PagingSource`, в сущностях и мапперах модели), Ktor client, `core:common` и `core:model` в `core:network`,
  `paging-common` в `core:common`, `paging-compose` в `core:design-system`.
- Всё остальное — `implementation`.
- Зависимость, которую библиотека находит только в рантайме (Coil берёт сетевой загрузчик через `ServiceLoader`:
  `coil-network-ktor` и движок `ktor-client-okhttp`), не относится к коду модуля, где ею пользуются косвенно, и
  подключается в приложениях (`androidApp`, `desktopApp`), а не в `core:design-system`.

## Compose Resources

- Все ресурсы лежат в одном модуле `core:resources`, в `src/commonMain/composeResources/`. Его `build.gradle.kts`
  содержит `compose.resources { publicResClass = true }`, поэтому `Res` публичный.
- В `core:resources` включено `kotlin { android { androidResources.enable = true } }`: без него плагин
  `com.android.kotlin.multiplatform.library` не кладёт ресурсы Compose в приложение, и на Android первый же
  `stringResource` падает с `MissingResourceException`. Это нужно каждому KMP-модулю, у которого есть
  `composeResources` (у нас такой один).
- Пакет класса `Res` задаёт плагин `wallify.compose`: `<namespace модуля>.generated.resources`, для `core:resources`
  это `org.kimoteru.wallify.core.resources.generated.resources`. В других модулях блок `compose.resources` не нужен.
- Модуль, которому нужны ресурсы, подключает `projects.core.resources`; фичи получают его от `wallify.feature`
  (`code/resources.md`).