# Экран: Component / Logic / Contract / Ui

Форма экрана и правила состояния. Образцы — `feature/splash` (простой экран), `feature/random` (экран с
состоянием), `feature/feed` (постраничный список), `feature/photo` (аргумент экрана и `ComponentEvent`),
`feature/root` (граф с состоянием).

Related: `code/navigation.md`, `code/di.md`, `design/foundations.md`, `architecture/layers.md`.

## Форма экрана

```
feature/<name>/src/commonMain/kotlin/.../feature/<name>/
├── presentation/components/XxxComponent.kt         интерфейс XxxComponent, XxxLogic, XxxNavigationEvent
├── presentation/components/DefaultXxxComponent.kt  реализация компонента
├── presentation/ui/XxxUi.kt                        XxxContract, PreviewXxxUi и composable XxxUi — больше ничего
├── presentation/ui/utils/                          блоки вёрстки, константы и хелперы экрана, всё `internal`
└── XxxModule.kt                                    корневой класс Koin-модуля фичи (Logic помечается @Factory)
```

Образец раскладки — `feature/root`: компонент и логика в `presentation/components`, вёрстка в `presentation/ui`.

В файле `XxxUi.kt` только контракт, превью и сам экран. Остальное, что нужно экрану, лежит в
`presentation/ui/utils/` (пакет `…presentation.ui.utils`): блоки вёрстки (`FeedFiltersOverlay`, `ProfileGrid`),
константы (`SPLASH_DURATION_MS`), хелперы. Всё верхнего уровня там `internal`, вспомогательное внутри одного файла
может быть `private`; за пределы модуля фичи выходит только `XxxUi`. Тип, который входит в `UiState` или `UiEvent`
(`ProfileContract.EditForm`), — часть контракта: он вложен в `XxxContract` и остаётся публичным, `internal` его
сделать нельзя.

| Часть | Роль | Знает |
|---|---|---|
| `XxxComponent` (интерфейс) | Что Ui может у экрана просить: `getUiState()`, `obtainUiEvent(event)` | Contract |
| `DefaultXxxComponent` | Реализация: `ComponentContext`, получает `Logic` из Koin, слушает события навигации | Logic, Decompose |
| `XxxLogic` | Состояние экрана (`uiState: StateFlow`), обработка `UiEvent`, вызовы domain, `navigate(...)` | Contract, domain |
| `XxxContract` | `UiState : BaseContract.UiState`, `UiEvent : BaseContract.UiEvent` | ничего |
| `XxxUi` | Composable экрана | только интерфейс `XxxComponent` и Contract |

Создание компонента и логики:

```kotlin
override val logic: XxxLogic = retainedLogic { get<XxxLogic>() }
```

`retainedLogic` (базовый компонент) держит логику в `instanceKeeper`, как ViewModel, и вызывает её `onCreate()` один
раз — когда логика создаётся, а не на каждый пересозданный компонент. `instanceKeeper.getOrCreate` напрямую не пишем.

## Правила

1. **Ui не знает про Logic и Koin.** Composable принимает `modifier: Modifier = Modifier` первым параметром и
   `component: XxxComponent` вторым. Состояние — `component.getUiState()`, действия — `component::obtainUiEvent`.
2. **Logic не знает про Compose.** Ни `@Composable`, ни `Color`, ни `Dp`, ни строк из ресурсов: в состоянии —
   данные и флаги, форматирование и тексты делает Ui.
3. Состояние — один `UiState` (`data class`, неизменяемый), обновляется через `MutableStateFlow.update { it.copy(…) }`.
   `MutableStateFlow` объявляется через `field` (`code/code-style.md` § Корутины и потоки), а не парой
   `_uiState` / `uiState`. `@Immutable` ставим на каждый `UiState` и `ScreenUiState`, даже если все поля стабильны:
   одно правило без проверки типов полей.
   Каждое поле состояния меняется отдельным приватным методом `updateXxx(value)` с одним
   `uiState.update { it.copy(xxx = value) }` внутри; в местах вызова `it.copy(…)` не пишем. Публичные `showXxx()` /
   `hideXxx()` — тонкие обёртки над ним: `fun showDialog() = updateIsVisibleDialog(true)`. Если поля нужно менять
   только вместе, это один метод с говорящим именем. Списки вариантов экрана (`Xxx.entries`, языки) —
   поля `UiState` со значением по умолчанию, не локальные `val` в composable.
