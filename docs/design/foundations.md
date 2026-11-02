# Дизайн-система: токены и компоненты

Как верстать экраны. Модуль — `core:design-system`; всё, что нужно экрану, доступно через `AppTheme`.

Related: `code/presentation.md`, `code/resources.md`.

## Токены

Приложение оборачивается в `AppTheme { … }` (тёмная и светлая схемы выбираются по системной теме). Внутри значения
читаются через объект `AppTheme`:

| Токен | Что |
|---|---|
| `AppTheme.color` | Цвета: `basic` (primary, background, error, white, black, dark, transparent, card, button), `text` (title, subtitle, common, fieldType, fieldHint, reversed, onReversed), `icons` (care, nav) |
| `AppTheme.typography` | Стили `h1`…`h8`, у каждого `regular`, `medium`, `semiBold`, `bold` |
| `AppTheme.padding`, `corner`, `alpha`, `borderWidth`, `elevation`, `blur`, `scale`, `durationAnim`, `zIndex` | Универсальные шкалы: отступы, скругления, прозрачности, обводки, тени, размытие, масштабы, длительности анимаций, слои |
| `AppTheme.size` | Размеры: шкала иконок и размеры конкретных компонентов (переключатель, чекбокс, разделитель, индикаторы, шторка) |
| `AppTheme.gradient` | Градиенты общего назначения (затемнение под текстом) |
| `AppTheme.ratio` | Узкие пропорции: доля ширины карточки и соотношения сторон изображений |

Два вида токенов:

- **Универсальные шкалы** (`padding`, `corner`, `alpha`, `borderWidth`, `elevation`, `blur`, `scale`, `durationAnim`,
  `zIndex`, шкала иконок) хранятся полностью, даже если часть значений пока не используется: это словарь проекта.
- **Узкие токены** — под конкретный экран или компонент (`ratio`, размеры компонента в `AppSizes`, специальные цвета и
  градиенты). Добавляются вместе с первым использованием и убираются, когда использование исчезло. У каждой группы
  узких токенов рядом лежит KDoc с примером (образец — `AppRatios`).

Пример узкого токена:

```kotlin
val appRatio = AppTheme.ratio

AsyncImage(
    modifier = Modifier
        .fillMaxWidth(appRatio.card.width)
        .aspectRatio(appRatio.image.portrait),
    model = url,
    contentDescription = null
)
```

## Выбор темы

Платформенные приложения оборачивают `RootGraphUi` в `AppTheme { … }`; тема следует за системной. Выбор темы
пользователем пока не хранится. Когда он понадобится, поле добавляется в `UserSettings` (`core:model`), подписка на
него — в `RootGraphLogic`, а приложения берут значение из состояния корневого компонента и передают `isDarkTheme` в
`AppTheme`.

## Адаптивность

Экран подстраивается под размер окна (телефон, планшет, окно десктопа) библиотеками Material 3 Adaptive от Google, портом
JetBrains для Compose Multiplatform. Они подключены в `core:design-system` как `api`, версия одна в каталоге
(`material3Adaptive`):

| Артефакт | Для чего |
|---|---|
| `adaptive` | `currentWindowAdaptiveInfo()`: класс размера окна и положение складки |
| `adaptive-layout` | Готовые раскладки: `ListDetailPaneScaffold`, `SupportingPaneScaffold` |
| `adaptive-navigation` | Навигатор по панелям для этих раскладок |
| `material3-adaptive-navigation-suite` | `NavigationSuiteScaffold`: нижняя панель, рейл или drawer по размеру окна |

Для простого ветвления есть `rememberWindowType()` (`Compact` / `Medium` / `Expanded`) из `ui/adaptive`. Сырые
`WindowSizeClass` и числа ширины в экранах не пишем: нужна новая граница — расширяем `WindowType`.

## Блюр

Размытый фон вместо затемнения (диалоги, шторки и другие оверлеи) делает библиотека Haze (`haze` и `haze-blur`, одна
версия в каталоге). Весь код блюра живёт в `core:design-system/.../ui/blur`; вне этого пакета `hazeSource`, `hazeBlur`
и `rememberHazeState` не используем.

| Что | Для чего |
|---|---|
| `BlurHost` | Создаёт состояние блюра для всего внутри. Один на приложение, в `RootGraphUi` |
| `Modifier.blurSource()` | Помечает содержимое, которое будет размыто (в корне — стек экранов) |
| `Modifier.blurBackdrop(level, alpha)` | Рисует размытую копию того, что под оверлеем. `alpha` 0…1 для появления и исчезновения |
| `BlurLayout(content, overlay)` | Блюр внутри экрана: `content` — источник, `overlay` (диалог, шторка) рисуется поверх |
| `BlurLevel` | `Small` / `Medium` / `Large`, значения берутся из `AppTheme.blur` |

Правила:

1. Затемнение скримом `Color.Black.copy(alpha = …)` под диалогом или шторкой не пишем: используем `blurBackdrop`.
2. Оверлей не лежит внутри источника, который он размывает: источник и оверлей — соседи в одном `Box`
   (`BlurLayout` делает это сам). Эффект видит только источники, лежащие ниже него по `zIndex`.
