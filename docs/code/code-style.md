# Стиль кода

Стиль проекта и готовые утилиты, форматирование, имена, корутины, время, `expect`/`actual`. Относится ко всему
Kotlin-коду.

Related: `code/presentation.md`, `code/di.md`, `architecture/overview.md` § Платформы.

## Стиль проекта и готовые утилиты

Новый код должен выглядеть так же, как соседний: тот же слой, та же форма, те же приёмы.

1. Перед правкой прочитай два-три соседних файла того же слоя (экран, репозиторий, API-класс) и повтори их форму:
   порядок членов, имена, локальные алиасы, способ обработки результата.
2. Не пиши по-своему то, для чего в проекте уже есть утилита. Сначала поиск по `core/common` (`rg` по имени и по
   смыслу), потом свой код. Типичные замены:

   | Не пишем | Пишем |
   |---|---|
   | `.map { … }.distinctUntilChanged()` | `.mapDistinctBy { … }` |
   | `combine(…) { … }.stateIn(…)` | `combineState(scope, …, initialValue) { … }` |
   | пара `_uiState` + `uiState = _uiState.asStateFlow()` | `field` (§ Корутины и потоки) |
   | `try { … } catch (e: Exception)` вокруг запроса | `responseOf { … }`, `executeRequest { … }` |
   | `Dispatchers.IO` / `Main` | `AppDispatchers` |
   | `""`, `"/"`, `"_"`, `"; "` | `String.Empty`, `String.Slash`, `String.Underscore`, `String.SemicolonSpace` |

3. Утилиты, нужной для повторяющегося приёма, нет — добавляем её в `core:common` (с KDoc, если контракт неочевиден) и
   пользуемся ею, а не копируем приём по файлам. Модуль, которому она нужна, подключает `core:common`.
4. Если соседний файл нарушает правила из `docs/`, прав док; соседний файл поправь отдельным коротким изменением или
   сообщи об этом.

## Форматирование

Стиль проекта — привычный нам стиль экранов и компонентов: узнаваемая форма Ui, Contract, Logic и локальные алиасы
токенов темы (`design/foundations.md`). Новый код пишем так же и подстраиваемся под соседние файлы.

- Основа — официальный стиль Kotlin (`kotlin.code.style=official`), отступ 4 пробела, строка до 120 символов;
  `.editorconfig` в корне задаёт то же самое для IDE.
- **Конец файла.** Файл заканчивается сразу после последнего символа кода: ни пустых строк, ни отступа снизу.
  Финальный перевод строки не добавляем (`insert_final_newline = false`). Это правило для каждого файла: Kotlin,
  Gradle, Markdown, XML, TOML, JSON.
- **Trailing comma.** В объявлениях, где каждый параметр или элемент стоит на своей строке, после последнего ставим
  запятую: параметры функции и конструктора, поля `data class`, варианты `enum`, параметры `sealed`-вариантов. В
  вызовах (аргументы функции, именованные аргументы, `Koin`-модули) запятую после последнего аргумента не ставим.
  `.editorconfig` задаёт то же (`ij_kotlin_allow_trailing_comma = true`, `…_on_call_site = false`), поэтому Reformat
  Code приводит код к этому виду. Почему в объявлениях: добавление параметра не меняет предыдущую строку в diff.
- **Порядок членов.** Класс (логика, репозиторий, модуль): зависимости и `by lazy` → состояние (`uiState`) →
  жизненный цикл (`onCreate`, `onResume`, …) → публичные точки входа (`obtainUiEvent`, `load`) → приватные функции.
  Тип, на который ссылается класс (`sealed interface XxxNavigationEvent`, `Destination`), — после класса, в том же
  файле. Файл Ui: `@Preview` → аргументы экрана (`@Serializable`) → Contract → `XxxUi` → приватные composable блоков.
- **`when` по типам.** Если ветки — типы из `sealed`-иерархии (`class`, `data class`, `data object`), всегда пишем
  `is`: `is UiEvent.Close -> …`, `is Child.Home -> …`. Без `is` — только значения: `enum` и константы
  (`BlurLevel.Small -> …`).
- **Пустые строки.** Между членами класса и блоками логики — одна пустая строка; подряд две не оставляем. В начале и
  конце тела функции, лямбды и `when` пустых строк нет; после заголовка класса или интерфейса одна пустая строка
  допустима.
- **Импорты.** Один плоский список по алфавиту, без пустых строк между группами, без звёздочных импортов и без
  неиспользуемых.
- **Reformat Code.** Перед отдачей работы пользователь может прогнать в IDE Reformat Code с Optimize imports по
  проекту: правила форматтера лежат в `.editorconfig` (включить «Enable EditorConfig support» в настройках Code
  Style). Reformat не убирает финальный перевод строки: это правило файла (см. выше) выполняется при правке.
