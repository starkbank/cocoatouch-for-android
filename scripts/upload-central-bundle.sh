#!/bin/sh
set -eu

zip=${1:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop> <version>}
name=${2:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop> <version>}
mode=${3:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop> <version>}
version=${4:?usage: upload-central-bundle.sh <zip> <deploymentName> <keep|drop> <version>}
api=https://central.sonatype.com/api/v1/publisher
attemptCap=${CENTRAL_POLL_ATTEMPTS:-90}
deadlineSeconds=${CENTRAL_POLL_DEADLINE_SECONDS:-1500}

case "$mode" in
    keep|drop) ;;
    *) echo "mode must be keep or drop"; exit 2 ;;
esac

case "$name" in
    ''|*[!A-Za-z0-9._-]*) echo "deployment name must match [A-Za-z0-9._-]"; exit 2 ;;
esac

case "$version" in
    ''|*[!A-Za-z0-9.-]*) echo "version must match [A-Za-z0-9.-]"; exit 2 ;;
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
    body=$(call -X DELETE "$api/deployment/$id") || { printf '%s\n' "$body" >&2; state="$state, drop failed"; exit 1; }
}

body=$(call "$api/published?namespace=com.starkbank&name=cocoatouch&version=$version") || { printf '%s\n' "$body" >&2; exit 1; }
published=$(printf '%s' "$body" | jq -r '.published') || { printf '%s\n' "$body" >&2; exit 1; }
[ "$published" != "true" ] || { echo "version $version is already published" >&2; exit 1; }

id=$(call -F "bundle=@$zip" "$api/upload?publishingType=USER_MANAGED&name=$name") || { printf '%s\n' "$id" >&2; id=; exit 1; }
record uploaded

attemptCount=0
pollStart=$(date +%s)
while [ "$attemptCount" -lt "$attemptCap" ] && [ $(($(date +%s) - pollStart)) -lt "$deadlineSeconds" ]; do
    attemptCount=$((attemptCount + 1))
    if ! response=$(call -X POST "$api/status?id=$id"); then
        printf '%s\n' "$response" >&2
        sleep 10
        continue
    fi
    if ! polled=$(printf '%s' "$response" | jq -r '.deploymentState'); then
        printf '%s\n' "$response" >&2
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

state="timeout ($state)"
[ "$mode" != "drop" ] || drop
exit 1