4. Действия пользователя — `UiEvent` (`sealed interface`, `data object` / `data class`). Метод на каждое действие в
   Ui не заводим: один `obtainUiEvent`.
5. Одноразовые эффекты: навигация — `navigate(NavigationEvent)` логики; сообщение пользователю — `NotifierManager`
   (без сети отдельный снекбар уже виден, текст логика берёт `getString`); сбой действия — `handleResponse`
   (§ Ошибка действия); действие, которое
   выполняет сам экран (открыть адрес в браузере через
   `LocalUriHandler`), — `ComponentEvent`: логика шлёт `sendComponentEvent(…)`, Ui ловит
   `component.collectComponentEvents { … }` (для него нужны `…Core`-типы с явным `ComponentEvent`). Не
   `StateFlow<Boolean>` с ручным сбросом.
6. Данные приходят из use case'ов и репозиториев `core:domain` (`architecture/layers.md`). Ошибки — `Response`; логика
   решает, что показать, а не бросает исключения в Ui. Сбой загрузки экран держит в `UiState` как
   `error: ResponseError?` (не `hasError: Boolean`); без контента Ui рисует по нему `ErrorContentLayout(error, onRetry)`
   (`design/foundations.md` § Нет сети), с контентом сбой идёт в снекбар (§ Ошибка действия).
7. Работа с данными — на `dispatchers.io` внутри use case'а или репозитория; `Logic` лишь запускает её в
   `coroutineScope`.
8. Жизненный цикл — переопределяем `onCreate/onStart/onResume/onPause/onStop` в `Logic` (подписки, мониторинг сети),
   отписка — в симметричном методе. Свои подписки с `Component` не пишем. `Logic` — retained instance, аналог
   ViewModel: переживает пересоздание компонента (поворот), а компонент и его lifecycle создаются заново. Поэтому
   `onCreate()` — это `init` логики: его вызывает фабрика `retainedLogic`, один раз при создании экземпляра, а
   `onStart/onResume/onPause/onStop` идут по циклу текущего компонента. Подписки и запросы на всю жизнь логики — в
   `onCreate()` в `coroutineScope` (он отменяется в `InstanceKeeper.onDestroy`); то, что привязано к виду экрана, —
   в `onStart/onStop`. `onLifecycleDestroy()` вызывается и при повороте: окончательную очистку в нём не делаем.
   Подписки на потоки пишем прямо в теле `onCreate()`, раскрыто: один `coroutineScope.launch { … }`, внутри —
   отдельный `launch { … }` на каждую подписку, и уже в нём `observeXxx()…collect { … }` (образец —
   `RootGraphLogic.onCreate`). `override fun onCreate() = observeXxx()` и подписку в отдельной приватной функции
   не пишем: всё, на что подписан экран, должно быть видно в одном месте. Разовые вызовы (`load()`) идут после
   этого блока.

   ```kotlin
   override fun onCreate() {
       coroutineScope.launch {
           launch {
               observeUserSettings()
                   .mapDistinctBy { settings -> settings.listImageQuality }
                   .collect { quality -> updateListImageQuality(quality) }
           }
       }

       load()
   }
   ```

   В подписке и в обработчике события тоже зовём `updateXxx(…)`: `uiState.update` прямо в `collect { … }`, в ветке
   `when` или в лямбде (`onRetry`) не пишем. Изменение не значением, а от текущего (счётчик, добавить в набор), —
   метод с говорящим именем: `addListRetry()`, `addPendingCollection(id)`.
