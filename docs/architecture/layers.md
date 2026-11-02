# Слои

Кто кого может вызывать и куда класть новый класс. Относится ко всему коду `core:*` и `feature:*`.

Related: `architecture/overview.md`, `architecture/data.md`, `code/presentation.md`, `code/di.md`.

## Правила слоёв (CRITICAL)

1. **Feature и presentation никогда не обращаются к репозиториям-реализациям, API, базе, DataStore и классам
   `core:data` / `core:network` / `core:database` напрямую.** Только через интерфейсы и use case'ы из `core:domain`.
2. **Интерфейсы** репозиториев и сервисов — в `core:domain`, **реализации** — в `core:data` (или в платформенном
   модуле, если это системный сервис).
3. **Use case** — одна ответственность, один публичный `invoke`, базовые классы в
   `core:domain/.../usecase` (`UseCase`, `SuspendUseCase`, `FlowUseCase`). Заводим, когда логика нужна нескольким
   экранам или это не одиночный вызов репозитория. Однострочная обёртка над репозиторием — не use case.
4. Результат операции, которая может упасть, — `Response<T>` (`architecture/data.md` § Response). В presentation —
   `.onSuccess` / `.onError` / `handleResponse`.
5. Состояние экрана живёт в `Logic` (`code/presentation.md`). Репозитории, use case'ы и менеджеры данных своего
   состояния экрана не держат: у них только зависимости из конструктора и (у репозиториев) источники данных.
6. Модели `core:model` не знают про UI и сеть. DTO запроса — рядом с API в `core:network` или `core:data`,
   маппер DTO → модель — в `core:data`.

## Цепочка

```
Ui (Compose)
  └─ Component          жизненный цикл и навигация
       └─ Logic         состояние экрана, обработка UiEvent
            └─ UseCase* / Repository interface   (core:domain)
                 └─ Repository impl              (core:data)
                      ├─ Api        Ktor, ответ → Response     (core:network)
                      ├─ Dao        Room                        (core:database)
                      ├─ Prefs      DataStore                   (core:datastore)
                      └─ Cache      память и диск               (core:cache)
```

Стрелки только вниз; никто не знает про слой выше себя.

## Что куда класть

| Класс | Куда |
|---|---|
| Интерфейс репозитория | `core/domain/.../repository/` |
| Use case | `core/domain/.../usecase/<область>/` |
| Реализация репозитория, источник данных | `core/data/.../<область>/` |
| API-класс (запросы), DTO | `core/network` (или `core/data`, если DTO нужен только репозиторию) |
| Модель предметной области | `core/model` |
| Сущность и DAO Room | `core/database` |
| Менеджер платформы (буфер, язык, ссылки) | интерфейс в `core/platform/commonMain`, реализации в `androidMain` / `jvmMain` |
| Общая логика с состоянием для нескольких экранов (уведомления, диалог ошибок) | `core/presentation/.../components/units/<name>/` |

## Пакеты внутри модуля

`org.kimoteru.wallify.core.<module>` и `org.kimoteru.wallify.feature.<name>`; внутри — по области
(`settings`, `config`, `download`), а не по типу класса. Имена — `code/code-style.md` § Имена.