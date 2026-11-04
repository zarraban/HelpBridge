# Звіт: зовнішня доступність застосунку (учасник 3 — сервер і мережа)

## 1. Вибір архітектури
Застосунок (Spring Boot 4, Java 25) і PostgreSQL 15 запущені в ізольованому Docker-середовищі (`docker compose`).
Публічну доступність з Інтернету забезпечено **тунелем ngrok**: HTTPS-вхід завершується на стороні ngrok,
агент ngrok на хості пересилає трафік на `localhost:8080`, де слухає контейнер застосунку.

### Чому не VPS
Планувалось розгортання на Azure VM (Ubuntu 24.04) за схемою Nginx + Certbot + UFW. Від неї довелось відмовитись:
- підписка Azure for Students обмежує регіони й розміри VM (`NotAvailableForSubscription`), а `az login` блокує політика тенанта
  (`AADSTS530035`, security defaults);
- Cloudflare Quick Tunnel не підключився з мережі розробника (`api.trycloudflare.com` недосяжний);
- ngrok працює без власного домену й VPS і дає стабільний HTTPS-URL на період перевірки.

Підготовлені для VPS файли (`nginx-help-bridge.conf`, `server-setup.sh`, `verify-external.sh`, `DEPLOY.md`) залишено в репозиторії
як **готовий альтернативний сценарій**; у поточному розгортанні вони не використовуються.

## 2. Схема потоку трафіку
```
Користувач (браузер/телефон)
   │ HTTPS :443  (https://<піддомен>.ngrok-free.dev)
   ▼
ngrok edge  ── TLS termination, X-Forwarded-Proto/For
   │ зашифрований тунель (вихідне з'єднання з хоста)
   ▼
агент ngrok на хості ──► localhost:8080 ──► контейнер help_bridge_app (Spring Boot, non-root, профіль prod)
                                              │ внутрішня мережа Docker
                                              ▼
                                         postgres-db:5432  (порти на хост не публікуються)
```
Вхідні порти хоста не відкривались: тунель встановлює вихідне з'єднання, а порт застосунку прив'язано до `127.0.0.1:8080`.

## 3. Конфігурація (файли)
| Файл | Призначення |
|---|---|
| `Dockerfile` | multi-stage збірка, runtime `eclipse-temurin:25-jre`, non-root користувач |
| `docker-compose.yml` | сервіси `postgres-db` і `help-bridge-app`, `restart: unless-stopped`, `stop_grace_period: 40s`, healthcheck, порт `127.0.0.1:8080:8080`, БД без `ports` |
| `src/main/resources/application-prod.properties` | `server.forward-headers-strategy=native`, secure/httpOnly/SameSite cookie, actuator лише `health`, Swagger вимкнено |
| `.env` (не в git) | `POSTGRES_*`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `CORS_ALLOWED_ORIGINS` |

Конфіденційні параметри передаються лише змінними середовища; `.env` додано до `.gitignore`.

Запуск: `docker compose up -d --build`, тунель: `ngrok http 8080`.

## 4. Проблеми, що виникли, і рішення
- **Роль БД не існувала** (`FATAL: role "helpbridge" does not exist`): том `postgres_data` зберігав старі облікові дані;
  вирішено `docker compose down -v` і повторним запуском.
- **Пароль адміна не збігався** — сидер створює адміна лише раз і пароль не оновлює; вирішено перестворенням тому.
- **Формат логіну**: застосунок очікує `ТИП_АКАУНТА:email` (наприклад `APPLICANT:user@mail`), бо облікові записи
  зберігаються в різних таблицях.

## 5. Тестування зовнішнього доступу
Перевірки виконано через публічний URL тунелю:

| Перевірка | Результат |
|---|---|
| `GET /actuator/health` | 200, `{"status":"UP"}` |
| `GET /login` | 200 |
| `GET /requests` без входу | 302 → `https://…/login` |
| `GET /actuator/env`, `/actuator/info` | 302 → `/login` (закриті) |
| `GET /swagger-ui/index.html`, `/v3/api-docs` | 404 (вимкнено в prod) |
| `POST /api/auth/login` без тіла | 401 |
| `http://` → `https://` | 307 на `https://` |
| Cookie `JSESSIONID` | `Secure; HttpOnly; SameSite=Strict` |
| Заголовки безпеки | HSTS, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff` |
| Порти 8080, 5432, 5433 ззовні | closed/filtered |
| Вхід ролей через веб-форму (admin, representative, applicant) | редірект на `/requests` |

Редірект `…/actuator/env → https://…/login` підтверджує коректну обробку `X-Forwarded-Proto` застосунком.

## 6. Обмеження поточного розгортання
- URL безкоштовного плану ngrok діє, поки запущено агента, а хост має бути ввімкнений.
- TLS-сертифікатом керує ngrok, тож Nginx/Certbot не потрібні.
- Для постійного розгортання підготовлено сценарій на VPS (див. `DEPLOY.md`).

## 7. Скріншоти
(додати: `/login` з HTTPS, `/requests` після входу, `docker compose ps`, вікно ngrok з `Forwarding`)
