## HTTP Logging Starter

`HTTP Logging Starter` — Spring Boot Starter для логирования методов контроллера.

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
```
```properties
http-logging.enabled=true
http-logging.level=INFO
```
### Доступные параметры

| Параметр                                | Описание                                       | Значение по умолчанию |
|-----------------------------------------|------------------------------------------------|--|
| http-logging.enabled                    | Включает/выключает логирование                 | true |
| http-logging.level                      | Уровень логирования (INFO, DEBUG, WARN, ERROR) | INFO |

### Использование

#### Автоматическое логирование методов контроллера
Стартовый аспект `HttpLoggingAspect` автоматически логирует методы контроллера, если в конфигурации включено `http-logging.enabled=true` и над соответствующим методом контроллера проставлена аннотация `@LogControllerMethodCall`.