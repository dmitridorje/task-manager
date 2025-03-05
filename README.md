## HTTP Logging Starter

`HTTP Logging Starter` — Spring Boot Starter для логирования HTTP-запросов и ответов.

### Подключение

Добавьте зависимость в ваш `build.gradle`:

```gradle
dependencies {
    implementation 'ru.bakhtin.logging:http-logging-starter:0.0.1-SNAPSHOT'
}
```
Если используете `maven`:

```xml
<dependency>
    <groupId>ru.bakhtin.logging</groupId>
    <artifactId>http-logging-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Конфигурация

Настройки выполняются через `application.yml` или `application.properties`:

```yaml
http-logging:
  enabled: true
  level: INFO
  include-request-parameters: "*"
  exclude-request-parameters: QUERY_STRING
  include-response-parameters: METHOD, URI
  exclude-response-parameters:
```
```properties
http-logging.enabled=true
http-logging.level=INFO
http-logging.include-request-parameters=*
http-logging.exclude-request-parameters=QUERY_STRING
http-logging.include-response-parameters=METHOD, URI
http-logging.exclude-response-parameters=
```
### Доступные параметры

| Параметр                                | Описание                                       | Значение по умолчанию |
|-----------------------------------------|------------------------------------------------|--|
| http-logging.enabled                    | Включает/выключает логирование                 | true |
| http-logging.level                      | Уровень логирования (INFO, DEBUG, WARN, ERROR) | INFO |
| http-logging.include-request-parameters | Логируемые параметры запроса                   |[]|
| http-logging.exclude-request-parameters | Исключенные параметры запроса                  |[]|
| http-logging.include-response-parameters | Логируемые параметры ответа                    |[]|
| http-logging.exclude-response-parameters | Исключаемые параметры ответа                   |[]|

### Использование

#### Автоматическое логирование HTTP-запросов
Стартовый аспект `HttpLoggingAspect` автоматически логирует HTTP-запросы и ответы, если в конфигурации включено `http-logging.enabled=true` и над соответствующим методом контроллера проставлена аннотация `@LogHttpRequestAndResponse`.

#### Доступные параметры для логирования запроса и ответа

Для запроса: METHOD, URI, QUERY_STRING, HEADERS, CLIENT_IP, USER_AGENT

Для ответа: STATUS, HEADERS, CONTENT_TYPE, CHARACTER_ENCODING

Если для логируемых параметров поставить звездочку, то будут логироваться, все возможные параметры.
Например:
```
include-response-parameters: "*"
```