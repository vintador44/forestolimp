# Forest Navigation - Kotlin Android App

Мобильное приложение для навигации, просмотра и оценки лесных маршрутов.

## Архитектура

### Project Structure
```
composeApp/src/androidMain/kotlin/org/example/project/
├── data/
│   ├── api/
│   │   ├── ForestNavigationApi.kt       # Retrofit API интерфейс
│   │   └── RetrofitClient.kt            # Retrofit конфигурация
│   ├── models/
│   │   ├── RouteModels.kt              # Модели маршрутов
│   │   └── UserModels.kt               # Модели пользователя и локаций
│   └── repository/
│       └── Repositories.kt              # Слой репозитория
├── ui/
│   ├── screens/
│   │   ├── RoadsListScreen.kt          # Список маршрутов
│   │   ├── RoadDetailScreen.kt         # Детали маршрута
│   │   ├── LocationsScreen.kt          # Список локаций
│   │   └── MenuScreen.kt               # Главное меню
│   ├── viewmodel/
│   │   ├── RoadsViewModel.kt           # ViewModel маршрутов
│   │   └── LocationsViewModel.kt       # ViewModel локаций
│   └── theme/
│       └── Theme.kt                     # Material 3 тема
├── utils/
│   └── Utils.kt                         # Утилиты форматирования
├── App.kt                               # Главная Composable функция
├── MainActivity.kt                      # Entry point
└── ...
```

## Основные функции

### Просмотр маршрутов
- Список всех маршрутов с основной информацией
- Детальная страница маршрута с:
  - Статистикой (дистанция, подъём, спуск)
  - Профилем высот
  - Информацией об авторе
  - Системой оценивания маршрута

### Локации
- Просмотр интересных мест
- Фильтрация по категориям
- Отображение координат и описания

### API Integration
- Retrofit для HTTP запросов
- Kotlinx Serialization для JSON парсинга
- Rate limiting поддержка
- Обработка ошибок

## Технологические стек

- **Kotlin** - язык программирования
- **Jetpack Compose** - UI фреймворк
- **Retrofit 2** - HTTP клиент
- **Kotlinx Serialization** - JSON парсинг
- **Navigation Compose** - навигация между экранами
- **Lifecycle/ViewModel** - управление состоянием
- **Material 3** - дизайн система

## Конфигурация сервера

В `RetrofitClient.kt` установлена базовая URL:
- **Эмулятор**: `http://10.0.2.2:5000`
- **Реальное устройство**: `http://192.168.x.x:5000`

Измените адрес в зависимости от вашей конфигурации.

## Запуск проекта

1. Убедитесь, что Node.js сервер запущен:
   ```bash
   docker-compose up
   ```

2. Запустите приложение в Android Studio или используя Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

## API Endpoints

### Маршруты
- `GET /api/roads` - Все маршруты
- `GET /api/roads/{id}` - Маршрут по ID
- `GET /api/roads/user/{userId}` - Маршруты пользователя
- `GET /api/route/elevations` - Профиль высот маршрута

### Локации
- `GET /api/locations` - Все локации
- `GET /api/locations/{id}` - Локация по ID

### Категории
- `GET /api/categories` - Все категории

### Погода
- `GET /api/weather` - Текущая погода
- `GET /api/weather/forecast/range` - Прогноз погоды

## Примечания

- Приложение использует Material Design 3
- Все сетевые запросы асинхронные с Coroutines
- ViewModel автоматически сохраняет состояние при ротации
- Поддержка русского языка

## История версий

### v1.0.0
- Базовая структура приложения
- Просмотр маршрутов и локаций
- Деталь маршрута с статистикой
