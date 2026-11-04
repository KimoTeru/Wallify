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

- Запрос — `executeRequest<XxxResponse> { client.get("path") }`, возвращает `Response<XxxResponse>`. Не-2xx превращается в
  `Response.Error` с HTTP-кодом; текст берётся из тела (`ApiErrorBody`: `errors`, `message` или `error`).
- API-класс — маленький класс с `HttpClient` в конструкторе и методами по эндпоинтам; относительные пути, без
  базового адреса.
- Базовый адрес, таймауты, логирование и статические заголовки приходят из `NetworkConfig`. Адрес и всё, что
  специфично для бэкенда (имена и формат заголовков авторизации и версии), определено в `apiNetworkConfig`.
  Имена query-параметров, форматы тел запросов, модели `XxxRequest` / `XxxResponse` — в API-классах (`api/`) и
  `model/request`, `model/response` модуля `core:network`. Остальной код этих деталей не знает: репозиторий
  принимает модели и enum'ы из `core:model`, а в строки и значения параметров их превращает API-класс
  (`toQuery()` в `api/RequestParameters.kt`).
- Модель ответа (`XxxResponse`) полная: в ней все поля из документации бэкенда, чтобы смена потребностей приложения
  не требовала правки сети. `@Serializable` с `@SerialName` для snake_case, у каждого свойства аннотация на своей
  строке; необязательные поля `null` по умолчанию, списки — `emptyList()`. Даты остаются строками: библиотеки дат в
  проекте нет. Поле без документированной формы — `JsonElement` / `JsonObject`.
- Доменная модель (`core:model`) — только то, что нужно приложению; без значений по умолчанию, `null` там, где
  данных может не быть, иначе подставляем значение в маппере (`?: String.Empty`, `?: 0`). Новое поле добавляем в
  модель, когда оно понадобилось экрану; в `XxxResponse` оно уже есть.
- Маппер — публичная функция-расширение в том же файле, что и модель: `XxxResponse.toDomain()` под моделью ответа,
  `XxxParams.toRequest()` под моделью запроса. Отдельных файлов мапперов нет; у каждой вложенной модели свой
  `toDomain()`. Вызывает его `XxxRemoteDataSourceImpl` (импорт `…model.response.<область>.toDomain`).
- Запрос с телом — форма (`formContent(...)` из `core:network`), `null`-поля в тело не попадают.
- Ответ с пустым телом (204) — `executeRequest<Unit>`.
- Цепочка для данных бэкенда: `XxxApi` (`core:network`, `Response<XxxResponse>`) → `XxxRemoteDataSource` + `Impl`
  (`core/data/.../remote/`, вызывает API и `toDomain()`) → `XxxRepository` + `Impl` (пагинация, кэш; тут же будет база).
  Методы: `getXxx`, `searchXxx`, постраничные — `getXxxFlow`.
- Логирование HTTP включено в debug-сборке и выключено в release (`AppBuild` из `core:network`): на Android — флаг
  `debuggable` приложения, на Desktop — свойство `wallify.debug`, которое задают только задачи `run` и `hotRun`
  (`build-logic`). Заголовок `Authorization` в логах скрыт (`sensitiveHeaders`).
- Динамические заголовки (токен пользователя, язык) добавляет `AuthHeadersPlugin` из `HeadersCollectorRepository`;
  вручную в запросах их не ставим.
- Для скачивания произвольных файлов используется отдельный `DownloadHttpClient` без базового адреса и без
  заголовков API — ключ не уходит на сторонние хосты.
- Состояние сети — `NetworkMonitor`; фичи видят его только как `ConnectivityRepository` из `core:domain`.
- Новый API-класс тестируется через `MockEngine` (`NetworkClientTest`).

## Бэкенд фото

Правила API (Unsplash), которые выполняет код:

- Картинки показываем только по URL из ответа (`PhotoUrls`); размер меняем параметрами этого URL, `ixid` из него не
  убираем. Ключ на хост картинок не отправляем: файлы качает `DownloadHttpClient`.
- Каждое «скачивание» фото (сохранить, поставить обоями) сопровождается `PhotoRepository.trackDownload(photoId)`;
  без этого вызова файл не качаем.
- Показывая фото, указываем автора и источник со ссылками на их профили; в ссылки добавляем
  `utm_source=<имя приложения>&utm_medium=referral`.
- Лимит страницы — 30 (`DEFAULT_PAGE_SIZE`); в демо-режиме 50 запросов в час, исчерпание приходит как 403.
- Название бэкенда не используем в имени приложения и в иконке.
- Пользовательские запросы (`/me`, коллекции пользователя, изменение фото) работают только с токеном пользователя
  (`Bearer`, см. § Сессия); с одним ключом доступа бэкенд отвечает 401.

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