9. Базовые алиасы (`BaseDefaultComponent`, `BaseLogic`, `BaseComponent`) подходят, когда у графа или экрана нет
   `ComponentEvent` и своего состояния; иначе берём `…Core` с явными типами (как `feature:root`).
10. Для списка с подгрузкой `Logic` держит `Flow<PagingData<T>>.cachedIn(coroutineScope)`
    (`architecture/data.md` § Пагинация). Поток отдаёт Ui интерфейс компонента (`val photos: Flow<PagingData<Photo>>`),
    а не `UiState`; запрос, от которого зависит список, берём из состояния:
    `uiState.mapDistinctBy { … }.flatMapLatest { … }.cachedIn(…)`. Если у экрана несколько списков (вкладки),
    `collectAsLazyPagingItems()` вызываем внутри composable своей вкладки, чтобы невидимые списки не грузились.

## Ошибка действия

Сбой показываем по одному правилу на весь проект, выбор — по тому, есть ли на экране контент:

- **контент есть** (фото открыто, список показан, форма заполнена) — снекбар корня `GlassSnackbar`: текст по типу
  ошибки с кодом и кнопка «Повторить», которая повторяет то же действие. Так идут разовые действия (скачать, добавить
  в коллекцию, сохранить профиль, войти, сбросить статистику), обновление, упавшее над показанными данными (shuffle,
  обновление списка), и сбой загрузки вошедшего пользователя в корне;
- **контента нет** — `ErrorContentLayout(error, onRetry)` на весь контент фичи (`design/foundations.md` § Нет сети).

Сами ничего не повторяем: ни автоповторов на 5xx, ни диалога ожидания. Повтор — только кнопкой пользователя; без
сети загрузку повторяет `RetryOnReconnect`.

1. Разовое действие логика выполняет через `handleResponse<T, XxxApiError>(request = { … }, onSuccess, onTypedError,
   onError, onRetry)` базовой логики, а без своих статусов — `handleResponseDefault(request, onSuccess, onError,
   onRetry)`. `.onError { … }` и `handleResponse` из `core:common` для действий не пишем: их сбой обрабатывается
   одинаково на всех экранах.
2. Свои статусы действия — enum области в `core/presentation/.../errors/` (`CollectionApiError`, `ProfileApiError`,
   `AuthApiError`), реализующий `ApiError(code, message: StringResource?)`; поиск — `Int.toApiError<T>()`. Коды берём
   только из документации бэкенда. Статус из enum показывает `message` снекбаром без повтора (`null` — экран
   показывает сам) и идёт в `onTypedError`. 401 в enum кладёт только вход: там сессии ещё нет.
3. Остальное решает `resolveApiFailure` по `ResponseErrorType`: лимит запросов — снекбар без кнопки (повтор сразу
   упадёт так же и потратит квоту); проверяется раньше enum, бэкенд шлёт его как 403. 401 — ничего: сессию уже
   очистил слой данных (`architecture/data.md` § Сессия), корень показывает вход. Остальное, в том числе 5xx и ошибка
   без кода, — снекбар с текстом по типу и кодом и «Повторить». Без сети корень снекбары экранов не показывает: виден
   его снекбар про интернет.
4. «Повторить» без `onRetry` повторяет тот же `request` с теми же колбэками. Если вокруг вызова есть своё состояние
   (индикатор, `isDownloading`, отметка «в процессе»), передаём `onRetry = { download() }` — повтор идёт тем же путём,
   что нажатие. `onError(error)` получает каждый сбой после обработки — для своего состояния экрана; снимать
   индикатор лучше после вызова, он `suspend` и возвращается по завершении.
5. Свой показ сбоя с повтором (загрузка упала над показанными данными) — `showErrorWithRetry(error) { … }` базовой
   логики: те же правила для лимита и сети.
