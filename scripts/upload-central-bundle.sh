#!/bin/sh
set -eu

zip=${1:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
name=${2:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
mode=${3:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
api=https://central.sonatype.com/api/v1/publisher

case "$mode" in
    keep|drop) ;;
    *) echo "mode must be keep or drop"; exit 2 ;;
esac

token=$(printf '%s:%s' "$CENTRAL_USERNAME" "$CENTRAL_PASSWORD" | base64 | tr -d '\n')
echo "::add-mask::$token"

record() {
    [ -z "${GITHUB_STEP_SUMMARY:-}" ] || echo "deployment $id $1" >> "$GITHUB_STEP_SUMMARY"
}

drop() {
    curl --silent --show-error --fail-with-body -X DELETE -H "Authorization: Bearer $token" "$api/deployment/$id" > /dev/null
}

id=$(curl --silent --show-error --fail-with-body \
    -H "Authorization: Bearer $token" \
    -F "bundle=@$zip" \
    "$api/upload?name=$name&publishingType=USER_MANAGED")

attempt=0
while [ "$attempt" -lt 90 ]; do
    attempt=$((attempt + 1))
    response=$(curl --silent --show-error --fail-with-body -X POST -H "Authorization: Bearer $token" "$api/status?id=$id")
    state=$(printf '%s' "$response" | jq -r '.deploymentState')
    if [ "$state" = "VALIDATED" ]; then
        echo "deployment $id VALIDATED"
        record VALIDATED
        [ "$mode" = "drop" ] || exit 0
        drop
        echo "deployment $id dropped"
        record dropped
        exit 0
    fi
    if [ "$state" = "FAILED" ]; then
        printf '%s' "$response" | jq '.errors'
        record FAILED
        drop
        exit 1
    fi
    sleep 10
done

echo "deployment $id still $state after 90 attempts"
record "timeout ($state)"
exit 1
