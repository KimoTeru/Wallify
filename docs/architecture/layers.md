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
6. Модели `core:model` не знают про UI и сеть. Модели запроса и ответа (`XxxRequest` / `XxxResponse`) — рядом с API в
   `core:network`; маппер `toDomain()` / `toRequest()` лежит в файле своей модели, отдельных файлов мапперов нет.

## Цепочка

```
Ui (Compose)
  └─ Component          жизненный цикл и навигация
       └─ Logic         состояние экрана, обработка UiEvent
            └─ UseCase* / Repository interface   (core:domain)
                 └─ Repository impl              (core:data)
                      ├─ RemoteDataSource  вызов Api + toDomain()      (core:data)
                      │    └─ Api  Ktor, ответ → Response              (core:network)
                      ├─ Dao        Room                        (core:database)
                      ├─ Prefs      DataStore                   (core:datastore)
                      └─ Files      каталог скачанных файлов    (core:cache)
```

Стрелки только вниз; никто не знает про слой выше себя.

## Что куда класть

| Класс | Куда |
|---|---|
| Интерфейс репозитория (любой, включая `SessionRepository`, `ConnectivityRepository`) | `core/domain/.../repository/` |
| Use case | `core/domain/.../usecase/<область>/` |
| Интерфейс фонового сервиса данных, наружу которого нужно показать состояние (`UserSessionManager`) | `core/domain/.../manager/`, реализация — `core/data/.../<область>/` |
| Реализация репозитория (`XxxRepositoryImpl`) | `core/data/.../repository/` |
| Источник данных | `core/data/.../<область>/` (например, `settings/`) |
| Удалённый источник (`XxxRemoteDataSource` + `Impl`, мапит ответ API в доменные модели) | `core/data/.../remote/` |
| Сервис данных без репозитория (синхронизация сессии, загрузка файлов) | `core/data/.../<область>/` (`session/`, `download/`) |
| API-класс (запросы), `XxxRequest`, `XxxResponse` | `core/network/.../api/`, `.../model/request/`, `.../model/response/<область>/` |
| Модель предметной области | `core/model` |
| Сущность и DAO Room (мапперы `toEntity()` / `toDomain()` — в файле сущности) | `core/database/.../entity/` и `core/database/.../dao/` (`AppDatabase` и `DatabaseModule` — в корне пакета; конвертеры — `converter/`) |
| Загрузка с кэшем в Room: `CacheLoader` (постраничные списки), `ListRemoteMediator`, `refreshableFlow`, `CacheStore`, ключи списков, очистка базы | `core/data/.../cache/` |
| Менеджер платформы (буфер, язык, ссылки) | интерфейс в `core/platform/commonMain`, реализации в `androidMain` / `jvmMain` |
| Общая логика с состоянием для нескольких экранов (уведомления, диалог ошибок) | `core/presentation/.../components/units/<name>/` |

## Пакеты внутри модуля

`org.kimoteru.wallify.core.<module>` и `org.kimoteru.wallify.feature.<name>`; внутри — по области
(`settings`, `config`, `download`, `session`); исключение — репозитории: интерфейсы лежат в `core/domain/.../repository/`,
реализации в `core/data/.../repository/`. Имена — `code/code-style.md` § Имена.