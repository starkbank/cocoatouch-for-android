#!/bin/sh
set -eu

here=$(cd "$(dirname "$0")" && pwd)
script="$here/upload-central-bundle.sh"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir "$work/bin"
touch "$work/bundle.zip"

cat > "$work/bin/curl" <<'STUB'
#!/bin/sh
input=$(cat)
printf '%s\n' "$input" >> "$STUB_DIR/stdins"
printf '%s\n' "$*" >> "$STUB_DIR/argvs"
method=GET
previous=
for arg in "$@"; do
    [ "$previous" != "-X" ] || method=$arg
    [ "$arg" != "-F" ] || method=POST
    previous=$arg
    url=$arg
done
echo "$method $url" >> "$STUB_DIR/calls"
case "$url" in
    */published*)
        echo "{\"published\":$(cat "$STUB_DIR/published")}"
        ;;
    */upload*)
        if [ "$(cat "$STUB_DIR/upload")" = "401" ]; then
            echo '{"error":"unauthorized"}'
            exit 22
        fi
        if [ "$(cat "$STUB_DIR/upload")" = "html" ]; then
            echo '<html>maintenance</html>'
            exit 0
        fi
        echo 28570f16-da32-4c14-bd2e-c1acc0782365
        ;;
    */status*)
        count=$(cat "$STUB_DIR/pollCount" 2>/dev/null || echo 0)
        count=$((count + 1))
        echo "$count" > "$STUB_DIR/pollCount"
        answer=$(sed -n "${count}p" "$STUB_DIR/polls")
        [ -n "$answer" ] || answer=$(tail -n 1 "$STUB_DIR/polls")
        if [ "$answer" = "notjson" ]; then
            echo '<html>oops</html>'
            exit 0
        fi
        if [ "$answer" = "503" ]; then
            echo '{"error":"unavailable"}'
            exit 22
        fi
        echo "{\"deploymentState\":\"$answer\",\"errors\":{\"pom\":[\"bad pom\"]}}"
        ;;
    */deployment/*)
        if [ "$(cat "$STUB_DIR/delete")" = "500" ]; then
            echo '{"error":"delete refused"}'
            exit 22
        fi
        ;;
esac
STUB
printf '#!/bin/sh\n' > "$work/bin/sleep"
chmod +x "$work/bin/curl" "$work/bin/sleep"

export STUB_DIR="$work"
export CENTRAL_USERNAME=fakeUser
export CENTRAL_PASSWORD=fakePassword
export GITHUB_STEP_SUMMARY="$work/summary"

fail() {
    echo "FAIL $scenario: $1" >&2
    exit 1
}

run() {
    scenario=$1
    mode=$2
    expectedCode=$3
    attemptCount=$4
    rm -f "$work/calls" "$work/pollCount" "$work/summary" "$work/stdins" "$work/argvs"
    : > "$work/summary"
    version=${deploymentVersion:-1.2.3}
    name=${deploymentName:-cocoatouch-$version}
    [ -n "${deploymentName:-}" ] || [ "$mode" != "drop" ] || name=cocoatouch-$version-dryrun-7
    code=0
    PATH="$work/bin:$PATH" CENTRAL_POLL_ATTEMPTS=$attemptCount sh "$script" "$work/bundle.zip" "$name" "$mode" "$version" > "$work/out" 2> "$work/err" || code=$?
    [ "$code" -eq "$expectedCode" ] || fail "exit $code, expected $expectedCode"
}

expectSummary() {
    [ "$(cat "$work/summary")" = "$1" ] || fail "summary was: $(cat "$work/summary")"
}

expectDeleteCount() {
    count=$(grep -c '^DELETE ' "$work/calls" || true)
    [ "$count" = "$1" ] || fail "DELETE count $count, expected $1"
}

echo 200 > "$work/upload"
echo 200 > "$work/delete"
echo false > "$work/published"

printf 'VALIDATED\n' > "$work/polls"
run validatedKeep keep 0 3
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED"
expectDeleteCount 0
grep -Fq 'publishingType=USER_MANAGED' "$work/argvs" || fail "publishingType missing"
! grep -q '^POST .*/deployment/' "$work/calls" || fail "unexpected POST to deployment"
echo "ok: validatedKeep"

run validatedDrop drop 0 3
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 dropped"
expectDeleteCount 1
grep -Fq 'publishingType=USER_MANAGED' "$work/argvs" || fail "publishingType missing"
! grep -q '^POST .*/deployment/' "$work/calls" || fail "unexpected POST to deployment"
grep -Fq 'deployment 28570f16-da32-4c14-bd2e-c1acc0782365 dropped' "$work/out" || fail "dropped not printed"
echo "ok: validatedDrop"

printf 'FAILED\n' > "$work/polls"
run failed keep 1 3
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 FAILED"
expectDeleteCount 1
grep -Fq 'bad pom' "$work/out" || fail "errors not printed"
echo "ok: failed"

