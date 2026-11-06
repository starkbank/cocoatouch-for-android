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
cat > /dev/null
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
    */upload*)
        if [ "$(cat "$STUB_DIR/upload")" = "401" ]; then
            echo '{"error":"unauthorized"}'
            exit 22
        fi
        echo dep-42
        ;;
    */status*)
        count=$(cat "$STUB_DIR/pollCount" 2>/dev/null || echo 0)
        count=$((count + 1))
        echo "$count" > "$STUB_DIR/pollCount"
        answer=$(sed -n "${count}p" "$STUB_DIR/polls")
        [ -n "$answer" ] || answer=$(tail -n 1 "$STUB_DIR/polls")
        if [ "$answer" = "503" ]; then
            echo '{"error":"unavailable"}'
            exit 22
        fi
        echo "{\"deploymentState\":\"$answer\",\"errors\":{\"pom\":[\"bad pom\"]}}"
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
    attempts=$4
    rm -f "$work/calls" "$work/pollCount" "$work/summary"
    : > "$work/summary"
    code=0
    PATH="$work/bin:$PATH" CENTRAL_POLL_ATTEMPTS=$attempts sh "$script" "$work/bundle.zip" cocoatouch-test "$mode" > "$work/out" 2> "$work/err" || code=$?
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

printf 'VALIDATED\n' > "$work/polls"
run validatedKeep keep 0 3
expectSummary "deployment dep-42 uploaded
deployment dep-42 VALIDATED"
expectDeleteCount 0
echo "ok: validatedKeep"

run validatedDrop drop 0 3
expectSummary "deployment dep-42 uploaded
deployment dep-42 VALIDATED
deployment dep-42 dropped"
expectDeleteCount 1
grep -Fq 'deployment dep-42 dropped' "$work/out" || fail "dropped not printed"
echo "ok: validatedDrop"

printf 'FAILED\n' > "$work/polls"
run failed keep 1 3
expectSummary "deployment dep-42 uploaded
deployment dep-42 FAILED"
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
expectSummary "deployment dep-42 uploaded
deployment dep-42 VALIDATED"
grep -Fq 'unavailable' "$work/err" || fail "poll body not on stderr"
echo "ok: pollRecovers"

printf 'PENDING\n' > "$work/polls"
run timeoutDrop drop 1 3
expectSummary "deployment dep-42 uploaded
deployment dep-42 timeout"
expectDeleteCount 1
echo "ok: timeoutDrop"

run badMode purge 2 3
expectSummary ""
echo "ok: badMode"

echo "all upload script scenarios passed"
