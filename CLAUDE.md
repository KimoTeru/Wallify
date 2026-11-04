# CLAUDE.md — Wallify

Kotlin Multiplatform (Android + Desktop) приложение на Compose Multiplatform. Архитектура, модули, паттерны и
дизайн-система описаны в `docs/` и автоматически не грузятся: открой `docs/README.md`, найди строки
под задачу в «Что когда читать» и прочитай эти доки до того, как трогать код.

**Это обязательно для любой модели.** Первым действием в чате — `Read` на `docs/README.md`, затем `Read` доков под
задачу. Хук `.cursor/hooks/require-docs.sh` блокирует правки файлов, пока в этом чате не прочитан `docs/README.md`.
Перед первой правкой напиши строку `Доки: <список прочитанных файлов>`. Не помнишь содержимое доков — перечитай, а не
угадывай: правило, которого нет в доках, не придумывают, а спрашивают у пользователя.

## Как работать

1. Загрузи доки под задачу: сборка и зависимости → `setup/`, где что лежит и кто кого вызывает → `architecture/`,
   как писать код → `code/`, вёрстка и токены → `design/`.
2. Проект не собираем: ни `./gradlew`, ни тесты, ни запуск приложения агент не запускает. Сборку и UI проверяет
   пользователь в IDE. Корректность проверяй чтением: импорты, `projects.*` в `build.gradle.kts` модуля, версии в
   каталоге, `include` в `settings.gradle.kts`.
3. Код пишем вручную правками файлов. Скрипты для массового переноса, генерации или замены кода (python, sed, awk,
   циклы в shell) не используем.
4. Правила нет ни в доках, ни здесь — спроси, а не придумывай. Поменялось правило — в том же изменении обнови док, где
   оно описано (`docs/**`), а не этот файл.

## Правила, которые действуют всегда

1. Общение с пользователем — на русском.
2. Платформы: Android и Desktop (JVM). В `commonMain` только мультиплатформенные API: без `android.*`, `java.*`,
   `javax.*`; платформенное — `expect`/`actual` в `core:platform` или в `androidMain`/`jvmMain` модуля
   (`architecture/overview.md` § Платформы).
3. Слои: feature → `presentation`, `domain`, `design-system`; в сеть, базу и хранилища feature не ходит. Данные —
   только через интерфейсы из `core:domain`, реализации в `core:data` (`architecture/layers.md`).
4. Результат операции данных — `Response<T>` из `core:common`, второго типа результата нет (`architecture/data.md`
   § Response).
5. DI — Koin Annotations: `@Factory` для логики экрана, `@Singleton` для репозиториев и менеджеров; у каждого
   модуля свой корневой `@Module @Configuration @ComponentScan` класс (`code/di.md`).
6. Экран — набор Component / Logic / Contract / Ui; логика не знает про Compose, Ui не знает про Logic
   (`code/presentation.md`).
7. Дизайн-система: цвета, шрифты, отступы и размеры только из `AppTheme`; `Color(0x…)`, `TextStyle(...)` и `N.dp` из
   головы в экранах нет. Прямой `AppTheme.…` — только в значениях параметров по умолчанию в сигнатуре; в теле
   composable — локальные `val appColor = AppTheme.color`, `val appTypography = AppTheme.typography` и
   `style = appTypography.h5.semiBold` (`design/foundations.md`).
8. Строки интерфейса не хардкодим в новых экранах: Compose Resources в `core:resources` (`code/resources.md`).
9. Конец файла: любой файл заканчивается сразу после последнего символа, без пустых строк и отступа снизу и без
   финального перевода строки. Стиль кода — как в соседних файлах проекта (`code/code-style.md` § Форматирование).
10. Потоки и время: `AppDispatchers` вместо `Dispatchers.*`, `TimeProvider` или `kotlin.time.Clock` вместо
    системных часов (`code/code-style.md`).
11. Версии зависимостей — только в `gradle/libs.versions.toml`, конфигурация сборки — в convention-плагинах
    `build-logic`, `repositories {}` в модулях запрещены (`setup/gradle-conventions.md`).
12. Ключи бэкенда не коммитим: `local.properties` или переменные окружения (`setup/config-and-secrets.md`).
    Адрес бэкенда публичный и встроен в `apiNetworkConfig`.
13. Название бэкенда (Unsplash) и его адрес допустимы в `core:network`, README и строках атрибуции в `core:resources`.
    Всё, что специфично для бэкенда (адрес, заголовки, имена параметров, форматы запросов и ответов), живёт в
    `core:network`: `apiNetworkConfig`, API-классы, модели запросов и ответов. Остальной код от конкретного бэкенда не зависит
    (`architecture/data.md` § Сеть).

Если более конкретное правило в доке противоречит правилу выше, прав док: в нём есть обоснование.