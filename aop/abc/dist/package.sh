#!/bin/sh -e

# Version of the package-body.shtml file to target; update this if you
# make an incompatible change in that file
PACKAGE='1'
# The Jedd runtime version
JEDD='0.2'
# The Xact version
XACT='1.0-1'

DATE=`date +%Y%m%d%H%M%S`

case "$1" in
  release)
	PREREL=
	;;
  snapshot)
	PREREL=$DATE
	;;
  *)
	echo "Please select one of 'snapshot' or 'release'"
	exit 1;
esac

ant clobber
mkdir -p classes/abc/main
echo "prerelease=$PREREL" > classes/abc/main/version.properties
# FIXME : URL refs for javadoc
ant jars javadoc runtime-javadoc copy-jars
rm classes/abc/main/version.properties

VERSION=`CLASSPATH=lib/abc-complete.jar java abc.aspectj.Version`

rm -rf package/abc-$VERSION

mkdir -p package/abc-$VERSION
mkdir -p package/abc-$VERSION/bin
cp -a build.xml CREDITS LESSER-GPL CPL LICENSING ant.settings.template lib/ \
      src/ runtime-src/ testing-src/ generated/ \
      ajc-harness/ javadoc/ runtime-javadoc/ \
      dist/abc-for-ajc-ant/ \
      package/abc-$VERSION/
cp dist/abc package/abc-$VERSION/bin/
cp dist/abc.bat package/abc-$VERSION/bin/


cd package

BINS="\
   abc-$VERSION/bin/abc \
   abc-$VERSION/bin/abc.bat \
   abc-$VERSION/lib \
   abc-$VERSION/runtime-javadoc/ \
"

SRCS="\
   abc-$VERSION/build.xml \
   abc-$VERSION/CREDITS \
   abc-$VERSION/LESSER-GPL \
   abc-$VERSION/CPL \
   abc-$VERSION/LICENSING \
   abc-$VERSION/ant.settings.template \
   abc-$VERSION/src/ \
   abc-$VERSION/generated/ \
   abc-$VERSION/runtime-src/ \
   abc-$VERSION/testing-src/ \
   abc-$VERSION/ajc-harness/ \
   abc-$VERSION/dist/abc-for-ajc-ant/ \
"


rm -f abc-$VERSION-bin.*
rm -f abc-$VERSION-src.*
tar --exclude-from ../dist/tar-excludes -czvf abc-$VERSION-bin.tar.gz $BINS
tar --exclude-from ../dist/tar-excludes -czvf abc-$VERSION-src.tar.gz $SRCS

rm -rf soot-dev-$DATE/
rm -f soot-dev-$DATE.tar.gz soot-dev-$DATE.zip
svn export /usr/local/src/soot-dev soot-dev-$DATE
find soot-dev-$DATE/generated/ -name \*.java -exec touch {} \;
tar -czvf soot-dev-$DATE.tar.gz soot-dev-$DATE/

rm -rf jasmin-dev-$DATE/
rm -f jasmin-dev-$DATE.tar.gz jasmin-dev-$DATE.zip
svn export /usr/local/src/jasmin-dev jasmin-dev-$DATE
tar -czvf jasmin-dev-$DATE.tar.gz jasmin-dev-$DATE/

rm -rf polyglot-dev-$DATE/
rm -f polyglot-dev-$DATE.tar.gz polyglot-dev-$DATE.zip
cp -a /usr/local/src/polyglot-dev ./polyglot-dev-$DATE
# only a rough approximation of what's needed...
tar --exclude CVS --exclude update.sh --exclude lib/ --exclude classes/ \
    --exclude cup-classes/ --exclude \*.class --exclude lib/*.jar \
    -czvf polyglot-dev-$DATE.tar.gz polyglot-dev-$DATE/

for d in abc-$VERSION-bin abc-$VERSION-src soot-dev-$DATE polyglot-dev-$DATE \
         jasmin-dev-$DATE
 do mkdir tmp
    cd tmp
    tar -xzvf ../$d.tar.gz
    zip -r ../$d.zip *
    cd ..
    rm -rf tmp
done

mkdir -p ../dists

rm -rf ../dists/$VERSION
mkdir -p ../dists/$VERSION/files
for root in abc-$VERSION-bin abc-$VERSION-src soot-dev-$DATE \
            polyglot-dev-$DATE jasmin-dev-$DATE
 do cp $root.tar.gz $root.zip ../dists/$VERSION/files/
done

mkdir ../dists/$VERSION/files/lib
cp abc-$VERSION/lib/*.jar ../dists/$VERSION/files/lib/
cp /usr/local/src/xact/java/xact-complete.jar \
    ../dists/$VERSION/files/lib/xact-complete-$XACT.jar
cp /usr/local/src/soot-dev/lib/jedd-runtime-$JEDD.jar \
    ../dists/$VERSION/files/lib/jedd-runtime-$JEDD.jar

mkdir ../dists/$VERSION/files/bin
cp abc-$VERSION/bin/abc ../dists/$VERSION/files/bin
cp abc-$VERSION/bin/abc.bat ../dists/$VERSION/files/bin

cp -a abc-$VERSION/runtime-javadoc ../dists/$VERSION/files/

echo "<!--#set var=\"version\" value=\"$VERSION\"-->" \
    > ../dists/$VERSION/package.shtml
echo "<!--#set var=\"sootversion\" value=\"dev-$DATE\"-->" \
    >> ../dists/$VERSION/package.shtml
echo "<!--#set var=\"jasminversion\" value=\"dev-$DATE\"-->" \
    >> ../dists/$VERSION/package.shtml
echo "<!--#set var=\"polyglotversion\" value=\"dev-$DATE\"-->" \
    >> ../dists/$VERSION/package.shtml
echo "<!--#set var=\"jeddversion\" value=\"$JEDD\"-->" \
    >> ../dists/$VERSION/package.shtml
echo "<!--#set var=\"xactversion\" value=\"$XACT\"-->" \
    >> ../dists/$VERSION/package.shtml
echo "<!--#include file=\"package-body-v$PACKAGE.shtml\"-->" \
    >> ../dists/$VERSION/package.shtml

cd ../dists/$VERSION/
ln -sf ../../includes/package-body-v$PACKAGE.shtml
ln -sf ../../includes/footer.shtml
ln -sf ../../includes/header.shtml

cd ..

chmod -R g+w $VERSION

echo Done.