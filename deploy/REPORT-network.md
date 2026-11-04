# Секція звіту: сервер і мережа (учасник 3)

## 1. Обрана архітектура
Застосунок розгорнуто в Docker Compose на віртуальній машині Azure (Ubuntu 24.04 LTS, Standard_B2s).
Публічний вхід — Nginx на хості (reverse proxy з TLS), контейнер застосунку доступний лише на `127.0.0.1:8080`,
PostgreSQL не публікує портів і доступний тільки у внутрішній мережі Docker.

## 2. Схема потоку трафіку
```
Користувач ──HTTPS:443──► Azure NSG (22/80/443) ──► UFW (22/80/443) ──► Nginx (TLS termination)
                                                                           │ HTTP, X-Forwarded-*
                                                                           ▼
                                                      127.0.0.1:8080 ─► контейнер help_bridge_app (Spring Boot, non-root)
                                                                           │ мережа Docker
                                                                           ▼
                                                                  postgres-db:5432 (без публікації портів)
```
HTTP (80) перенаправляється на HTTPS (301).

## 3. Сервер і доступ
- VM Azure, автентифікація лише за SSH-ключем, парольний і root-вхід вимкнено.
- Фаєрвол у два шари: Azure NSG і UFW (default deny incoming; дозволено 22, 80, 443).
- Docker обходить UFW через iptables, тому порт застосунку опубліковано як `127.0.0.1:8080:8080`, а БД — без `ports`.

## 4. DNS
Безкоштовне DNS-ім'я Azure (`<label>.<region>.cloudapp.azure.com`) з A-записом на статичну публічну IP. AAAA відсутній (IPv6 не використовується).

## 5. Nginx
Файл: `deploy/nginx-help-bridge.conf`. Передає `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`;
HTTP/1.1 + keepalive до upstream; `client_max_body_size 10m`.
Застосунок працює з `server.forward-headers-strategy=native`, тому бачить https і реальну IP клієнта; `internal-proxies` лишено за замовчуванням (приватні діапазони, включно з Docker).

## 6. TLS (Let's Encrypt / Certbot)
`certbot --nginx` видає сертифікат (HTTP-01), додає блок 443 і редірект. Сертифікат діє 90 днів;
автооновлення — systemd-таймер certbot, перевірено `certbot renew --dry-run`.

## 7. Розгортання
`git clone` → `.env` (chmod 600) → `docker compose up -d --build`. Автозапуск: `restart: unless-stopped` + Docker daemon під systemd.
Скрипти: `deploy/server-setup.sh`, `deploy/verify-external.sh`.

## 8. Результати перевірки (заповнити після розгортання)
| Перевірка | Очікування | Результат |
|---|---|---|
| `http://<fqdn>` | 301 → https | |
| `https://<fqdn>/actuator/health` | 200, `{"status":"UP"}` | |
| Swagger UI | не 200 | |
| `nmap`: 8080, 5432/5433 | closed/filtered | |
| Сертифікат | Let's Encrypt, дійсний | |
| `certbot renew --dry-run` | успішно | |
Скріншоти: (додати)
