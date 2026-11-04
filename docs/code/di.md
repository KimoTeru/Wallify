# DI: Koin Annotations

Как регистрировать зависимости и подключать фичи. Koin 4 с аннотациями; граф собирается и проверяется на этапе
компиляции Kotlin-плагином Koin (`io.insert-koin.compiler.plugin`), KSP для Koin не используем.

Related: `code/presentation.md`, `architecture/layers.md`, `setup/gradle-conventions.md` § Новый модуль.

## Правила

1. В каждом Gradle-модуле с привязками один корневой класс в корневом пакете модуля:

   ```kotlin
   @Module
   @Configuration
   @ComponentScan("org.kimoteru.wallify.core.data")
   class DataModule
   ```

   Без любой из трёх аннотаций привязки модуля не попадут в граф: ошибка будет не при сборке, а при запуске.
2. Привязки пишем аннотациями на самих классах:
   - `@Factory` — логика экрана (новый экземпляр на экран; жизнью управляет `instanceKeeper`) и use case'ы;
   - `@Singleton` — репозитории, менеджеры, клиенты, всё без привязки к экрану. Не `@Single`: у `@Singleton` те же
     параметры, а имя совпадает с JSR-330.
3. Класс, реализующий один интерфейс, привязывается к нему автоматически:
   `@Singleton class XxxRepositoryImpl : XxxRepository`. Реализации наружу не торчат.
4. Если класс не может нести аннотацию (тип из `core:common` и `core:domain`, чужая библиотека, нужна сборка в коде),
   привязка — функция внутри `@Module`-класса: `@Singleton fun json(): Json = Json { … }`. Параметры функции
   подставляются из графа. Use case'ы `core:domain` регистрируются так, в `DataModule`: `domain` и `common` про Koin не
   знают.
5. Параметры, которые передаёт вызывающий (`parametersOf`), помечаем `@InjectedParam` в конструкторе.
6. Значения из сборки и окружения (ключи бэкенда) в `startKoin` не передаём: они генерируются в константы
   `ApiBuildConfig` модуля `core:network`, а `NetworkModule` сам собирает из них `ApiCredentials` и `NetworkConfig`
   (`setup/config-and-secrets.md`). Тип, который приходит в граф снаружи как готовый объект, помечаем `@Provided` на
   параметре конструктора (узко: параметр, а не класс).
7. Именованные привязки (`@Named`) не заводим без причины: если нужны два клиента одного типа, лучше обёртка-тип
   (так сделан `DownloadHttpClient`).
8. Платформенные привязки — аннотация на `actual`-классе или на классе в `androidMain` / `jvmMain`; его подхватывает
   `@ComponentScan` корневого класса модуля. `expect fun platformXxxModule()` не нужны. `Context` приходит из
   `androidContext()` и в проверке графа не требует `@Provided`.
9. Бизнес-код не зависит от Koin: классы получают зависимости через конструктор. Аннотации допустимы в `core:data`,
   `core:network`, `core:cache`, `core:database`, `core:datastore`, `core:platform`, `core:presentation` и
   `feature:*`; в `core:common`, `core:model`, `core:domain` их нет. `KoinComponent` допустим в `Component` и `Logic`
   экрана (базовые классы уже его реализуют).
10. Нужно, чтобы привязка создавалась на старте приложения (например, `SessionHeadersSynchronizer`):
    `@Singleton(createdAtStart = true)`.

## Старт

- Каждое приложение — свой `@KoinApplication` класс: `MainApplication` (Android) и `DesktopApp` (Desktop). Модули в
  нём не перечисляются: все `@Module @Configuration` из зависимостей приложения находятся сами, и весь граф
  проверяется при компиляции. Зависимости на модули приложению даёт `applyAppGraph()` в `build-logic`.
- Старт — `startKoin<MainApplication> { … }` (импорт `org.koin.plugin.module.dsl.startKoin`). В блоке только то, что
  зависит от платформы: на Android `androidContext(...)`.
- Desktop: `main` вызывает `startKoin<DesktopApp>()` до создания корневого компонента.

## Фичи

Образец — `feature/splash` (`presentation/components` для логики, `XxxModule` в корне пакета):

```kotlin
@Module
@Configuration
@ComponentScan("org.kimoteru.wallify.feature.splash")
class SplashModule

@Factory
class SplashLogic : BaseLogic<…>()
```

- `XxxModule.kt` в корне пакета фичи; сам `Logic` помечается `@Factory`.
- Плагин Koin и `koin-annotations` подключает `wallify.feature`. В обычном модуле — `alias(libs.plugins.koinCompiler)`
  и `implementation(libs.koin.annotations)` в `build.gradle.kts`.
- Новый модуль с привязками добавляется в `graphProjects` (`build-logic/.../AppGraph.kt`), иначе плагин его не увидит.
- Фича получает репозитории и use case'ы из `core:domain` через конструктор логики, Koin подставляет реализации.

## Проверка и ошибки

- Отсутствующая зависимость, неверный тип параметра, привязка не из той области видимости — ошибки сборки с кодом
  `[KOIN-…]`, а не падение при запуске.
- Версия Kotlin должна быть из списка, проверенного плагином (`supported-kotlin-versions.txt` в репозитории
  `koin-compiler-plugin`); при обновлении Kotlin проверяем и плагин.
- Не используем: `koin-ksp-compiler`, KSP-плагин для Koin, импорты `org.koin.ksp.generated.*`, ручные `module { … }`,
  `factoryOf` / `singleOf` в коде модулей и в `startKoin` приложений.

## Тесты

Зависимости для теста передаём в конструктор руками; Koin в юнит-тестах не поднимаем.