6. Обновление постраничного списка, упавшее над показанными элементами: Ui не зовёт `NotifierManager`. Событие и
   счётчик — в контракте фичи, `BaseContract` о списках не знает: `UiEvent.ListRefreshError(list: Long,
   error: ResponseError?)` и `UiState.listRetries: Int`. Экран передаёт их списку явно:
   `PagingPhotoGrid(refreshRetries = uiState.listRetries, onRefreshError = { list, error ->
   obtainEvent(…ListRefreshError…) })`, свой список — `RefreshErrorEffect(paging, retries, onChange)` (`ui/paging`).
   В событии только данные, без лямбд: лямбда из Ui в логике переживает поворот экрана и держит
   старые объекты Compose. Логика отвечает
   `showListRefreshError(event.list, event.error) { addListRetry() }` (`addListRetry()` прибавляет `listRetries`):
   сбой списка показывается один раз, пока список не сообщит, что сбой прошёл (после поворота тот же сбой приходит
   снова), «Повторить» прибавляет счётчик, и упавший список сам вызывает `retry()` (`design/foundations.md`
   § Списки).
7. Снекбар с повтором не переживает экран: повтор запускается в `coroutineScope` логики, а при `onDestroy` логика
   закрывает свой снекбар (`NotifierManager.dismiss`). Повтор из уже закрытого экрана и не выполнился бы: scope
   отменён.
8. Сбой загрузки без контента (`Resource.Error`, `PagingLoadException` пустого списка) через `handleResponse` не идёт:
   экран держит его в состоянии и рисует `ErrorContentLayout` (правило 6 раздела «Правила»).

## Каркас Ui

```kotlin
@Preview
@Composable
fun PreviewXxxUi() {
    AppTheme {
        XxxUi(
            modifier = Modifier.background(AppTheme.color.basic.background),
            component = object : XxxComponent {
                @Composable
                override fun getUiState(): XxxContract.UiState = XxxContract.UiState()
            }
        )
    }
}

@Composable
fun XxxUi(
    modifier: Modifier = Modifier,
    component: XxxComponent
) {
    val appColor = AppTheme.color
    val uiState = component.getUiState()
    val obtainEvent = component::obtainUiEvent
    // вёрстка из токенов AppTheme
}
```

- У каждого Ui есть `@Preview` с именем `PreviewXxxUi` над самим Ui: тема + фейковый компонент с `UiState()`
  (`androidx.compose.ui.tooling.preview.Preview`, зависимость приходит от `wallify.compose`).
- Контракт — `object XxxContract` (или `interface`) в файле Ui; `UiState` помечаем `@Immutable`.
- `LaunchedEffect(Unit)` — для разовых эффектов экрана (например, задержка сплеша); ключ — то, что реально
  перезапускает эффект.
- Одноразовые события компонента собираем `collectComponentEvents { … }` / `collectAsEffect`.
- Каждый значимый блок вёрстки — отдельный composable без знания о компоненте: принимает данные и лямбды.
- Лямбды-обработчики и слоты в `val` внутри composable не складываем (`val onRefreshError: (…) -> Unit = { … }`,
  `val screens: @Composable (Modifier) -> Unit`): лямбду пишем прямо в аргументе вызова, повторяющуюся вёрстку
  выносим в приватный composable.
- Диалоги, шторки и оверлеи (`ScreenOverlay`, `GlassOverlay`, `DefaultDescriptionDialog`, `XxxManagerUi`) вызываем на
  верхнем уровне функции `XxxUi`, после основного контейнера экрана, а не внутри него. Так они не получают фон и
  отступы контейнера и не зависят от его размеров. Шторки и диалоги экрана идут через `ScreenOverlay`: их рисует
  `BlurHost` экрана корня рядом с источником блюра (`design/foundations.md` § Блюр).
- Решения не принимаем в Ui: что выбрать, что показать, доступно ли действие — поле `UiState` или обработка
  `UiEvent` в логике. Ui сообщает факт (`FirstTopicShown(slug)`, `ToggleColor(color)`), логика решает, что с ним
  делать; Ui остаются вёрстка, тексты из ресурсов и сопоставление состояния с параметрами компонентов.
