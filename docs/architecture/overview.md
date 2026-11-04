# Архитектура: модули и платформы

Где что лежит и какой модуль от какого зависит. Что такое каждый слой и кто кого вызывает — `architecture/layers.md`.

Related: `setup/gradle-conventions.md`, `architecture/data.md`.

## Модули

| Модуль | Роль |
|---|---|
| `androidApp`, `desktopApp` | Точки входа платформ: окно или `Application`, старт Koin (`@KoinApplication`), создание корневого компонента. Список модулей графа им даёт `applyAppGraph()` в `build-logic` |
| `feature:root` | Корневой граф навигации, диалог ошибок, снекбары, баннер отсутствия сети; `RootGraphUi(component)` — корневой экран, тему вокруг него задают приложения |
| `feature:splash`, `feature:home` | Экраны |
| `core:presentation` | Базовые классы экрана, общие UI-блоки с логикой (шторка, диалог ошибок, уведомления, разрешения) |
| `core:design-system` | Тема, токены, компоненты без бизнес-логики |
| `core:resources` | Единственное место ресурсов: строки (по областям), шрифты, картинки; публичный `Res` |
| `core:domain` | Интерфейсы репозиториев, use case'ы |
| `core:data` | Реализации репозиториев, сессия, скачивание файлов |
| `core:model` | Чистые `@Serializable` модели |
| `core:network` | Ktor-клиент, `Response`-обёртка запросов, API-классы, модели запросов и ответов с мапперами в доменные модели, мониторинг сети |
| `core:database`, `core:datastore`, `core:cache` | Room, DataStore, кэш на диске и в памяти |
| `core:platform` | `expect`/`actual`: диспетчеры, время, язык, буфер обмена, переходы по системным ссылкам |
| `core:common` | Без зависимостей на другие `core`: `Response`, `AppDispatchers`, пагинация, расширения, flow-утилиты |

## Граф зависимостей

Стрелка идёт от модуля к тому, от кого он зависит. Каждый `core`-модуль может зависеть от `core:common` (не нарисовано).
Feature-модули получают `common`, `model`, `domain`, `presentation`, `design-system`, `resources` от плагина
`wallify.feature`.

```mermaid
flowchart TB
    subgraph apps["Приложения"]
        androidApp
        desktopApp
    end

    subgraph features["Фичи"]
        root["feature:root"]
        splash["feature:splash"]
        home["feature:home"]
    end

    subgraph ui["UI"]
        presentation["core:presentation"]
        designSystem["core:design-system"]
        resources["core:resources"]
    end

    subgraph business["Бизнес"]
        domain["core:domain"]
        data["core:data"]
        model["core:model"]
    end

    subgraph infra["Инфраструктура"]
        network["core:network"]
        database["core:database"]
        datastore["core:datastore"]
        cache["core:cache"]
        platform["core:platform"]
    end

    common["core:common"]

    apps --> features
    apps --> data
    apps --> platform
    apps --> presentation
    root --> splash
    root --> home
    features --> presentation
    features --> domain
    presentation --> designSystem
    presentation --> resources
    features --> resources
    presentation --> platform
    domain --> model
    data --> domain
    data --> network
    network --> model
    data --> database
    data --> datastore
    data --> cache
    data --> platform
    infra -.-> common
    business -.-> common
    ui -.-> common
```

## Правила зависимостей

1. `core:common`, `core:model` ни от чего не зависят (кроме библиотек). `core:model` — только данные, без логики и UI.
2. `core:domain` зависит от `core:model` и `core:common`, не знает про `core:data`, сеть, базу и UI.
3. Реализации (`core:data`) знают про `domain` и инфраструктуру; обратной зависимости нет.
   `core:network` зависит от `core:model`: модель ответа API мапится в доменную рядом с собой (`toDomain()`),
   `core:model` про сеть не знает.
4. Feature-модули не зависят друг от друга, кроме `feature:root`, который собирает экраны в стек.
5. Приложения зависят от всех модулей с Koin-привязками (список в `build-logic`, `applyAppGraph()`), чтобы собрать
   граф; в коде приложения используют только `feature:root`, `design-system` (тема) и `core:resources` (строки).
6. Новая зависимость, которая добавляет стрелку против этого графа, — повод остановиться и спросить.

## Платформы

- Поддерживаются Android и Desktop (JVM). iOS-таргетов нет; добавлять их без просьбы не нужно.
- В `commonMain` — только мультиплатформенные API: `kotlin.*`, `kotlinx.*`, Compose Multiplatform, Ktor, Room KMP,
  Koin. Без `android.*`, `java.*`, `javax.*`.
- Платформенная реализация — `expect`/`actual` в одном модуле (`core:platform`, `core:cache`, `core:datastore`,
  `core:database`, `core:network`): интерфейс или `expect` в `commonMain`, реализации в `androidMain` и `jvmMain`.
- Если `androidMain` и `jvmMain` нужен один и тот же JVM-код, пока допустимо небольшое дублирование; общий
  промежуточный source set заводится только по просьбе.
- `TODO` и `return null` вместо реализации на одной из платформ не оставляем.