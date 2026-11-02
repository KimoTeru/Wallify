# Ресурсы: строки, иконки, картинки

Где лежат и как получаются строки, иконки и изображения.

Related: `setup/gradle-conventions.md` § Compose Resources, `design/foundations.md`.

## Строки

1. Новые тексты интерфейса — Compose Resources: `src/commonMain/composeResources/values/strings.xml` модуля, где
   текст показывается. Доступ — `stringResource(Res.string.name)` в composable.
2. Ключ — `snake_case` по смыслу, а не по тексту: `error_network_title`. К каждой строке — английский комментарий с
   контекстом (где показывается, что вставляется в плейсхолдер).
3. В `Logic`, репозиториях и use case'ах строк нет: туда приходят данные и коды, перевод делает Ui.
4. Модуль, у которого появился первый ресурс, получает блок `compose.resources { packageOfResClass = "…" }`
   (`setup/gradle-conventions.md`); пакет — `org.kimoteru.wallify.<module>.generated.resources`.
5. Название приложения для Android — `androidApp/src/main/res/values/strings.xml` (`app_name`).
6. Литералы, уже оставшиеся в коде (тексты диалога ошибок и баннера сети, подпись сплеша), — наследие: при правке
   этих мест переносим их в ресурсы, а не добавляем новые рядом.

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