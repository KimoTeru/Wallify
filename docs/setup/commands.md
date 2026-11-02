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
```

- Первый запуск после правок сборки — Gradle sync в IDE. Ошибки компиляции пользователь присылает целиком, агент
  правит их по одной.
- Если после sync Gradle жалуется на модуль, которого нет, проверь `settings.gradle.kts` и `.idea/gradle.xml`
  (`.idea` в git не лежит).
- `./gradlew allTests` — корневая задача-агрегатор из `build.gradle.kts`.

## Тесты

- Тесты лежат в `commonTest` модуля; платформенные — в `jvmTest` (только если нужен платформенный API).
- Новая чистая логика (утилиты, источники данных, мапперы) идёт с тестом рядом: образцы —
  `core/common/.../response/ResponseTest.kt`, `core/network/.../NetworkClientTest.kt` (`MockEngine`).