- Токены темы в теле composable — только через локальные алиасы в начале функции: `val appColor = AppTheme.color`,
  `val appPadding = AppTheme.padding`, `val appTypography = AppTheme.typography`, и дальше
  `style = appTypography.h5.semiBold`. Прямой `AppTheme.typography` / `AppTheme.color` допустим лишь в значениях
  параметров по умолчанию в сигнатуре и в `@Preview` (`design/foundations.md` § Правила).
- Колбэк события компонента — локальный `val obtainEvent = component::obtainUiEvent`.
- Строковые константы из одного-двух символов (`""`, `"/"`, `"_"`, `"; "`) берём из `String.Companion` в
  `core:common/.../extensions/StringExt.kt`: `String.Empty`, `String.Slash`, `String.Underscore`,
  `joinToString(String.SemicolonSpace)`. Нужной константы нет — добавляем её туда, а не пишем литерал.
- Комментарий отвечает на «почему», а не пересказывает код. KDoc — на публичных классах и функциях, у которых
  контракт неочевиден (что приходит, что возвращается, когда ошибка).
- Закомментированный код и `TODO` без причины не оставляем.

## Имена

| Что | Как |
|---|---|
| Пакеты | `org.kimoteru.wallify.core.<module>`, `org.kimoteru.wallify.feature.<name>`; без дефисов и подчёркиваний |
| Экран | `XxxComponent` (интерфейс), `XxxDefaultComponent`, `XxxLogic`, `XxxContract`, `XxxUi`, `XxxNavigationEvent` |
| Репозиторий | интерфейс `XxxRepository` в `domain`, реализация `XxxRepositoryImpl` в `data` |
| Use case | глагол + `UseCase`: `ObserveUserSettingsUseCase` |
| Koin-модуль | корневой класс `XxxModule` модуля (`@Module @Configuration @ComponentScan`) |
| Платформенные реализации | `AndroidXxx`, `JvmXxx`; файлы `Xxx.android.kt`, `Xxx.jvm.kt` |
| Boolean | `isX`, `hasX`, `canX` |

- Один файл — один основной класс; маленькие связанные типы (контракт, события) могут жить рядом.
- Состояния и события — `sealed interface` / `data class`, не набор `Boolean`-флагов.

## Корутины и потоки

- Диспетчеры — только через `AppDispatchers` из `core:common`; `Dispatchers.IO`, `Dispatchers.Main` напрямую не
  используем, чтобы код тестировался.
- `GlobalScope` нет. Scope логики экрана — `coroutineScope` базового класса; фоновая работа приложения — общий
  `CoroutineScope` из Koin.
- В `catch (e: Exception)` внутри `suspend`-кода `CancellationException` пробрасывается (`responseOf`,
  `executeRequest` уже делают это).
- Публичное состояние — `StateFlow` (читаемый), `MutableStateFlow` — скрытый. Пару «приватный `_uiState` + публичный
  `uiState = _uiState.asStateFlow()`» не пишем: используем explicit backing field через `field`:

  ```kotlin
  override val uiState: StateFlow<UiState>
      field = MutableStateFlow(UiState())
  ```

  Снаружи свойство — `StateFlow`, внутри класса оно умное приводится к `MutableStateFlow`: `uiState.update { … }`,
  `uiState.value = …`. Образец — `DecomposeLifecycle.lifecycleOwner()` в `core/presentation/.../ComponentContextExt.kt`.
  Правило относится ко всем парам «изменяемый тип скрыт, читаемый открыт», где первый — подтип второго. Если
  подтипа нет (читаемое значение строится преобразованием), оставляем пару.
- Одноразовые события (навигация, снекбар) — `StateEventFlow` и `StateEventFlowWithContent` из `core:common`, не
  `StateFlow<Boolean>`.
- Комбинация потоков в состояние — `combineState(scope, f1, f2, initialValue) { … }` из `core:common`.

## Время

- «Сейчас» — `TimeProvider` из `core:platform` или `kotlin.time.Clock`; `System.currentTimeMillis()` в
  `commonMain` нет.
- Работа с датами и часовыми поясами — отдельная библиотека (сейчас в проекте её нет): подключается по просьбе, через
  каталог версий. Локальный день без неё — смещение пояса из `TimeZoneProvider` (`core:platform`) к «сейчас» из
  `TimeProvider`; `java.util.TimeZone` только в его реализациях.

## expect / actual

- `expect` — минимальный интерфейс платформенной возможности; логика над ним остаётся в `commonMain`.
- Для сервисов предпочитаем интерфейс в `commonMain` + реализации в `androidMain`/`jvmMain`, помеченные
  `@Singleton` (`code/di.md`); `expect class` — для провайдеров путей и построителей (БД, DataStore).
- Платформенный код не тянет за собой UI: никаких `Context` и `Activity` в публичном API общего модуля.

## Прочее

- Публичный API модуля делаем минимальным: `internal` по умолчанию для деталей реализации.
- Строковые константы в `object`/`private const val`, не «магические» строки в коде.
- `@Suppress` — точечно и с причиной в комментарии.