- `CompositionLocal` — только для того, что нужно любому потомку поддерева и имеет хорошее значение по умолчанию
  ([CompositionLocal](https://developer.android.com/develop/ui/compose/compositionlocal)): токены темы, выбор
  пользователя на всё приложение из корня (`LocalPhotoGridMode`, `LocalWindowTypeOverride`), окружение
  (`LocalIsOnline`, `LocalNavigationPadding`) и инфраструктура дизайн-системы (`LocalOverlayHost`). Что нужно одному
  или двум компонентам, передаём параметром (`mode`, `onRefreshError`), а связь со скроллом — инверсией управления
  (`nestedScroll` на контейнере у владельца). Экран local заново не провайдит, чтобы поменять поведение одного
  компонента: передаёт параметр. Тема (`AppTheme`) — исключение: она, как `MaterialTheme`, явно провайдит все свои
  токены, чтобы вложенная тема сбрасывала переопределения снаружи и не зависела от значений по умолчанию local'ов.
- Значения, которые меняются на каждом кадре (прокрутка, анимация), в теле composable не читаем: передаём лямбдой и
  читаем в `graphicsLayer` / `drawBehind` / `offset { }`, иначе весь экран перекомпоновывается на каждом кадре.
- Данные для отрисовки готовит логика: адреса картинок по качеству, цвет, пропорции, инициалы, строки из нескольких
  полей (`@jane • Oslo`) — поля `UiState` или модели элементов списка (`PhotoCardUi`, `AvatarUi`,
  `design/foundations.md` § Изображения фото). В Ui их не вычисляем и `remember` по данным не заводим: `remember`
  в Ui — только для состояния самой вёрстки (анимация, измерение, контроллер), а производное от данных —
  `UiState` из логики. Так элемент списка получает модель и не захватывает `uiState`: с strong skipping лямбда,
  захватившая `uiState`, создаётся заново при любом его изменении (символ в поиске, в форме), и все видимые
  карточки перекомпоновываются. По той же причине в композиции не копируем постраничный список
  (`itemSnapshotList`): один элемент — `paging.peek(index)`.

## Общие блоки (`core:presentation`)

| Блок | Как использовать |
|---|---|
| `NotifierManager` | `showShort(text)` / `showLong(text)`; с кнопкой — `showWithAction(text, actionLabel, onAction)`, возвращает снекбар для `dismiss(model)`. Снекбар рисует корневой экран, всегда `GlassSnackbar`; кнопка закрывает его и вызывает `onAction`. Текст сбоя по типу ошибки с кодом — `showError(error)` (`extensions/ResponseErrorExt.kt`, `ResponseError.errorMessage()`), с повтором — `showErrorWithRetry` базовой логики; экраны для действий берут `handleResponse` (§ Ошибка действия). Пока нет сети корень снекбары экранов не показывает и сам держит `error_no_internet`. Сеть читает только `RootGraphLogic` |
| Шторка (`DefaultBottomSheet…`) | Компонент со своей логикой; открывать через его логику |
| Разрешения (`take_permission`, Android) | Только `androidMain`; на Desktop аналога нет |
| Свойства экрана для enum | Доменный enum в `core:model`, экранный `XxxUi` в `core:presentation` (подпись и другие свойства экрана). Связь — `toDomain()` / `toPresentation()` в файле `XxxUi`, одной строкой через `val domain` (`architecture/layers.md`). Образец — `ContentFilter` и `ContentFilterUi`. Фича свою функцию на доменный enum не пишет |

- Новый блок с общей логикой и состоянием — `core/presentation/.../components/units/<name>/` в слоях `domain` /
  `data` / `presentation`; классы блока помечаются аннотациями Koin, отдельный модуль не нужен: пакет уже
  сканирует `PresentationModule`.
- Корневой экран (`feature:root`) подписан на `NotifierManager` и `ConnectivityRepository`; фичи подписываться на
  них самостоятельно не должны.
- В `RootGraphUi` `NotifierManagerUi` вызывается после `Box`, в котором лежит `RootGraphStack`, а не внутри него:
  снекбар — оверлей над всем корнем, и фон или отступы стека на него не действуют.