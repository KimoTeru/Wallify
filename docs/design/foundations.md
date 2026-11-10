# Дизайн-система: токены и компоненты

Как верстать экраны. Модуль — `core:design-system`; всё, что нужно экрану, доступно через `AppTheme`.

Related: `code/presentation.md`, `code/resources.md`.

## Токены

Приложение оборачивается в `AppTheme { … }` (тёмная и светлая схемы выбираются по системной теме). Внутри значения
читаются через объект `AppTheme`:

| Токен | Что |
|---|---|
| `AppTheme.color` | Цвета: `basic` (primary, background, error, white, black, dark, transparent, card, button), `text` (title, subtitle, common, fieldType, fieldHint, reversed, onReversed), `icons` (care, nav), `glass` (слои стекла) |
| `AppTheme.typography` | Стили `h1`…`h8` и `caps` (микро-подписи, текст пишем `.uppercase()`), у каждого `regular`, `medium`, `semiBold`, `bold`; шрифт — Plus Jakarta Sans из `core:resources` |
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

Тема — «Ночь» и «День» палитры Crystalline; пользователь выбирает `ThemeMode` (`System` / `Night` / `Day`) в
настройках, значение лежит в `UserSettings.themeMode`. `RootGraphLogic` подписан на него и кладёт в состояние корня;
платформенные приложения оборачивают `RootGraphUi` в `AppTheme(isDarkTheme = rootComponent.isDarkTheme())`
(`RootGraphTheme.kt` в `feature:root`), сами `ThemeMode` не знают. Android-приложение дополнительно переключает цвет
значков системных панелей (`enableEdgeToEdge` в `MainActivity`), потому что тема приложения может отличаться от
системной.

## Нет сети

Отсутствие сети — состояние, а не событие, поэтому его не показываем модальным окном (так в Material и в гайдах по
офлайну): снекбар `error_no_internet` держит `RootGraphLogic`, пока сети нет, а у экрана с
неудавшейся загрузкой — инлайн-ошибка с «повторить».

1. Текст ошибки списка или экрана берём из `networkErrorTitle()` / `networkErrorDescription()`: без сети это «нет
   интернета» и обещание, что загрузится само, иначе общий сбой. Корень кладёт состояние сети в `LocalIsOnline`.
2. Экран, который читает данные, подключает `RetryOnReconnect(isFailed) { retry }`: загрузка повторяется сама, когда
   сеть вернулась, и только тогда (сбой при сети в цикле не повторяем). Запись (скачать, добавить в коллекцию) так не
   повторяем: неизвестно, дошёл ли первый запрос, повтор остаётся за пользователем.
3. Неудавшаяся запись без сети отдельного окна не открывает: снекбар про интернет уже виден. Та же запись при сети
   показывает `error_dialog_description` тем же снекбаром и прячется сама.

## Заголовок окна Desktop

Заголовок окна красим под тему, а не оставляем системно-серым. На macOS заголовок прозрачный
(`apple.awt.transparentTitleBar`, `apple.awt.fullWindowContent`), приложение рисуется под ним и начинается с отступа
`AppTheme.size.window.titleBarHeight`; светлые или тёмные кнопки окна задаёт `apple.awt.windowAppearance` по схеме
темы. На Windows и Linux окно без системной рамки (`undecorated`): `WindowFrame` в `desktopApp` рисует свой заголовок
с кнопками (свернуть, развернуть или восстановить, закрыть), перетаскивание и разворот двойным кликом, а края и углы
окна для изменения размера (`resizeEdge`, `resizeCorner`) рисуют невидимые полосы, потому что у окна без рамки своих
краёв нет. Размеры берём из `AppTheme.size.window`, платформу определяет `desktopApp/main.kt`.

## Стекло

Вид интерфейса — «кристаллическое стекло»: полупрозрачные слои на глубоком фоне, волосяная рамка со светлым верхом,
крупные скругления. Цвета слоёв — `AppTheme.color.glass`. Скругление, которого нет шагом шкалы, складываем из
`AppTheme.corner`: карточки — `large + smallSpecial`, шторки — `large + small`, диалоги — `large + medium`, поля —
`medium`; капсулы — `CircleShape`.

