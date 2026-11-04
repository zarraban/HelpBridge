# Розгортання (учасник 3) — Azure VM

1. Azure VM: Ubuntu 24.04 LTS, Standard_B2s, SSH-ключ, вхідні порти 22/80/443 (NSG),
   статична публічна IP + DNS name label (`<label>.<region>.cloudapp.azure.com`).
2. DNS: Azure автоматично створює A-запис для DNS-мітки. Перевірка: `dig +short <fqdn>`.
   (AAAA немає — IPv6 на VM не налаштовано.)
3. SSH: `ssh -i key.pem azureuser@<fqdn>`. Парольний вхід на Azure вимкнено за замовчуванням,
   root-вхід заборонений. Додати користувача до docker: `sudo usermod -aG docker azureuser` (перелогінитись).
4. `git clone https://github.com/zarraban/<repo>.git && cd <repo>/deploy && sudo bash server-setup.sh <fqdn> you@mail.com`
5. `cd .. && cp .env.example .env && nano .env` (нові паролі, JWT_SECRET, ADMIN_*,
   `CORS_ALLOWED_ORIGINS=https://<fqdn>`), `chmod 600 .env`.
6. `docker compose up -d --build && docker compose ps` (app має бути healthy).
7. З ноутбука: `bash deploy/verify-external.sh <fqdn> <IP>`.

Два шари фаєрвола: Azure NSG (зовнішній) + UFW на VM (22/80/443).
Не ставити `server.tomcat.remoteip.internal-proxies=.*`.
Економія кредиту: Stop (deallocate) VM у порталі, коли не працюєте.
Фолбек без VPS: `cloudflared tunnel --url http://localhost:8080` або `ngrok http 8080`.
