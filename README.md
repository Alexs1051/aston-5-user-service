# 7. Паттерны микросервисной архитектуры

---

### Цель работы
Добавить к существующей системе паттерны: gateway api, service discovery, circuit breaker, external configuration - реализации данных паттернов можно найти в модулях spring cloud.

### Список сервисов:
- https://github.com/Alexs1051/aston-5-user-service
- https://github.com/Alexs1051/aston-5-notification-service
- https://github.com/Alexs1051/aston-api-gateway
- https://github.com/Alexs1051/aston-circuit-breaker (добавлено)
- https://github.com/Alexs1051/aston-config-server
- https://github.com/Alexs1051/aston-eureka-server

### Результат
Таймкоды:
- 00:03-00:06 = config-server
- 00:10-00:15 = circuit-breaker
- 00:18-00:22 = api-gateway
- 00:23-00:49 = circuit-breaker (демонстрация работы: запрос => api_gateway => circuit-breaker => сервис)

https://github.com/user-attachments/assets/e9425377-d70b-4630-a0f9-3bcd6a661507

#### Тестовое покрытие
<img width="1759" height="316" alt="Screenshot_7" src="https://github.com/user-attachments/assets/0b1e1e71-b845-4c3c-a126-33fa80b52658" />
<img width="1760" height="315" alt="Screenshot_6" src="https://github.com/user-attachments/assets/b14749ef-9044-4670-9326-137e744e9560" />
