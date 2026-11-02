# Конфигурация и секреты

Где лежат адрес бэкенда, ключ доступа и как они попадают в приложение. Секреты в git не попадают никогда.

Related: `architecture/data.md` § Сеть, `setup/gradle-conventions.md`.

## Правила

1. Адрес и ключ бэкенда не пишем в код, `gradle.properties`, README и тесты. Источники — `local.properties`
   (в `.gitignore`) и переменные окружения.
2. В тестах используем вымышленный хост (`https://api.example.com/`) и фейковый ключ.
3. Значение по умолчанию для адреса — заглушка `https://example.invalid/`, чтобы проект собирался без настройки.
4. Новое секретное значение добавляется так же, как существующие: `localSecret(property, env, default)` в
   `build-logic` → `GenerateApiConfigTask` → константа в `ApiBuildConfig` → привязка в `NetworkModule`. Приложения
   значения не передают.

## Откуда берутся значения

| Что | `local.properties` | Переменная окружения |
|---|---|---|
| Адрес бэкенда | `api.baseUrl` | `API_BASE_URL` |
| Ключ доступа | `api.accessKey` | `API_ACCESS_KEY` |
| Логирование HTTP | `api.enableLogging` | `API_ENABLE_LOGGING` (по умолчанию `false`) |

- Значения читает convention-плагин `wallify.api.config` (подключён в `core:network`) при сборке и генерирует
  `ApiBuildConfig` в `core/network/build/generated/apiConfig`. Файл лежит в `build/`, в git он не попадает.
  Одинаково для Android и Desktop.
- Переменные окружения нужны при сборке, а не при запуске: для Desktop задавайте их в Run Configuration Gradle-задачи
  или в `local.properties`.
- После смены значений пересоберите проект: задача `generateApiConfig` обновит константы.
- Правило чтения: сначала `local.properties`, потом окружение, потом значение по умолчанию.

## Где живёт всё, что специфично для бэкенда

Одно место — `apiNetworkConfig` в `core/network/.../config/ApiNetworkConfig.kt`: имена и значения статических
заголовков (авторизация ключом, версия API). Остальной код от конкретного бэкенда не зависит
(`architecture/data.md` § Сеть).