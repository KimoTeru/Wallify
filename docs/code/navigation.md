# Навигация: Decompose

Как устроены стек экранов и переходы. Образец — `feature/root` (граф) и `feature/splash` (экран).

Related: `code/presentation.md`, `code/di.md`.

## Правила

1. Навигация — Decompose `StackNavigation` внутри компонента графа. Граф — наследник
   `BaseNavigationDefaultComponent` (или `…Core`, если у графа есть своё состояние, как у корня).
2. Конфигурация экрана — `@Serializable sealed interface Config : BaseNavigationConfig` внутри компонента графа;
   `data object` для экранов без аргументов, `data class` — с аргументами. Аргументы — только сериализуемые данные
   (id, строки), не модели с логикой.
3. `child(config, componentContext)` создаёт дочерний компонент и передаёт ему **колбэки навигации**
   (`navigateNext = { … }`), а не ссылку на граф. Экран не знает, куда идёт дальше.
4. Переход инициирует `Logic` через `navigate(NavigationEvent)`; компонент ловит событие в `obtainNavigateEvent` и
   вызывает `navigate(config)` / `navigateClearBackStack(config)` графа.
5. События навигации — `sealed interface XxxNavigationEvent : BaseNavigationEvent`, по одному на экран/граф.
6. Назад — `onBackClicked()`; пустой стек закрывает приложение через колбэк `finishApp`, который даёт платформа
   (Android: `finishAffinity`, Desktop: `exitApplication`).
7. Корневой компонент создаётся один раз платформой и получает `componentContext` из Decompose
   (Android: `defaultComponentContext()`, Desktop: `DefaultComponentContext(LifecycleRegistry)` +
   `LifecycleController`). Desktop создаёт его на UI-потоке (`runOnUiThread`).
8. Системная кнопка «назад» на Android обрабатывается Decompose (`handleBackButton = true`); свои обработчики —
   через `PlatformBackHandler`.

## Экран по сессии

Какой экран показать — основную навигацию или вход — решает не сам экран, а корневая логика (`RootGraphLogic`): после
сплеша она подписывается на `AuthRepository.isSignedIn` и на каждое значение шлёт событие навигации `Main` (сессия
есть) или `Auth` (нет); граф отвечает `navigateClearBackStack`. Поэтому вход, выход и истёкшая сессия
(`SessionExpired`) сами переводят на нужный экран. Экраны входа и выхода сами никуда не переходят: они меняют сессию, а граф реагирует.
Сплеш сообщает о конце вызовом `logic.onSplashFinished()`; если приложение восстановлено не на сплеше, граф вызывает
его сам при создании.

## Новый экран в графе

1. Фича по образцу `splash` (`code/presentation.md`).
2. В `Config` графа — новый элемент, в `Child` — новый `class`, в `child(...)` — создание компонента.
3. В `RootGraphUi` (`when` по `Child`) — вызов `XxxUi`.
4. Переход: событие в логике → `obtainNavigateEvent` → `navigate(...)`.
5. Зависимость графа на модуль фичи — в `build.gradle.kts` графа; модуль фичи добавляется в `graphProjects` (`code/di.md`).

## Вложенный граф вкладок

Основная навигация вошедшего пользователя — отдельный граф `feature:main` (`DefaultMainGraphComponent`) со своим
стеком вкладок. Корень зависит от него и кладёт вкладки одним ребёнком стека; сами вкладки от корня не зависят.

1. Переключение вкладки — `navigation.bringToFront(config)`: вкладки остаются в стеке и хранят состояние, системная
   «назад» возвращает на предыдущую. Выбранную вкладку берём из активного ребёнка стека
   (`stack.subscribeAsState()`), а не храним в состоянии: иначе «назад» рассинхронизирует её.
2. Экран вкладки ничего не знает о корне: переходы наружу (открыть фото, профиль) идут колбэками, которые граф
   вкладок получает от корня и передаёт детям (`openPhoto`). Фото и чужой профиль лежат в стеке корня поверх всей
   навигации (`Config.Photo(photoId)`, `Config.Profile(username)`), у них есть кнопка назад.
3. Один и тот же экран может жить в двух местах: `feature:profile` — вкладка «Студия» (`ProfileTarget.CurrentUser`,
   без кнопки назад) и чужой профиль в корне (`ProfileTarget.Username`, `navigateBack` задан). Различие передаётся
   аргументом компонента.
4. Аргумент экрана, который нужен логике (`photoId`, `ProfileTarget`), компонент передаёт так:
   `get<XxxLogic> { parametersOf(arg) }`, а в конструкторе логики он помечен `@InjectedParam`; параметр не должен
   быть `null`, для необязательного заводим тип-цель.
5. Окно вкладок адаптируется (`GlassDock` / `GlassRail` / `GlassDrawer`, `design/foundations.md` § Стекло).

## Анимации

Переходы стека — `stackAnimation { fade() + scale() }` и подобные из Decompose Compose; значения длительности — из
`AppTheme.durationAnim`.