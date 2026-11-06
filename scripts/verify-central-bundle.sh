#!/bin/sh
set -eu

zip=${1:?usage: verify-central-bundle.sh <zip> <version> [--unsigned]}
version=${2:?usage: verify-central-bundle.sh <zip> <version> [--unsigned]}
unsigned=${3:-}
dir="com/starkbank/cocoatouch/$version"
listing=$(mktemp)
trap 'rm -f "$listing"' EXIT
unzip -Z1 "$zip" | grep -v '/$' > "$listing" || true
status=0

outside=$(awk -v prefix="$dir/" 'index($0, prefix) != 1' "$listing")
if [ -n "$outside" ]; then
    echo "outside $dir: $outside"
    status=1
fi

bases="cocoatouch-$version.aar cocoatouch-$version.pom cocoatouch-$version.module cocoatouch-$version-sources.jar cocoatouch-$version-javadoc.jar"
suffixes=".md5 .sha1"
[ "$unsigned" = "--unsigned" ] || suffixes="$suffixes .asc"
for base in $bases; do
    for suffix in "" $suffixes; do
        grep -Fxq "$dir/$base$suffix" "$listing" || { echo "missing: $dir/$base$suffix"; status=1; }
    done
done

pom=$(unzip -p "$zip" "$dir/cocoatouch-$version.pom" 2>/dev/null || true)
projectPom=$(printf '%s' "$pom" | sed '/<licenses>/,$d')
for tag in name description url; do
    printf '%s' "$projectPom" | grep -q "<$tag>" || { echo "missing: <$tag>"; status=1; }
done
for tag in licenses developers scm; do
    printf '%s' "$pom" | grep -q "<$tag>" || { echo "missing: <$tag>"; status=1; }
done

[ "$status" -eq 0 ] || exit 1
echo "bundle OK: $(wc -l < "$listing" | tr -d ' ') files"