| Что | Как |
|---|---|
| Поверхность | `Modifier.glassSurface(level, shape, edges)`; `GlassLevel`: `Sub` (утопленные панели), `Standard` (карточки), `Elevated` (приподнятые элементы), `Floating` (панели поверх фото: док, строка поиска), `Solid` (шторки и диалоги, непрозрачные). Линия — `edges` (`GlassEdge`, по умолчанию все стороны и по форме); выбранные стороны рисуются прямыми, `ScrollBlurBar` оставляет только низ. Контейнер с содержимым — `GlassCard` |
| Кнопки | `GlassButton` (`Glass`, `Solid` для главного действия, `Ghost`), `GlassIconButton` |
| Выбор | `GlassChip` (выбранный — залитый), `GlassSegmentedControl` |
| Поиск | `GlassSearchField`: отправка клавишей поиска, крестик очистки |
| Ввод в форме | `GlassTextField`: подпись над полем, утопленная панель (`GlassLevel.Sub`); `maxLines > 1` — многострочное поле. Форма лежит в `GlassOverlay` |
| Навигация | `GlassDock` (телефон), `GlassRail` (планшет), `GlassDrawer` (большое окно); пункты — `GlassNavItem` |
| Шторка / диалог | `GlassOverlay(isSheet, onDismiss) { close -> … }`: шторка на `Compact`, диалог на остальных. Экран вкладки кладёт её в `ScreenOverlay { … }`: навигация рисует её над доком и размывает всё под ней. Стекло шторки уходит под системную панель, содержимое остаётся выше неё, нижняя линия выключена |
| Прячущиеся панели | `LocalNavigationVisibility` + `rememberHideOnScrollConnection`: список вешает на себя `nestedScroll`, док и верхняя панель экрана скрываются при прокрутке вниз и возвращаются при прокрутке вверх |
| Цифры | `GlassStat` (значение и подпись), `Sparkline` (график ряда без осей) |
| Фото | `PhotoCard` (карточка с автором), `PhotoGridLayout` (колонки по ширине окна), `PagingPhotoGrid<T>` (постраничная сетка со всеми состояниями), `UserAvatar`; компоновку (`PhotoGridMode`: мозаика, сетка, список) и число колонок берут из `LocalPhotoGridMode` и `LocalPhotoGridColumns`, их задаёт корень по настройкам пользователя — экран их не выбирает; выбор компоновки рисует `PhotoLayoutCard` (миниатюра раскладки вместо текстового переключателя). Выбор пользователя действует на ленту; «Кураторские», «Случайные» и профиль всегда показывают сетку одинаковых карточек (`LocalPhotoGridMode provides PhotoGridMode.Grid` в самом экране) |

Правила:

1. Док, рейл и drawer рисует только навигация вкладок (`feature:main`). Она кладёт в `LocalNavigationPadding`
   место, занятое плавающим элементом; экран берёт отступы через `rememberScreenPadding()` (`header`, `list(gap)`,
   `full`), чтобы список уходил под док, а не заканчивался над ним, и не считает системные панели сам.
   Горизонтальный край контента, дока и рейла — `AppTheme.padding.medium` (16 dp). Промежуток между колонками
   сетки фото — тот же.
2. Панели и кнопки поверх списка или фото (док, рейл, поиск, чипы и кнопки верхней панели, «назад» на фото) —
   `GlassLevel.Floating` (у `GlassButton`, `GlassChip`, `GlassIconButton` это параметр `level`): матовое стекло, `frostedBackdrop` размывает то,
   что под ними, на непрозрачной подложке цвета фона (иначе резкий контент просвечивает и панель выглядит просто
   полупрозрачной). Источник размытия — сам список (`PhotoGridLayout` сам помечает себя `blurSource`), а панель — его
   сосед в `Box`, не потомок: внутри источника эффект его не видит (§ Блюр). Экран без сетки (настройки) помечает
   свою прокручиваемую колонку `blurSource(BlurSourceZIndex.Screen)` сам. Тон панели (`glass.dock`) плотный, чтобы
   содержимое под ней не читалось, даже если блюр слабый.
   Шапки и формы на широком окне не растягиваем: ограничиваем `AppTheme.size.panel.contentMaxWidth` и центрируем
   (образец — `ProfileHeader`).
   Список, над которым плавает панель (заголовок, чипы, поиск), начинается под ней и уходит под неё при прокрутке:
   ленту, настройки, детальное фото, подборки и случайные собираем через один `FloatingBarScreen(scrolledPx, bar)`:
   экран кладёт в `bar` свой заголовок или кнопку и передаёт, насколько прокручен список (`ScrollState.value` или
   `LazyStaggeredGridState.scrolledPx()`). Фон — `ScrollBlurBar`: до прокрутки прозрачный, дальше то же матовое
   стекло, что у дока, с линией только по нижнему краю. Док прячется при прокрутке вниз, топбар остаётся. Чипы и кнопки внутри без своего блюра.
   Настройки и детальное фото держат заголовок и «назад» в `ScrollBlurBar`: до прокрутки фон прозрачный, и по мере
   скролла на высоту панели он доходит до того же матового стекла. Профиль только вешает на прокрутку
   `rememberHideOnScrollConnection`, чтобы пряталась навигация.
   На широком окне шапка деталей фото не матовеет при прокрутке и само фото не источник блюра: иначе
   размытие растекается фоном на весь экран. Фон остаётся сплошным.
