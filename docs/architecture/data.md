# Данные: Response, сеть, сессия, хранилища, пагинация

Как устроен слой данных. Что куда класть по слоям — `architecture/layers.md`.

Related: `setup/config-and-secrets.md`, `code/di.md`, `design/foundations.md` § Списки.

## Response

`Response<T>` из `core:common` — единственный тип результата операции данных: `Response.Success(data)` или
`Response.Error(ResponseError(message, code))`. Второго типа результата нет.

- `code` — HTTP-статус; `ResponseError.DEBUG_ERROR_CODE` (`-1`), если кода нет (сеть недоступна, ошибка разбора).
- Обработка: `map`, `flatMap`, `onSuccess`, `onError`, `handleResponse`, `getOrNull`; зависимые вызовы подряд —
  `combineResponse` (до пяти шагов), независимые — `combineResponses`.
- Блок, который может бросить исключение, оборачивается в `responseOf { … }`: `CancellationException`
  пробрасывается, остальное становится `Response.Error`.
- Типизированная ошибка (код, enum бэкенда) — `DetailedResponse<T, E>`; `executeRequestWithErrorCode` возвращает
  `DetailedResponse<T, Int>`.
- Состояние загрузки для UI — `Resource<T>` (`Loading` / `Success` / `Error`), строится через `asResource()` и
  `asResourceFlow()`.
- `catch (e: Exception)` в `suspend`-коде всегда пропускает `CancellationException`.

## Сеть

`core:network`. Один `HttpClient` из Koin, собирается `HttpClientFactory`.

- Запрос — `executeRequest<Dto> { client.get("path") }`, возвращает `Response<Dto>`. Не-2xx превращается в
  `Response.Error` с HTTP-кодом; текст берётся из тела (`ApiErrorBody`: `errors`, `message` или `error`).
- API-класс — маленький класс с `HttpClient` в конструкторе и методами по эндпоинтам; относительные пути, без
  базового адреса.
- Базовый адрес, таймауты, логирование и статические заголовки приходят из `NetworkConfig`. Всё, что специфично для
  бэкенда (имена и формат заголовков авторизации и версии), определено в одном месте — `apiNetworkConfig`. Остальной
  код этих деталей не знает.
- Логирование включается флагом; заголовок `Authorization` в логах скрыт (`sensitiveHeaders`).
- Динамические заголовки (токен пользователя, язык) добавляет `AuthHeadersPlugin` из `HeadersCollectorRepository`;
  вручную в запросах их не ставим.
- Для скачивания произвольных файлов используется отдельный `DownloadHttpClient` без базового адреса и без
  заголовков API — ключ не уходит на сторонние хосты.
- Состояние сети — `NetworkMonitor`; фичи видят его только как `ConnectivityRepository` из `core:domain`.
- Новый API-класс тестируется через `MockEngine` (`NetworkClientTest`).

## Сессия

- `SessionRepository` хранит токен пользователя в DataStore (`updateAuthToken`, `clear`). Нет токена — приложение
  работает как анонимный клиент со статическим ключом.
- `SessionHeadersSynchronizer` стартует вместе с Koin и сводит токен и язык (из `UserSettings` или системный) в
  `HeadersCollectorRepository`. Токен уходит как `Bearer <token>`, заменяя статический заголовок авторизации.
- Истёкшая сессия: любой код, увидевший 401, вызывает `ApiErrorManager.emitEvent(SessionExpired)`; корневой экран
  очищает сессию (`code/presentation.md` § Общие блоки).

## Хранилища

| Что | Где | Правило |
|---|---|---|
| Реляционные данные | `core:database` (Room KMP) | Схему не меняем без явной просьбы; правка сущности — новая версия `AppDatabase` и миграция. Схема экспортируется в `schemas/` |
| Настройки и маленькие значения | `core:datastore` (`DataStorePrefs`) | `core:datastore` — только хранилище ключ-значение без смысла полей. Что именно хранится (например, язык), описывает модель в `core:model`, маппинг и доступ — `core:data`; feature читает только через репозиторий |
| Ключ-значение с временем и кэшем в памяти | `KeyValueRepository` (Room + `core:cache`) | Для данных, которым не нужна своя таблица |
| Файлы | `core:cache` (`DiskCache`) | Ключ превращается в безопасное имя файла; чтение и запись под мьютексом |

- Пути к файлам и базе дают платформенные провайдеры (`expect`/`actual`); в `commonMain` путей вида `/data/...`
  нет.
- Пользовательские настройки — `UserSettings` (`core:model`) через `UserSettingsRepository`; язык и другие поля
  читают оттуда, а не из DataStore напрямую.

## Загрузка файлов

`FileDownloader` (`core:data`): `getOrDownload(url, name)` возвращает путь к файлу в кэше и повторно не скачивает.

- Параллельные запросы одного файла ждут первую загрузку.
- Файл пишется во временный путь и переносится атомарно; пустой ответ — ошибка, а не файл.
- Расширение определяется по URL (`formatFromUrl`); имя файла — `name`, приведённое к безопасному виду.

## Пагинация

`androidx.paging` (мультиплатформенный): `paging-common` в `core:common`, `paging-compose` в `core:design-system`.

- Репозиторий отдаёт `Flow<PagingData<T>>` через `createPager(pageSize) { page, loadSize -> Response<List<T>> }`.
  Первая страница — 1; страница короче запрошенной — последняя. `initialLoadSize` равен `pageSize`, не меняем:
  иначе арифметика номеров страниц ломается.
- Ошибка страницы приходит как `PagingLoadException` с HTTP-кодом.
- В `Logic` поток кэшируется `cachedIn(coroutineScope)`, в Ui — `collectAsLazyPagingItems()` (`design/foundations.md`
  § Списки).
- `RemoteMediator` с Room заводится только под конкретную офлайн-ленту и по просьбе.