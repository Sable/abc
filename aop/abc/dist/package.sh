#!/bin/sh -e

case "$1" in
  release)
	PREREL=
	;;
  snapshot)
	PREREL=`date +%Y%m%d%H%M%S`
	;;
  *)
	echo "Please select one of 'snapshot' or 'release'"
	exit 1;
esac

ant clobber
mkdir -p classes/abc/main
echo "prerelease=$PREREL" > classes/abc/main/version.properties
# FIXME : URL refs for javadoc
ant jars javadoc runtime-javadoc
rm classes/abc/main/version.properties

VERSION=`CLASSPATH=lib/abc-complete.jar java abc.aspectj.Version`

rm -rf package/abc-$VERSION

mkdir -p package/abc-$VERSION
mkdir -p package/abc-$VERSION/bin
cp -a build.xml lib/ src/ runtime-src/ testing-src/ ajc-harness/ javadoc/ runtime-javadoc/ package/abc-$VERSION/
cp dist/abc package/abc-$VERSION/bin/
cd package

BINS="\
   abc-$VERSION/bin/abc \
   abc-$VERSION/lib/abc-complete.jar \
   abc-$VERSION/lib/abc-runtime.jar \
   abc-$VERSION/lib/abc-for-ajc-ant.jar \
   abc-$VERSION/runtime-javadoc/ \
"

SRCS="\
   abc-$VERSION/src/ \
   abc-$VERSION/runtime-src/ \
   abc-$VERSION/testing-src/ \
   abc-$VERSION/ajc-harness/ \
"


rm -f abc-$VERSION-bin.*
rm -f abc-$VERSION-src.*
tar -czvf abc-$VERSION-bin.tar.gz $BINS
tar --exclude-from ../dist/tar-excludes -czvf abc-$VERSION-src.tar.gz $SRCS

mkdir tmp
cd tmp
tar -xzvf ../abc-$VERSION-bin.tar.gz
zip -r ../abc-$VERSION-bin.zip abc-$VERSION/
cd ..
rm -rf tmp

mkdir tmp
cd tmp
tar -xzvf ../abc-$VERSION-src.tar.gz
zip -r ../abc-$VERSION-src.zip abc-$VERSION/
cd ..
rm -rf tmp

cd ..
