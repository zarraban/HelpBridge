#!/usr/bin/env bash
# Запускати З ІНШОЇ машини (не з VPS): bash verify-external.sh YOUR_DOMAIN VPS_IP
D="${1:?domain}"; IP="${2:?ip}"
echo "== HTTP -> HTTPS redirect (очікуємо 301)"; curl -sI "http://$D" | head -n3
echo "== HTTPS health (очікуємо 200 + UP)"; curl -s -o /dev/null -w "%{http_code}\n" "https://$D/actuator/health"; curl -s "https://$D/actuator/health"; echo
echo "== Swagger (очікуємо 401/403/404, не 200)"; curl -s -o /dev/null -w "%{http_code}\n" "https://$D/swagger-ui/index.html"
echo "== Інші actuator (очікуємо 401/403/404)"; curl -s -o /dev/null -w "%{http_code}\n" "https://$D/actuator/env"
echo "== Закриті порти 8080 і 5433 (очікуємо closed/filtered)"; nmap -Pn -p 22,80,443,5432,5433,8080 "$IP"
echo "== Сертифікат"; echo | openssl s_client -servername "$D" -connect "$D:443" 2>/dev/null | openssl x509 -noout -issuer -dates
