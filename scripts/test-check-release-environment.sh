#!/bin/sh
set -eu

here=$(cd "$(dirname "$0")" && pwd)
script="$here/check-release-environment.sh"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir "$work/bin"

cat > "$work/bin/gh" <<'STUB'
#!/bin/sh
printf '%s\n' "$*" >> "$STUB_DIR/calls"
case "$2" in
    */deployment-branch-policies)
        cat "$STUB_DIR/policies.json"
        ;;
    *)
        cat "$STUB_DIR/environment.json"
        exit "$(cat "$STUB_DIR/environment.code")"
        ;;
esac
STUB
chmod +x "$work/bin/gh"

export STUB_DIR="$work"

fail() {
    echo "FAIL $scenario: $1" >&2
    exit 1
}

protectedEnvironment='{"protection_rules":[{"type":"required_reviewers","prevent_self_review":true,"reviewers":[{"type":"User","reviewer":{"login":"maintainer"}}]}],"deployment_branch_policy":{"protected_branches":false,"custom_branch_policies":true}}'
protectedPolicies='{"total_count":2,"branch_policies":[{"name":"master","type":"branch"},{"name":"v*","type":"tag"}]}'

run() {
    scenario=$1
    expectedCode=$2
    rm -f "$work/calls"
    code=0
    PATH="$work/bin:$PATH" sh "$script" starkbank/cocoatouch-for-android maven-central > "$work/out" 2> "$work/err" || code=$?
    [ "$code" -eq "$expectedCode" ] || fail "exit $code, expected $expectedCode: $(cat "$work/err")"
}

expectError() {
    grep -Fq -- "$1" "$work/err" || fail "stderr lacks: $1 (was: $(cat "$work/err"))"
}

reset() {
    printf '%s\n' "$protectedEnvironment" > "$work/environment.json"
    printf '%s\n' "$protectedPolicies" > "$work/policies.json"
    echo 0 > "$work/environment.code"
}

reset
run protected 0
grep -Fxq 'environment maven-central protected: reviewers, prevent_self_review, policies master + v*' "$work/out" || fail "success line missing"
grep -Fq 'repos/starkbank/cocoatouch-for-android/environments/maven-central' "$work/calls" || fail "environment not queried"
echo "ok: protected"

reset
echo '{"message":"Not Found","status":"404"}' > "$work/environment.json"
echo 1 > "$work/environment.code"
run missingEnvironment 1
expectError "does not exist"
echo "ok: missingEnvironment"

reset
printf '%s\n' '{"protection_rules":[{"type":"required_reviewers","prevent_self_review":true,"reviewers":[]}],"deployment_branch_policy":{"custom_branch_policies":true}}' > "$work/environment.json"
run noReviewers 1
expectError "required reviewers"
echo "ok: noReviewers"

reset
printf '%s\n' '{"protection_rules":[{"type":"required_reviewers","prevent_self_review":false,"reviewers":[{"type":"User"}]}],"deployment_branch_policy":{"custom_branch_policies":true}}' > "$work/environment.json"
run selfReviewAllowed 1
expectError "prevent_self_review"
echo "ok: selfReviewAllowed"

reset
printf '%s\n' '{"protection_rules":[{"type":"required_reviewers","prevent_self_review":true,"reviewers":[{"type":"User"}]}],"deployment_branch_policy":{"protected_branches":true,"custom_branch_policies":false}}' > "$work/environment.json"
run noCustomPolicy 1
expectError "custom_branch_policies"
echo "ok: noCustomPolicy"

reset
printf '%s\n' '{"total_count":1,"branch_policies":[{"name":"v*","type":"tag"}]}' > "$work/policies.json"
run policyWithoutMaster 1
expectError "master"
echo "ok: policyWithoutMaster"

reset
printf '%s\n' '{"total_count":1,"branch_policies":[{"name":"master","type":"branch"}]}' > "$work/policies.json"
run policyWithoutTag 1
expectError "v*"
echo "ok: policyWithoutTag"

echo "all release environment scenarios passed"
