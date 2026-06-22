# Travel Companion

Android-приложение-спутник путешественника: помогает спланировать поездку и держать в одном месте погоду, бюджет и местные события — без необходимости открывать десяток разных сайтов.

## Функционал

- **Аккаунт** — регистрация и вход по email/паролю (пароль хранится хэшированным, SHA-256)
- **Поездки** — список поездок, создание с автодополнением города (Nominatim/OpenStreetMap), удаление с подтверждением
- **Погода** — текущая температура и описание погоды для города поездки (OpenWeatherMap)
- **Расходы** — учёт трат по категориям и валютам (CNY/USD/EUR/RUB) с автоматическим пересчётом в рубли по текущему курсу
- **События** — поиск реальных мероприятий в городе поездки (Ticketmaster), детальная карточка события, добавление в закладки поездки, шаринг и переход на страницу регистрации

## Стек

- **Kotlin** + **Jetpack Compose** (Material 3)
- Архитектура **MVVM**: Screen → ViewModel (`StateFlow`) → Repository → DAO/Retrofit
- **Navigation Compose** — единый граф навигации
- **Room** — локальное хранилище (`users`, `trips`, `expenses`, `saved_events`)
- **Retrofit + Gson** — интеграция с внешними API
- **Coroutines** — асинхронность
- **Coil** — загрузка изображений
- Сборка: **Gradle (Kotlin DSL)** + **KSP** для Room compiler

### Внешние API

| Сервис | Назначение | Нужен ключ |
|---|---|---|
| [OpenWeatherMap](https://openweathermap.org/api) | погода | да |
| [Ticketmaster Discovery](https://developer.ticketmaster.com/) | события в городе | да |
| [exchangerate-api.com](https://www.exchangerate-api.com/) | курсы валют (база RUB) | нет |
| [Nominatim (OpenStreetMap)](https://nominatim.org/) | автодополнение городов | нет |

## Запуск проекта

### Требования
- Android Studio (последняя стабильная версия)
- JDK 17+
- minSdk 26 / targetSdk 36

### Настройка API-ключей

1. Скопируйте `local.properties.example` в `local.properties`
2. Укажите путь к Android SDK (`sdk.dir`)
3. Получите и впишите ключи:
   - `WEATHER_API_KEY` — на [openweathermap.org](https://openweathermap.org/api)
   - `TICKETMASTER_API_KEY` — на [developer.ticketmaster.com](https://developer.ticketmaster.com/)

`local.properties` не попадает в git — ключи остаются только у вас локально.

### Сборка и запуск

```bash
./gradlew assembleDebug
```

Либо откройте проект в Android Studio и нажмите **Run ▶** на эмуляторе/устройстве (API 26+).

## Структура проекта

```
data/            — Entity, DAO, Repository (по доменам: events/, exchange/, geo/, weather/)
ui/auth/         — экраны входа и регистрации
ui/trip/         — список поездок, создание (с автодополнением города), детали поездки
ui/expense/      — расходы поездки с мультивалютной конвертацией
ui/events/       — список событий и детальный экран события
ui/common/       — общие компоненты (загрузка, пустые состояния)
ui/theme/        — тема Material 3
ui/nav/          — граф навигации (Navigation Compose)
```
