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

## Новый экран в графе

1. Фича по образцу `splash` (`code/presentation.md`).
2. В `Config` графа — новый элемент, в `Child` — новый `class`, в `child(...)` — создание компонента.
3. В `RootGraphUi` (`when` по `Child`) — вызов `XxxUi`.
4. Переход: событие в логике → `obtainNavigateEvent` → `navigate(...)`.
5. Зависимость графа на модуль фичи — в `build.gradle.kts` графа; модуль фичи добавляется в `graphProjects` (`code/di.md`).

## Анимации

Переходы стека — `stackAnimation { fade() + scale() }` и подобные из Decompose Compose; значения длительности — из
`AppTheme.durationAnim`.