echo 401 > "$work/upload"
run uploadRejected keep 1 3
expectSummary ""
expectDeleteCount 0
grep -Fq 'unauthorized' "$work/err" || fail "body not on stderr"
echo "ok: uploadRejected"

echo 200 > "$work/upload"
printf 'PENDING\n503\nVALIDATED\n' > "$work/polls"
run pollRecovers keep 0 5
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED"
grep -Fq 'unavailable' "$work/err" || fail "poll body not on stderr"
echo "ok: pollRecovers"

printf 'PENDING\n' > "$work/polls"
run timeoutDrop drop 1 3
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 timeout (PENDING)"
expectDeleteCount 1
echo "ok: timeoutDrop"

printf 'PENDING\n503\n' > "$work/polls"
run pollAttemptCounts keep 1 2
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 timeout (PENDING)"
[ "$(grep -c '^POST .*status' "$work/calls")" = "2" ] || fail "expected exactly 2 polls"
echo "ok: pollAttemptCounts"

printf 'VALIDATED\n' > "$work/polls"
run tokenOnStdinOnly drop 0 3
! grep -q 'Bearer' "$work/argvs" || fail "token in argv"
expectedHeader="Authorization: Bearer $(printf 'fakeUser:fakePassword' | base64 | tr -d '\n')"
! grep -vFx "$expectedHeader" "$work/stdins" || fail "stdin header is not the expected token"
[ "$(wc -l < "$work/stdins" | tr -d ' ')" = "$(wc -l < "$work/calls" | tr -d ' ')" ] || fail "a call without stdin header"
echo "ok: tokenOnStdinOnly"

echo 500 > "$work/delete"
run deleteFailsAfterValidatedDrop drop 1 3
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED, drop failed"
grep -Fq 'delete refused' "$work/err" || fail "delete body not on stderr"
echo 200 > "$work/delete"
echo "ok: deleteFailsAfterValidatedDrop"

printf 'notjson\nVALIDATED\n' > "$work/polls"
run pollBodyNotJson keep 0 3
grep -Fq '<html>oops' "$work/err" || fail "non-json body not on stderr"
echo "ok: pollBodyNotJson"

echo true > "$work/published"
run alreadyPublished keep 1 3
expectSummary ""
! grep -q '/upload' "$work/calls" || fail "uploaded an already published version"
grep -Fq 'version 1.2.3 is already published' "$work/err" || fail "message not on stderr"
echo false > "$work/published"
echo "ok: alreadyPublished"

deploymentName='x&publishingType=AUTOMATIC#'
run badName keep 2 3
deploymentName=
[ ! -s "$work/calls" ] || fail "curl called for a bad name"
echo "ok: badName"

deploymentVersion='1.2.3&name=other'
run badVersion keep 2 3
deploymentVersion=
[ ! -s "$work/calls" ] || fail "curl called for a bad version"
echo "ok: badVersion"

run badMode purge 2 3
expectSummary ""
echo "ok: badMode"

deploymentName=cocoatouch-1.2.3-dryrun-7
run keepNeedsTheReleaseName keep 2 3
deploymentName=
[ ! -s "$work/calls" ] || fail "curl called for a mismatched keep name"
grep -Fq 'needs a deployment named cocoatouch-1.2.3' "$work/out" || fail "expected name not printed"
echo "ok: keepNeedsTheReleaseName"

deploymentName=cocoatouch-1.2.3
run dropNeedsADryRunName drop 2 3
deploymentName=
[ ! -s "$work/calls" ] || fail "curl called for a mismatched drop name"
grep -Fq 'needs a deployment named cocoatouch-1.2.3-dryrun-' "$work/out" || fail "expected name not printed"
echo "ok: dropNeedsADryRunName"

deploymentName=cocoatouch-1.2.3-dryrun-
run dropNeedsARunId drop 2 3
deploymentName=
[ ! -s "$work/calls" ] || fail "curl called for a dry-run name without a run id"
echo "ok: dropNeedsARunId"

printf 'VALIDATED\n' > "$work/polls"
deploymentName=cocoatouch-1.2.3-dryrun-7
run dropAcceptsADryRunName drop 0 3
deploymentName=
expectSummary "deployment 28570f16-da32-4c14-bd2e-c1acc0782365 uploaded
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 VALIDATED
deployment 28570f16-da32-4c14-bd2e-c1acc0782365 dropped"
expectDeleteCount 1
echo "ok: dropAcceptsADryRunName"

echo html > "$work/upload"
run uploadResponseIsNotAnId keep 1 3
echo 200 > "$work/upload"
expectSummary ""
expectDeleteCount 0
grep -Fq '<html>maintenance' "$work/err" || fail "body not on stderr"
! grep -q '/status' "$work/calls" || fail "polled with a bad id"
echo "ok: uploadResponseIsNotAnId"

echo "all upload script scenarios passed"
