#!/bin/sh
set -eu

repo=${1:?usage: check-release-environment.sh <owner/repo> <environment>}
environment=${2:?usage: check-release-environment.sh <owner/repo> <environment>}
path="repos/$repo/environments/$environment"
failed=0

fail() {
    echo "$1" >&2
    failed=1
}

details=$(gh api "$path") || {
    printf '%s\n' "$details" >&2
    echo "environment $environment does not exist; create it with a required reviewer and a deployment policy before the first run" >&2
    exit 1
}

reviewerCount=$(printf '%s' "$details" | jq '[.protection_rules[]? | select(.type == "required_reviewers") | select((.reviewers | length) > 0)] | length')
[ "$reviewerCount" -gt 0 ] || fail "environment $environment has no required reviewers"

selfReviewCount=$(printf '%s' "$details" | jq '[.protection_rules[]? | select(.type == "required_reviewers") | select(.prevent_self_review == true)] | length')
[ "$selfReviewCount" -gt 0 ] || fail "environment $environment does not set prevent_self_review"

customPolicy=$(printf '%s' "$details" | jq -r '.deployment_branch_policy.custom_branch_policies // false')
[ "$customPolicy" = "true" ] || fail "environment $environment does not set custom_branch_policies"

policies=$(gh api "$path/deployment-branch-policies") || {
    printf '%s\n' "$policies" >&2
    exit 1
}

hasPolicy() {
    count=$(printf '%s' "$policies" | jq --arg name "$1" --arg type "$2" '[.branch_policies[]? | select(.name == $name and .type == $type)] | length')
    [ "$count" -gt 0 ]
}

hasPolicy master branch || fail "environment $environment has no deployment policy for branch master"
hasPolicy 'v*' tag || fail "environment $environment has no deployment policy for tag v*"

[ "$failed" -eq 0 ] || exit 1
echo "environment $environment protected: reviewers, prevent_self_review, policies master + v*"
