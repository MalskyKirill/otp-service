# Проект по курсу Специализированные инструменты разработки на языке Java (OTP Service) НИЯУ МИФИ

Учебный проект для генерации, доставки и проверки одноразовых OTP-кодов для подтверждения пользовательских операций.

## Описание проекта

Сервис позволяет регистрировать пользователей, выполнять аутентификацию по JWT, управлять конфигурацией OTP-кодов через API администратора, отправлять коды по нескольким каналам и валидировать их.

## Возможности
- регистрация пользователей с ролями ADMIN и USER
- запрет на создание второго администратора
- логин с выдачей JWT-токена
- админское API для управления конфигурацией OTP
- получение списка всех пользователей, кроме администраторов
- удаление пользователей вместе с привязанными OTP-кодами
- генерация OTP-кодов для пользовательских операций
- валидация OTP-кодов
- поддержка статусов OTP: ACTIVE, EXPIRED, USED
- доставка OTP по каналам:
    - FILE
    - EMAIL
    - SMS
    - TELEGRAM
- автоматическое истечение просроченных OTP-кодов по расписанию
- логирование основных действий и ошибок
- автоматическая загрузка схемы БД при старте приложения

## Технологии
- Java 17
- Maven
- PostgreSQL 17
- JDBC
- com.sun.net.httpserver.HttpServer
- Jackson
- BCrypt
- JWT (jjwt)
- SLF4J + Logback
- Angus Mail
- jSMPP
- Java HttpClient

## Архитектура
Приложение разделено на три основных слоя:

- handler — обработка HTTP-запросов
- service — бизнес-логика
- dao — работа с БД через JDBC

Дополнительно используются:

- security — JWT, аутентификация, авторизация
- notification — отправка OTP по разным каналам
- scheduler — фоновое истечение просроченных кодов
- util — JSON, генерация OTP и прочие утилиты
- config — конфигурация приложения и инициализация схемы

## Структура проекта
```text
src/main/java/ru/mephi/malskiy
├── OtpApp.java
├── config
├── dao
├── dto
├── exeption
├── handler
├── model
├── notification
├── scheduler
├── security
├── service
└── util

src/main/resources
├── application.properties
├── schema.sql
├── email.properties
├── sms.properties
└── telegram.properties
```

## Роли пользователей
- ADMIN — может управлять конфигурацией OTP и пользователями
- USER — может генерировать и проверять OTP-коды

## Статусы OTP-кодов
- ACTIVE — код активен и может быть использован
- EXPIRED — срок действия кода истёк
- USED — код успешно прошёл валидацию и больше не может использоваться

## Модель данных
### Таблица users
- id
- login
- password_hash
- role
- created_at

### Таблица otp_config
- id
- code_length
- lifetime_seconds

### Таблица otp_codes
- id
- user_id
- operation_id
- code
- status
- created_at
- expires_at
- used_at

## API
### Health
`GET /health`

Проверка, что сервис запущен.

Пример ответа:

```json
{
  "status": "OK"
}
```

### Аутентификация
`POST /auth/register`

Регистрация нового пользователя.

Пример запроса:

```json
{
  "login": "user1",
  "password": "1234",
  "role": "USER"
}
```

Пример ответа:

```json
{
  "message": "User registered successfully"
}
```

`POST /auth/login`

Логин пользователя и получение JWT.

Пример запроса:

```json
{
  "login": "user1",
  "password": "1234"
}
```

Пример ответа:

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer",
  "expiresInMinutes": 60
}
```

### Админское API
Все запросы требуют заголовок:

`Authorization: Bearer <JWT>`

`GET /admin/otp-config`

Получить текущую конфигурацию OTP.

`PUT /admin/otp-config`

Обновить длину и время жизни OTP.

Пример запроса:

```json
{
  "codeLength": 6,
  "lifetimeSeconds": 300
}
```

`GET /admin/users`

Получить список всех пользователей, кроме администраторов.

`DELETE /admin/users/{id}`

Удалить обычного пользователя и его OTP-коды.

### Пользовательское API
Все запросы требуют заголовок:

`Authorization: Bearer <JWT>`

`POST /user/otp`

Сгенерировать OTP-код для операции и отправить по выбранному каналу.

Пример запроса:

```json
{
  "operationId": "payment-123",
  "channel": "FILE",
  "destination": "otp_codes.txt"
}
```

Пример ответа:

```json
{
  "message": "OTP code generated successfully",
  "operationId": "payment-123",
  "channel": "FILE",
  "expiresAt": "2026-05-04T12:34:56"
}
```

`POST /user/otp/validate`

Проверить OTP-код.

Пример запроса:

```json
{
  "operationId": "payment-123",
  "code": "123456"
}
```

Пример успешного ответа:

```json
{
  "valid": true,
  "status": "USED",
  "message": "OTP validated successfully"
}
```

Пример ответа при неверном коде:

```json
{
  "valid": false,
  "status": "ACTIVE",
  "message": "Invalid OTP code"
}
```

Пример ответа при просроченном коде:

```json
{
  "valid": false,
  "status": "EXPIRED",
  "message": "OTP code expired"
}
```

## Поддерживаемые каналы доставки
### FILE
Код записывается в файл `otp_codes.txt` в корне проекта.

### EMAIL
Код отправляется по SMTP через почтовый сервис.

### SMS
Код отправляется через SMPP-эмулятор.

### TELEGRAM
Код отправляется через Telegram Bot API.

## Конфигурация
### application.properties
```properties
db.url=jdbc:postgresql://localhost:5432/otp_service
db.username=postgres
db.password=postgres