3. Корневой источник — `BlurSourceZIndex.Root`, источник внутри экрана — `BlurSourceZIndex.Screen`; другие значения
   не вводим.
4. Силу блюра выбираем уровнем `BlurLevel`, радиусы и цвета подкраски в экранах не пишем. Новый вид — новый уровень в
   `ui/blur`.
5. Без `BlurHost` (превью, тесты) и там, где платформа не умеет блюр (Android 11 и ниже), `blurBackdrop` сам рисует
   полупрозрачный тёмный скрим.
6. Блюр дорогой: размываем только оверлеи, а не каждый элемент списка.

Пример диалога внутри экрана:

```kotlin
BlurLayout(
    modifier = modifier.fillMaxSize(),
    overlay = {
        if (uiState.isVisibleDialog) {
            DefaultDialog(
                title = "Title",
                onDismiss = { obtainEvent(UiEvent.HideDialog) }
            )
        }
    }
) {
    // содержимое экрана
}
```

## Правила

1. В экранах нет `Color(0x…)`, `TextStyle(fontSize = …)`, `FontWeight`, голых `N.dp` и `N.sp`. Нужного значения нет в
   токенах — добавляем токен в `design-system/.../theme/compositions` (одно место для всех экранов), а не локальную
   константу.
2. **Прямой `AppTheme.…` — только в значениях параметров по умолчанию в сигнатуре composable** (там локальных
   переменных ещё нет): `titleStyle: TextStyle = AppTheme.typography.h5.semiBold`. В теле composable токены берём
   полным вариантом через локальные `val` в начале функции и пишем по ним:

   ```kotlin
   val appColor = AppTheme.color
   val appPadding = AppTheme.padding
   val appTypography = AppTheme.typography

   Text(
       text = title,
       style = appTypography.h5.semiBold,
       color = appColor.text.title
   )
   ```

   Имена локальных значений: `appColor`, `appTypography`, `appPadding`, `appCorner`, `appSize`, `appAlpha`,
   `appElevation`; брать только те, что нужны. `@Preview` исключение: он не имеет других значений и может писать
   `AppTheme.color…` напрямую. Изменение цвета стиля — `withColor`, прозрачности цвета — `withAlpha`
   (`AppThemeExt.kt`).
3. Кратные отступы — расширения `Dp` (`half`, `times2`, …) из `designsystem.extensions`, а не пересчёт руками.
4. Готовый компонент есть — используем его, а не верстаем заново (список ниже). Нет подходящего — новый компонент
   добавляется в `design-system/.../ui/<name>/` без бизнес-логики: данные и лямбды на входе.
5. Модификатор — первый параметр composable: `modifier: Modifier = Modifier`, применяется к корневому элементу;
   обязательные параметры идут после него и вызываются по имени.
6. Клики по кнопкам и карточкам — `debouncedClickable` (защита от двойных нажатий), а не `clickable`.
7. Анимации включаются флагом темы (`animEnabled`); длительности — `AppTheme.durationAnim`.

## Готовые компоненты

Что искать в `core:design-system/.../ui/`, прежде чем верстать своё:

- блюр фона для оверлеев (`blur`: `BlurHost`, `blurSource`, `blurBackdrop`, `BlurLayout`);
- кнопки (`button`), поля ввода (`edit_text`), переключатели и чекбоксы (`switch`, `checkbox`), табы (`tab_row`),
  выбор значения (`selection`);
- карточки (`card`), разделители (`divider`), бейдж (`badge`), нижняя панель (`bottom_bar`), верхняя панель
  (`top_bar`: `DefaultTopBar`), диалоги (`dialog`: `DefaultDialog`, `DefaultDescriptionDialog`);
- индикаторы и загрузчики (`indicator`, `full_screen_loader`), скелетон-заглушка (`shimmer`: `ShimmerBox`),
  pull-to-refresh (`pull_to_refresh`);
- изображения (`image`), иконки (`icon`: `AppIcons`, `iconContainer`), тени (`shadow`), модификаторы и анимации
  (`modifier`, `animation`, `compose`), раскладки (`layout`);
- клики (`click`), тексты с ссылками (`text`), информационные блоки (`info`).

## Списки

- Постраничный список — `collectAsLazyPagingItems()` в Ui и `pagingItemsWithInsert` внутри `LazyColumn` /
  `LazyVerticalStaggeredGrid` (`ui/paging`): ключ элемента — `paging.keyAt(index) { it.id }`, вставка баннера на
  нужный индекс — `PagingListInsert` / `PagingStaggeredInsert`.
- Состояния списка — расширения `LazyPagingItems`: `isRefreshing`, `isAppending`, `refreshError`, `appendError`,
  `isEmpty`; повтор после ошибки — `paging.retry()`.
- Пока список грузится, показываем `ShimmerBox`, а не пустой экран.
- Системные отступы и клавиатура — `WindowInsets`, а не константы.