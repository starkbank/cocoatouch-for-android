#!/bin/sh
set -eu

zip=${1:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
name=${2:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
mode=${3:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop>}
api=https://central.sonatype.com/api/v1/publisher
attemptCap=${CENTRAL_POLL_ATTEMPTS:-90}

case "$mode" in
    keep|drop) ;;
    *) echo "mode must be keep or drop"; exit 2 ;;
esac

token=$(printf '%s:%s' "$CENTRAL_USERNAME" "$CENTRAL_PASSWORD" | base64 | tr -d '\n')
echo "::add-mask::$token"

id=
state=uploaded
recorded=

record() {
    recorded=$1
    echo "deployment $id $1"
    [ -z "${GITHUB_STEP_SUMMARY:-}" ] || echo "deployment $id $1" >> "$GITHUB_STEP_SUMMARY"
}

# The maintainer publishes exactly the id in the summary, so every non-zero exit must leave one behind
finish() {
    code=$?
    [ "$code" -ne 0 ] || return 0
    [ -n "$id" ] || return 0
    [ "$state" != "$recorded" ] || return 0
    record "$state"
}
trap finish EXIT

call() {
    printf 'Authorization: Bearer %s' "$token" | curl --silent --show-error --fail-with-body --connect-timeout 10 --max-time 120 -H @- "$@"
}

drop() {
    body=$(call -X DELETE "$api/deployment/$id") || { printf '%s\n' "$body" >&2; exit 1; }
}

id=$(call -F "bundle=@$zip" "$api/upload?name=$name&publishingType=USER_MANAGED") || { printf '%s\n' "$id" >&2; id=; exit 1; }
record uploaded

attempt=0
while [ "$attempt" -lt "$attemptCap" ]; do
    attempt=$((attempt + 1))
    if ! response=$(call -X POST "$api/status?id=$id"); then
        printf '%s\n' "$response" >&2
        sleep 10
        continue
    fi
    if ! polled=$(printf '%s' "$response" | jq -r '.deploymentState'); then
        sleep 10
        continue
    fi
    state=$polled
    if [ "$state" = "VALIDATED" ]; then
        record VALIDATED
        [ "$mode" = "drop" ] || exit 0
        drop
        record dropped
        exit 0
    fi
    if [ "$state" = "FAILED" ]; then
        printf '%s' "$response" | jq '.errors'
        drop
        exit 1
    fi
    sleep 10
done

state=timeout
[ "$mode" != "drop" ] || drop
exit 1