3. Цвет основной кнопки и текст на ней — `basic.button.primary` и `text.reversed`: на ночной схеме кнопка белая, на дневной
   тёмная, поэтому белый или чёрный текст на ней не пишем.
4. Диалог и шторка не растягиваем на всё окно: диалог — не шире `AppTheme.size.panel.dialogMaxWidth` (560 dp), шторка —
   `sheetMaxWidth` (640 dp), на широком окне они по центру (так в Material 3). Новый диалог или шторка берут эти
   токены, а не `fillMaxWidth()`.
5. Цвет, которого нет в токенах (например, цвет фильтра по цвету фото), экран сам не вводит: нет токена — нет цветового
   пятна, используем текст.

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

Для простого ветвления есть `rememberWindowType()` (`Compact` / `Medium` / `Expanded`) из `ui/adaptive`. Выбор компоновки
показываем только там, где устройство может достичь `Expanded` (`rememberCanUseExpandedLayout()`: Desktop всегда,
Android — от ширины планшета, телефон нет). Пользователь
может принудительно выбрать компоновку экрана в настройках (`ScreenLayout`): корень кладёт её в
`LocalWindowTypeOverride`, а `rememberWindowType()` её учитывает — поэтому ветвим только через него, не читая размер
окна напрямую. Окно Desktop при смене выбора (но не при запуске) само принимает типичную ширину этой компоновки
(`AppTheme.size.window`): корень отдаёт приложению `requestedWindowType()` и `isSettingsLoaded()`. Там же
минимальные ширина и высота окна (`minWidth`, `minHeight`): окно Desktop нельзя ужать меньше. При принудительных
Medium и Expanded минимальная ширина растёт до `mediumMinWidth` и `expandedMinWidth`: навигации (рейлу, drawer) нужно
место, иначе содержимое экранов сжимается до нескольких символов в строке. Сырые
`WindowSizeClass` и числа ширины в экранах не пишем: нужна новая граница — расширяем `WindowType`.
Drawer и рейл стоят у начала окна и отступают от выреза камеры (`WindowInsets.safeDrawing`, сторона Start):
в горизонтальном положении камера на том краю, где в вертикальном была строка состояния. Короткое окно
(высота ниже средней границы, `rememberIsShortWindow()`) держит шапку в один компактный ряд: крупный
заголовок и второй ряд туда не помещаются.

## Изображения фото

Качество картинок выбирает пользователь: три выбора в `UserSettings` (`listImageQuality`, `detailImageQuality`,
`downloadImageQuality`). `ImageQuality.Original` — только для скачивания: на экране это огромный файл без видимой
разницы. Адрес по качеству — одна функция `PhotoUrls.forQuality(quality)` из `core:domain`; поля `urls.regular` и
другие в экранах не читаем.

