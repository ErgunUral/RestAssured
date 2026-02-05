#!/bin/bash

# Configuration
PROJECT_ID="4" # From your logs: project=4
GITLAB_URL="https://gitlab-qa.paytr.com"
BRANCH_NAME="test-results-feb-5-2026"
SCHEDULE_DESC="2 Saatte Bir Otomatik Test (Automated)"
CRON_PATTERN="0 */2 * * *" # Every 2 hours

# Check for API Token
if [ -z "$GITLAB_API_TOKEN" ]; then
    echo "❌ HATA: GITLAB_API_TOKEN çevre değişkeni bulunamadı."
    echo "Lütfen token'ı tanımlayıp tekrar çalıştırın:"
    echo "export GITLAB_API_TOKEN='sizin-kişisel-erişim-tokenınız'"
    exit 1
fi

echo "🚀 GitLab Schedule Oluşturuluyor..."
echo "-----------------------------------"
echo "Proje ID: $PROJECT_ID"
echo "Branch: $BRANCH_NAME"
echo "Zamanlama: $CRON_PATTERN (Her 2 saatte bir)"

RESPONSE=$(curl --request POST --header "PRIVATE-TOKEN: $GITLAB_API_TOKEN" \
     --header "Content-Type: application/json" \
     --data "{
        \"description\": \"$SCHEDULE_DESC\",
        \"ref\": \"$BRANCH_NAME\",
        \"cron\": \"$CRON_PATTERN\",
        \"cron_timezone\": \"Europe/Istanbul\",
        \"active\": true
    }" \
    "$GITLAB_URL/api/v4/projects/$PROJECT_ID/pipeline_schedules")

if [[ $RESPONSE == *"id"* ]]; then
    echo "✅ BAŞARILI: Zamanlama oluşturuldu!"
    echo "Detaylar: $RESPONSE"
else
    echo "❌ HATA: Zamanlama oluşturulamadı."
    echo "Sunucu Yanıtı: $RESPONSE"
fi
