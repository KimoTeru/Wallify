# Проверки и запуск

Кто что проверяет. Агент проект не собирает и не запускает: ни `./gradlew`, ни тесты, ни приложение. Сборку и UI
проверяет пользователь в IDE.

Related: `setup/gradle-conventions.md`.

## Что делает агент

Статическая проверка перед тем, как отдать работу:

- Каждый импорт из проекта (`org.kimoteru.wallify.*`) указывает на существующий класс или функцию.
- Модуль, который импортирует класс из другого модуля, имеет `projects.*` на него в `build.gradle.kts` (feature-модули
  получают `common`, `model`, `domain`, `design-system`, `presentation` от плагина `wallify.feature`).
- Типы, которые видны в публичном API модуля, отдаются через `api`, иначе потребитель получит
  `Cannot access class …` (`setup/gradle-conventions.md` § api и implementation).
- Новая зависимость есть в `gradle/libs.versions.toml`, новый модуль — в `settings.gradle.kts`.
- Нет ссылок на удалённые классы и ресурсы (поиск по имени).

Правка кода — только инструментами редактирования файлов. Разовое чтение (`rg`, `ls`) допустимо; скрипты, которые
меняют код, нет.

## Что делает пользователь

```
./gradlew :androidApp:assembleDebug     # Android
./gradlew :desktopApp:run               # Desktop
./gradlew :desktopApp:hotRun --auto     # Desktop с hot reload
./gradlew allTests                      # тесты всех модулей
./gradlew buildRelease                  # release-APK и установщик Desktop для текущей ОС
```

- Первый запуск после правок сборки — Gradle sync в IDE. Ошибки компиляции пользователь присылает целиком, агент
  правит их по одной.
- Если после sync Gradle жалуется на модуль, которого нет, проверь `settings.gradle.kts` и `.idea/gradle.xml`
  (`.idea` в git не лежит).
- `./gradlew allTests` — корневая задача-агрегатор из `build.gradle.kts`.
- `./gradlew buildRelease` — тоже корневая: собирает release-APK и установщик Desktop и копирует их в
  `build/release/` как `Wallify-<версия>.<apk|dmg|msi|deb>` (задачи `copyReleaseApk` и `copyReleaseInstaller` в
  convention-плагинах приложений). Стандартные выходы AGP и Compose не переносим: их ищет IDE. APK берём из Artifacts
  API (`SingleArtifact.APK`), а не по пути `build/outputs`: AGP сам свяжет копирование со сборкой. Установщик
  собирается только под ОС, на которой запущена задача: DMG на macOS, MSI на Windows, DEB на Linux.

## MCP Hot Reload

- `.mcp.json` в корне подключает MCP-сервер Compose Hot Reload: Claude Code сам запускает задачу
  `:desktopApp:hotMcpServer`, агент `./gradlew` не вызывает. Задача приходит с плагином Compose Multiplatform
  (Hot Reload 1.2.0+), отдельной зависимости нет.
- В конфиге полный путь задачи: короткое `hotMcpServer` совпадёт и с `hotMcpServerJvm` KMP-модулей с Compose, и
  Gradle запустит несколько серверов.
- Приложение запускает пользователь (`./gradlew :desktopApp:hotRun --auto`). Сервер ждёт его и подключается сам; через
  него агент видит экран Desktop (скриншот, дерево семантики, ошибки UI, логи) и проверяет свою правку после reload.
- Сервер работает только с Desktop: вёрстку Android он не показывает.

## Тесты

- Тесты лежат в `commonTest` модуля; платформенные — в `jvmTest` (только если нужен платформенный API).
- Новая чистая логика (утилиты, источники данных, мапперы) идёт с тестом рядом: образцы —
  `core/common/.../response/ResponseTest.kt`, `core/network/.../NetworkClientTest.kt` (`MockEngine`).