server.port=8080

jwt.secret=change-this-secret-key-change-this-secret-key
jwt.expiration.minutes=60

otp.default.length=6
otp.default.lifetime.seconds=300
```

### email.properties
Пример для Mail.ru:

```properties
email.username=your_box@mail.ru
email.password=YOUR_APP_PASSWORD
email.from=your_box@mail.ru

mail.smtp.host=smtp.mail.ru
mail.smtp.port=465
mail.smtp.auth=true
mail.smtp.ssl.enable=true
```

### sms.properties
```properties
smpp.host=localhost
smpp.port=2775
smpp.system_id=smppclient1
smpp.password=password
smpp.system_type=OTP
smpp.source_addr=OTPService
```

### telegram.properties
```properties
telegram.bot.token=YOUR_BOT_TOKEN
telegram.chat.id=YOUR_CHAT_ID
telegram.api.url=https://api.telegram.org/bot%s/sendMessage
```

## Безопасность 
Файлы с реальными данными отсутствуют в репозитории.

## Подготовка базы данных
Приложение автоматически загружает `schema.sql` при каждом старте.

Это означает, что:
- таблицы создаются автоматически, если их нет
- стартовая конфигурация OTP добавляется автоматически
- вручную запускать SQL-скрипт не требуется

Нужно заранее создать саму базу данных в PostgreSQL.

Пример:

```bash
createdb otp_service
```

## Запуск проекта
1. Клонировать репозиторий
```bash
git clone <repo_url>
cd <project_folder>
```

2. Создать PostgreSQL базу
```bash
createdb otp_service
```

3. Заполнить конфиги
- src/main/resources/application.properties (редактировать по желанию)
- src/main/resources/email.properties (обязательно)
- src/main/resources/sms.properties (обязательно)
- src/main/resources/telegram.properties (обязательно)

4. Собрать проект
```bash
mvn clean package
```

5. Запустить приложение
```bash
mvn exec:java -Dexec.mainClass="ru.mephi.malskiy.OtpApp"
```

или из IDE через `OtpApp`.

## Примеры curl
### Проверка сервиса
```bash
curl http://localhost:8080/health
```

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "login": "user1",
    "password": "1234",
    "role": "USER"
  }'
```

### Логин
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "user1",
    "password": "1234"
  }'
```

### Создание OTP в файл
```bash
curl -X POST http://localhost:8080/user/otp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "operationId": "payment-123",
    "channel": "FILE",
    "destination": "otp_codes.txt"
  }'
```

### Создание OTP по email
```bash
curl -X POST http://localhost:8080/user/otp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "operationId": "payment-email-1",
    "channel": "EMAIL",
    "destination": "target@example.com"
  }'
```

### Создание OTP по SMS
```bash
curl -X POST http://localhost:8080/user/otp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "operationId": "payment-sms-1",
    "channel": "SMS",
    "destination": "79001234567"
  }'
```

### Создание OTP в Telegram
```bash
curl -X POST http://localhost:8080/user/otp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "operationId": "payment-telegram-1",
    "channel": "TELEGRAM",
    "destination": "Kirill"
  }'
```

### Валидация OTP
```bash
curl -X POST http://localhost:8080/user/otp/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "operationId": "payment-123",
    "code": "123456"
  }'
```

## Планировщик истечения OTP
В приложении реализован фоновый планировщик, который через заданный интервал помечает все просроченные активные OTP-коды как `EXPIRED`.

## Логирование
Логируются:
- запуск приложения
- ошибки подключения
- регистрация и логин
- создание OTP
- отправка уведомлений
- истечение просроченных кодов
- ошибки при работе с БД и внешними сервисами
