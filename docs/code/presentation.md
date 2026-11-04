# Экран: Component / Logic / Contract / Ui

Форма экрана и правила состояния. Образцы — `feature/splash` (простой экран), `feature/home` (экран с состоянием),
`feature/root` (граф с состоянием).

Related: `code/navigation.md`, `code/di.md`, `design/foundations.md`, `architecture/layers.md`.

## Форма экрана

```
feature/<name>/src/commonMain/kotlin/.../feature/<name>/
├── ui/XxxComponent.kt     интерфейс XxxComponent, XxxDefaultComponent, XxxLogic, XxxNavigationEvent
├── ui/XxxUi.kt            XxxContract (UiState, UiEvent) и composable XxxUi
└── XxxModule.kt           корневой класс Koin-модуля фичи (Logic помечается @Factory)
```

| Часть | Роль | Знает |
|---|---|---|
| `XxxComponent` (интерфейс) | Что Ui может у экрана просить: `getUiState()`, `obtainUiEvent(event)` | Contract |
| `XxxDefaultComponent` | Реализация: `ComponentContext`, получает `Logic` из Koin, слушает события навигации | Logic, Decompose |
| `XxxLogic` | Состояние экрана (`uiState: StateFlow`), обработка `UiEvent`, вызовы domain, `navigate(...)` | Contract, domain |
| `XxxContract` | `UiState : BaseContract.UiState`, `UiEvent : BaseContract.UiEvent` | ничего |
| `XxxUi` | Composable экрана | только интерфейс `XxxComponent` и Contract |

Создание компонента и логики:

```kotlin
override val logic: XxxLogic = instanceKeeper.getOrCreate { get<XxxLogic>() }
```

`instanceKeeper` сохраняет логику при пересоздании компонента.

## Правила

1. **Ui не знает про Logic и Koin.** Composable принимает `modifier: Modifier = Modifier` первым параметром и
   `component: XxxComponent` вторым. Состояние — `component.getUiState()`, действия — `component::obtainUiEvent`.
2. **Logic не знает про Compose.** Ни `@Composable`, ни `Color`, ни `Dp`, ни строк из ресурсов: в состоянии —
   данные и флаги, форматирование и тексты делает Ui.
3. Состояние — один `UiState` (`data class`, неизменяемый), обновляется через `MutableStateFlow.update { it.copy(…) }`.
   `MutableStateFlow` объявляется через `field` (`code/code-style.md` § Корутины и потоки), а не парой
   `_uiState` / `uiState`. Для Compose помечаем `@Immutable`, если в нём есть нестабильные типы.
   Каждое поле состояния меняется отдельным приватным методом `updateXxx(value)` с одним
   `uiState.update { it.copy(xxx = value) }` внутри; в местах вызова `it.copy(…)` не пишем. Публичные `showXxx()` /
   `hideXxx()` — тонкие обёртки над ним: `fun showDialog() = updateIsVisibleDialog(true)` (образец —
   `ApiErrorManagerLogic`). Если поля нужно менять только вместе, это один метод с говорящим именем.
4. Действия пользователя — `UiEvent` (`sealed interface`, `data object` / `data class`). Метод на каждое действие в
   Ui не заводим: один `obtainUiEvent`.
5. Одноразовые эффекты: навигация — `navigate(NavigationEvent)` логики; сообщение пользователю — `NotifierManager`;
   ошибка запроса — `ApiErrorManager`. Не `StateFlow<Boolean>` с ручным сбросом.
6. Данные приходят из use case'ов и репозиториев `core:domain` (`architecture/layers.md`). Ошибки — `Response`; логика
   решает, что показать (`handleResponse`), а не бросает исключения в Ui.
7. Работа с данными — на `dispatchers.io` внутри use case'а или репозитория; `Logic` лишь запускает её в
   `coroutineScope`.
8. Жизненный цикл — переопределяем `onCreate/onStart/onResume/onPause/onStop` в `Logic` (подписки, мониторинг сети),
   отписка — в симметричном методе. Свои подписки с `Component` не пишем.
9. Базовые алиасы (`BaseDefaultComponent`, `BaseLogic`, `BaseComponent`) подходят, когда у графа или экрана нет
   `ComponentEvent` и своего состояния; иначе берём `…Core` с явными типами (как `feature:root`).
10. Для списка с подгрузкой `Logic` держит `Flow<PagingData<T>>.cachedIn(coroutineScope)`
    (`architecture/data.md` § Пагинация).

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

## Общие блоки (`core:presentation`)

| Блок | Как использовать |
|---|---|
| `ApiErrorManager` | `emitEvent(ShowDialog / HideDialog / SessionExpired)` из любого слоя; диалог рисует корневой экран |
| `NotifierManager` | `showShort(text)` / `showLong(text)`; снекбар рисует корневой экран |
| Шторка (`DefaultBottomSheet…`) | Компонент со своей логикой; открывать через его логику |
| Разрешения (`take_permission`, Android) | Только `androidMain`; на Desktop аналога нет |

- Новый блок с общей логикой и состоянием — `core/presentation/.../components/units/<name>/` в слоях `domain` /
  `data` / `presentation`; классы блока помечаются аннотациями Koin, отдельный модуль не нужен: пакет уже
  сканирует `PresentationModule`.
- Корневой экран (`feature:root`) подписан на `ApiErrorManager`, `NotifierManager` и `ConnectivityRepository`;
  фичи подписываться на них самостоятельно не должны.