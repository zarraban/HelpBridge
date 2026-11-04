#!/usr/bin/env bash
# Одноразове налаштування чистого Ubuntu 22.04/24.04 VPS. Запускати від root: bash server-setup.sh YOUR_DOMAIN you@mail.com
set -euo pipefail
DOMAIN="${1:?domain}"; EMAIL="${2:?email}"

apt-get update && apt-get -y upgrade
apt-get install -y ca-certificates curl git ufw nginx certbot python3-certbot-nginx

# Docker (офіційний скрипт)
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker

# Firewall: тільки 22, 80, 443
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# Nginx
install -m 644 "$(dirname "$0")/nginx-help-bridge.conf" /etc/nginx/sites-available/help-bridge
sed -i "s/YOUR_DOMAIN/${DOMAIN}/g" /etc/nginx/sites-available/help-bridge
ln -sf /etc/nginx/sites-available/help-bridge /etc/nginx/sites-enabled/help-bridge
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

# TLS (потребує, щоб DNS A-запис уже вказував на цей сервер)
certbot --nginx -d "$DOMAIN" -m "$EMAIL" --agree-tos --no-eff-email --redirect -n
certbot renew --dry-run
systemctl list-timers | grep -i certbot || true
