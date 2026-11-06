#!/bin/sh
set -eu

here=$(cd "$(dirname "$0")" && pwd)
script="$here/verify-central-bundle.sh"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
dir=com/starkbank/cocoatouch/1.2.3

writePom() {
    cat > "$1" <<POM
<project>
  <name>cocoatouch</name>
  <description>library</description>
  $projectUrl
  <licenses><license><name>MIT</name><url>https://example.com/license</url></license></licenses>
  <developers><developer><id>x</id></developer></developers>
  $scm
</project>
POM
}

stage() {
    rm -rf "$work/stage" "$work/bundle.zip"
    mkdir -p "$work/stage/$dir"
    projectUrl='<url>https://example.com</url>'
    scm='<scm><url>https://example.com/scm</url></scm>'
    for base in cocoatouch-1.2.3.aar cocoatouch-1.2.3.pom cocoatouch-1.2.3.module cocoatouch-1.2.3-sources.jar cocoatouch-1.2.3-javadoc.jar; do
        case "$base" in
            *.pom) writePom "$work/stage/$dir/$base" ;;
            *) echo content > "$work/stage/$dir/$base" ;;
        esac
        for suffix in .md5 .sha1 .asc; do
            echo checksum > "$work/stage/$dir/$base$suffix"
        done
    done
}

repackPom() {
    writePom "$work/stage/$dir/cocoatouch-1.2.3.pom"
}

check() {
    scenario=$1
    expectedCode=$2
    expectedText=$3
    shift 3
    rm -f "$work/bundle.zip"
    (cd "$work/stage" && zip -qr "$work/bundle.zip" .)
    code=0
    sh "$script" "$work/bundle.zip" "$@" > "$work/out" 2>&1 || code=$?
    [ "$code" -eq "$expectedCode" ] || { echo "FAIL $scenario: exit $code, expected $expectedCode" >&2; cat "$work/out" >&2; exit 1; }
    grep -Fq "$expectedText" "$work/out" || { echo "FAIL $scenario: output lacks '$expectedText'" >&2; cat "$work/out" >&2; exit 1; }
    echo "ok: $scenario"
}

stage
check goodSigned 0 "bundle OK" 1.2.3

stage
find "$work/stage" -name '*.asc' -delete
check goodUnsigned 0 "bundle OK" 1.2.3 --unsigned

stage
rm "$work/stage/$dir/cocoatouch-1.2.3.aar.asc"
check missingSignature 1 "missing: $dir/cocoatouch-1.2.3.aar.asc" 1.2.3

stage
mkdir -p "$work/stage/com/starkbank/cocoatouch/0.0.0-ci"
echo stray > "$work/stage/com/starkbank/cocoatouch/0.0.0-ci/x"
check entryOutsideVersionDir 1 "outside $dir" 1.2.3

stage
scm=
repackPom
check pomWithoutScm 1 "missing: <scm>" 1.2.3

stage
projectUrl=
repackPom
check projectUrlMissing 1 "missing: <url>" 1.2.3

stage
check wrongVersionArgument 1 "outside com/starkbank/cocoatouch/9.9.9" 9.9.9

echo "all bundle verifier scenarios passed"
