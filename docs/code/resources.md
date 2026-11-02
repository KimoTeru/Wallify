# Ресурсы: строки, иконки, картинки

Где лежат и как получаются строки, иконки и изображения.

Related: `setup/gradle-conventions.md` § Compose Resources, `design/foundations.md`.

## Строки

1. Все ресурсы проекта — строки, шрифты, картинки — лежат в одном модуле `core:resources`,
   `src/commonMain/composeResources/`. В других модулях папки `composeResources` нет.
2. Все строки — один файл на язык: `values/strings.xml` (английский, язык по умолчанию). Внутри он разбит
   комментариями-разделами по областям (`Common`, `Errors`, `Legal`, `Permissions`); новая область — новый
   раздел в конце, а не новый файл. Так другой язык — это ровно один файл с тем же набором ключей.
3. Ключ — `snake_case` с префиксом области, по смыслу, а не по тексту: `error_dialog_title`, `permission_allow_access`.
   Ключи общие на весь проект, повторять их нельзя. К каждой строке — английский комментарий с контекстом (где
   показывается, что вставляется в плейсхолдер).
4. Доступ — публичный `Res` из `org.kimoteru.wallify.core.resources.generated.resources`:
   `stringResource(Res.string.error_dialog_title)`. Модуль, которому нужны ресурсы, зависит от `core:resources`
   (фичи получают его от `wallify.feature`).
5. В `Logic`, репозиториях и use case'ах строк нет: туда приходят данные и коды, перевод делает Ui.
6. Строка с подстановкой — `%1$s`, `%2$s` в XML и `stringResource(Res.string.name, arg1, arg2)`; склеивать текст из
   частей в коде нельзя (порядок слов зависит от языка).
7. Название приложения для Android (манифест, лаунчер) — `androidApp/src/main/res/values/strings.xml` (`app_name`);
   в экранах используется `Res.string.app_name`.
8. Литералов вида `Text("…")`, `title = "…"` в composable нет. Единственное исключение — технические подписи
   (`label` анимаций, имена иконок).
9. Новый язык: папка `values-<код>/` рядом с `values/` (`values-ru/strings.xml`, `values-de/strings.xml`) с тем же
   набором ключей, в том же порядке и с теми же разделами; комментарии-контексты переносим как есть. Ключа нет в
   переводе — используется английский. Добавили строку в `values/strings.xml` — добавьте её во все языки.
   Региональные варианты — `values-pt-rBR/`. Язык выбирает система (`systemLanguageTag()`, раздел «Язык» ниже).

## Иконки

- Простые иконки — `ImageVector`, нарисованный в коде (`AppIcons` в `core:design-system`, `ui/icon`): путь
  `moveTo`/`lineTo`/`close` в viewport 24×24, заливка чёрная — цвет задаёт `tint`. Новая иконка — новый `val` в
  `AppIcons` + функция `IconXxx(modifier, tint)` рядом.
- Путь иконки берём из Material Symbols (24 px, Filled); сложные многоцветные векторы — XML в `composeResources/drawable`.
- Иконка, на которую нигде нет ссылки, не лежит в проекте.

## Изображения

- Картинки по сети — Coil (`AsyncImage`); локальные растровые — `composeResources/drawable`, `painterResource`.
- Размер и форма — токены `AppTheme.size` / `AppTheme.corner`, не числа в вёрстке.

## Язык

Язык интерфейса и заголовок `Accept-Language` берутся из `UserSettings.languageTag` или системного языка
(`systemLanguageTag()` в `core:platform`); отдельной подсистемы смены локали в проекте нет.