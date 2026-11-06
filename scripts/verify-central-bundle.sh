set -eu

zip=${1:?usage: verify-central-bundle.sh <zip> <version> [--unsigned]}
version=${2:?usage: verify-central-bundle.sh <zip> <version> [--unsigned]}
unsigned=${3:-}
dir="com/starkbank/cocoatouch/$version"
listing=$(mktemp)
trap 'rm -f "$listing"' EXIT
unzip -Z1 "$zip" | grep -v '/$' > "$listing" || true
status=0

outside=$(grep -v "^$dir/" "$listing" || true)
if [ -n "$outside" ]; then
    echo "outside $dir: $outside"
    status=1
fi

bases="cocoatouch-$version.aar cocoatouch-$version.pom cocoatouch-$version.module cocoatouch-$version-sources.jar cocoatouch-$version-javadoc.jar"
suffixes=".md5 .sha1"
[ "$unsigned" = "--unsigned" ] || suffixes="$suffixes .asc"
for base in $bases; do
    for suffix in "" $suffixes; do
        grep -qx "$dir/$base$suffix" "$listing" || { echo "missing: $dir/$base$suffix"; status=1; }
    done
done

pom=$(unzip -p "$zip" "$dir/cocoatouch-$version.pom" 2>/dev/null || true)
for tag in name description url license developer scm; do
    printf '%s' "$pom" | grep -q "<$tag>" || { echo "pom missing: <$tag>"; status=1; }
done

[ "$status" -eq 0 ] || exit 1
echo "bundle OK: $(wc -l < "$listing" | tr -d ' ') files"