1. Качество — данные экрана, а не окружение дерева: `Logic` экрана берёт его из `ObserveUserSettingsUseCase` в свой
   `UiState`, Ui передаёт его явно. `CompositionLocal` для него не заводим: он для того, что может понадобиться
   любому потомку (тема, раскладка сетки), а качество нужно нескольким экранам и делает их зависимости неявными
   ([CompositionLocal](https://developer.android.com/develop/ui/compose/compositionlocal)).
2. Фото показываем `ProgressiveAsyncImage` (`ui/image`; `PhotoCard` уже делает это): цвет фото, затем самая маленькая
   версия (`ImageQuality.Low`), затем выбранная проявляется поверх, и маленькая уходит из композиции — карточка
   не держит две картинки. Маленькая не грузится, если совпадает с выбранной или выбранная уже в памяти Coil. На экране
   фото `placeholderKey` — адрес той же картинки из списка
   ([Coil recipes](https://coil-kt.github.io/coil/recipes)).
3. Размер картинки Coil берёт у вида; `Size.ORIGINAL` и `size(...)` в запросах не задаём.

## Блюр

Размытый фон вместо затемнения (диалоги, шторки и другие оверлеи) делает библиотека Haze (`haze` и `haze-blur`, одна
версия в каталоге). Весь код блюра живёт в `core:design-system/.../ui/blur`; вне этого пакета `hazeSource`, `hazeBlur`
и `rememberHazeState` не используем.

| Что | Для чего |
|---|---|
| `BlurHost` | Создаёт состояние блюра для всего внутри. По одному на каждый экран корневого стека (`RootGraphStack`): состояние живёт там же, где источник и плашки (так делает сам Haze), иначе при переходе плашки уходящего экрана размывают приходящий и блюр «перескакивает». Над стеком ничего не размывается, поэтому общего хоста и источника у корня нет |
| `Modifier.blurSource()` | Помечает содержимое экрана, которое будет размыто |
| `Modifier.blurBackdrop(level, alpha)` | Рисует размытую копию того, что под оверлеем. `alpha` 0…1 для появления и исчезновения |
| `BlurLayout(content, overlay)` | Блюр внутри экрана: `content` — источник, `overlay` (диалог, шторка) рисуется поверх |
| `BlurLevel` | `Small` / `Medium` / `Large`, значения берутся из `AppTheme.blur` |

Правила:

1. Затемнение скримом `Color.Black.copy(alpha = …)` под диалогом или шторкой не пишем: используем `blurBackdrop`.
2. Оверлей не лежит внутри источника, который он размывает: источник и оверлей — соседи в одном `Box`
   (`BlurLayout` делает это сам). Эффект видит только источники, лежащие ниже него по `zIndex`.
3. Источник по умолчанию — `BlurSourceZIndex.Root`, источник содержимого экрана под его оверлеями —
   `BlurSourceZIndex.Screen`; другие значения не вводим.
4. Силу блюра выбираем уровнем `BlurLevel`, радиусы и цвета подкраски в экранах не пишем. Новый вид — новый уровень в
   `ui/blur`.
5. Без `BlurHost` (превью, тесты) и там, где платформа не умеет блюр (Android 11 и ниже), `blurBackdrop` сам рисует
   полупрозрачный тёмный скрим.
6. Стиль блюра задаётся только в `blurBackdrop`: непрозрачная подложка цвета фона (`backgroundColor`), без шума
   (`noiseFactor(0f)`) и с тоном фона поверх. Подложка нужна, потому что источник блюра лежит над фоном экрана:
   без неё размытая копия полупрозрачна, а резкий контент просвечивает «призраком».
7. Блюр дорогой: размываем оверлеи и несколько плавающих панелей (`frostedBackdrop`), а не каждый элемент списка;
   плашка автора на карточке фото лежит внутри источника и потому остаётся плотной тёмной, без блюра.
8. Производительность ([Haze performance](https://chrisbanes.github.io/haze/latest/performance/)):
   - Стекло плавающих панелей (`frostedBackdrop`) — `HazePerformanceMode.Performance`: панель перерисовывается на
     каждом кадре прокрутки, а при её радиусе разницы с `Balanced` не видно. Оверлеи (`blurBackdrop`) остаются на
     режиме по умолчанию.
   - Эффект существует, только пока поверхность видна: стекло создаётся условно, а не прячется прозрачностью.
   - Прогрессивный блюр не используем: для затухания эффекта берём `mask`.
   - Источник один на экран, вложенных источников в одном `BlurHost` не заводим.

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
   константу. Скругление, которое складывается из шагов `AppTheme.corner`, новым именем не заводим: пишем сумму
   (`large + small`).
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
- стеклянные компоненты (`glass`: поверхности, кнопки, чипы, поиск, док / рейл / drawer, оверлей, `GlassStat`,
  `ScreenPadding`), карточка фото и сетка (`photo`), аватар (`avatar`), график (`chart`);
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
- Список фото — `PagingPhotoGrid` (все состояния внутри); если сетке нужен свой заголовок или смешанные элементы,
  берём `PhotoGridLayout` и добавляем `item(span = StaggeredGridItemSpan.FullLine)` (образец — `feature:profile`).
  Текст из ресурсов (`stringResource`) вычисляем до лямбды `LazyStaggeredGridScope`: она не composable.
- Пока список грузится, показываем `ShimmerBox`, а не пустой экран.
- Состояние прокрутки сетки — `rememberPhotoGridState(keys…)`: в ключах всё, что заменяет список целиком (тема,
  запрос и фильтры, вкладка, новый набор). Новый список открывается сверху, тот же после поворота — на прежнем месте.
- `null`-элемент списка из Room рисуем `PhotoCardPlaceholder`, а не пропускаем: элемент нулевой высоты раскладывает
  в видимую область сотни заглушек и тянет загрузку всего списка. `PagingPhotoGrid` делает это сам.
- Список с данными бэкенда обновляется жестом pull-to-refresh (`DefaultPullToRefreshBox`, отступ индикатора — под
  плашку экрана). `PagingPhotoGrid` делает это сам; в своей сетке оборачиваем `PhotoGridLayout`.
- Системные отступы и клавиатура — `WindowInsets`, а не константы.