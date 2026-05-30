# NetScanner — Network Intelligence Tool

Приложение для анализа сети на Android с поддержкой root и nmap.

## Возможности

- **Сканирование сети** — обнаружение всех устройств в /24 подсети
- **Сканер портов** — 65535 портов через nmap или встроенный сканнер
- **OS Fingerprinting** — определение операционной системы через nmap -O
- **Service Detection** — версии сервисов через nmap -sV
- **MAC + Vendor** — определение производителя по MAC-адресу
- **Оценка рисков** — цветовая маркировка опасных портов
- **Root режим** — полный функционал через su
- **Fallback режим** — базовое сканирование без root

## Сборка

### Требования
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17+
- Android SDK 34
- Gradle 8.4

### Шаги

1. Открой папку `NetScanner` в Android Studio
2. **File → Open** → выбери папку NetScanner
3. Подожди синхронизацию Gradle (~2-3 мин при первом запуске)
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK будет в `app/build/outputs/apk/debug/app-debug.apk`

### Gradle Wrapper JAR

При первом открытии Android Studio сам скачает `gradle-wrapper.jar`.
Если не скачал — открой Terminal в Android Studio и выполни:
```
gradle wrapper --gradle-version 8.4
```

## Установка nmap на устройство

Для полного функционала установи nmap через Termux:

```bash
pkg install nmap
```

Или скопируй бинарник nmap для ARM64 в `/data/local/tmp/nmap` и дай права:
```bash
chmod +x /data/local/tmp/nmap
```

Приложение автоматически найдёт nmap через `which nmap`.

## Структура проекта

```
app/src/main/
├── java/com/netscanner/app/
│   ├── activities/       — экраны приложения
│   ├── adapters/         — RecyclerView адаптеры
│   ├── models/           — data классы
│   └── utils/            — RootUtils, NmapRunner, NetworkUtils
└── res/
    ├── layout/           — XML макеты
    ├── drawable/         — иконки и фоны
    ├── values/           — цвета, темы, строки
    └── font/             — шрифт Inter
```
