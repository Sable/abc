#!/bin/sh -e

# Version of the package-body.shtml file to target; update this if you
# make an incompatible change in that file
PACKAGE='6'
# The Jedd runtime version
JEDD='snapshot'
# The Xact version
XACT='1.0-1'
# The JavaBDD version
JAVABDD='0.6'
# The Paddle version
PADDLE='snapshot'

# path to ajc-harness
TESTSUITE=$PWD/../abc-testing/ajc-harness

# directory to package into
PACKAGE_DIR=$PWD/package

# dists dir
DISTS_DIR=$PWD/dists

# abc directory relative to abc-dist
ABC_DIR='../abc'

# extensions to bundle
BUNDLED_EXTS='../abc-ja ../abc-ja-exts'

# these will potentially be out of sync slightly, but never mind
UNIXTIME=`date +%s`
DATE=`date +%Y%m%d%H%M%S`

if ! realpath -h &> /dev/null ; then
	echo "Please install 'realpath'..."
	exit 2
fi
	

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

# for reference
OLD_PWD=$PWD

# === build abc proper === #
cd $ABC_DIR
ant clobber || exit 3
mkdir -p classes/abc/main
echo "prerelease=$PREREL" > classes/abc/main/version.properties
# FIXME : URL refs for javadoc
ant jars javadoc runtime-javadoc options-doc copy-jars || exit 4
rm classes/abc/main/version.properties

VERSION=`CLASSPATH=lib/abc-complete.jar java abc.aspectj.Version`

TARGET=$PACKAGE_DIR/abc-$VERSION

rm -rf $TARGET

mkdir -p $TARGET
mkdir -p $TARGET/abc
mkdir -p $TARGET/abc/bin
mkdir -p $TARGET/abc/doc
cp -a build.xml CREDITS LESSER-GPL CPL LICENSING ant.settings.template $TARGET/abc
cp -a lib/ src/ runtime-src/ testing-src/ paddle-src/ generated/ \
      ajc-harness/ $TESTSUITE javadoc/ runtime-javadoc/ dist/ \
      $TARGET/abc
cp $OLD_PWD/abc $TARGET/abc/bin/
cp $OLD_PWD/abc.bat $TARGET/abc/bin/
cp doc/options/usage.pdf $PACKAGE_DIR/abc-$VERSION/abc/doc/
cp doc/options/usage.ps $TARGET/abc/doc/
cd $OLD_PWD

BINS="\
   abc-$VERSION/abc/bin/abc \
   abc-$VERSION/abc/bin/abc.bat \
   abc-$VERSION/abc/lib \
   abc-$VERSION/abc/runtime-javadoc/ \
   abc-$VERSION/abc/doc/usage.pdf \
   abc-$VERSION/abc/doc/usage.ps \
"

SRCS="\
   abc-$VERSION/abc/build.xml \
   abc-$VERSION/abc/CREDITS \
   abc-$VERSION/abc/LESSER-GPL \
   abc-$VERSION/abc/CPL \
   abc-$VERSION/abc/LICENSING \
   abc-$VERSION/abc/ant.settings.template \
   abc-$VERSION/abc/src/ \
   abc-$VERSION/abc/paddle-src/ \
   abc-$VERSION/abc/generated/ \
   abc-$VERSION/abc/runtime-src/ \
   abc-$VERSION/abc/testing-src/ \
   abc-$VERSION/abc/ajc-harness/ \
   abc-$VERSION/abc/dist/abc-for-ajc-ant/ \
   abc-$VERSION/abc/dist/abc-debian \
   abc-$VERSION/abc/dist/abc \
"

# === build bundled extensions === #
for EXT in $BUNDLED_EXTS ; do
	ext=`basename $EXT`
	cd $OLD_PWD/$EXT
	ant clean jars || exit 5
	rm -rf $TARGET/$ext
	mkdir -p $TARGET/$ext
	cp -a --parents `cat srcs.list` $TARGET/$ext
	cp -a --parents `cat bins.list` $TARGET/$ext
	BINS="$BINS `perl -p -e \"s/^(.)/abc-$VERSION\/$ext\/\$&/; s|[^/]+/\.\./||g;\" bins.list`"
	SRCS="$SRCS `perl -p -e \"s/^(.)/abc-$VERSION\/$ext\/\$&/; s|[^/]+/\.\./||g;\" srcs.list`"
	cd $OLD_PWD
done

cd $PACKAGE_DIR

rm -f abc-$VERSION-bin.*
rm -f abc-$VERSION-src.*
tar --exclude-from $OLD_PWD/tar-excludes -czvf abc-$VERSION-bin.tar.gz $BINS
tar --exclude-from $OLD_PWD/tar-excludes -czvf abc-$VERSION-src.tar.gz $SRCS

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

rm -rf tmp

for d in abc-$VERSION-bin abc-$VERSION-src soot-dev-$DATE polyglot-dev-$DATE \
         jasmin-dev-$DATE
 do mkdir $PACKAGE_DIR/tmp
    cd $PACKAGE_DIR/tmp
    tar -xzvf ../$d.tar.gz
    zip -r ../$d.zip *
    cd $PACKAGE_DIR
    rm -rf tmp
done

mkdir -p $PACKAGE_DIR/tmp/abc-$VERSION
cd $PACKAGE_DIR/tmp/abc-$VERSION

for root in abc-$VERSION-bin abc-$VERSION-src soot-dev-$DATE \
            polyglot-dev-$DATE jasmin-dev-$DATE
 do cp ../../$root.tar.gz .
done

cp /usr/local/src/xact/java/xact-complete.jar \
    xact-complete-$XACT.jar
cp /usr/local/src/jedd-dev/runtime/lib/jedd-runtime.jar \
    jedd-runtime-$JEDD.jar
cp /usr/local/src/paddle-dev/lib/paddle-custom.jar \
    paddle-$PADDLE.jar
cp /usr/local/src/resources/JavaBDD/javabdd_$JAVABDD.jar \
    javabdd_$JAVABDD.jar
cp /usr/local/src/resources/javabdd_src_$JAVABDD.tar.gz \
    javabdd_src_$JAVABDD.tar.gz

mkdir debian

cp $OLD_PWD/debian/control debian/
touch debian/rules
chmod 755 debian/rules
echo '#!/usr/bin/make -f' >> debian/rules
echo "export PREREL=$PREREL" >> debian/rules
echo "export ABC_VER=$VERSION" >> debian/rules
echo "export PADDLE_VER=$PADDLE" >> debian/rules
echo "export JEDD_VER=$JEDD" >> debian/rules
echo "export JAVABDD_VER=$JAVABDD" >> debian/rules
echo "export XACT_VER=$XACT" >> debian/rules
echo "export SOOT_VER=dev-$DATE" >> debian/rules
echo "export POLYGLOT_VER=dev-$DATE" >> debian/rules
echo "export JASMIN_VER=dev-$DATE" >> debian/rules
cat $OLD_PWD/debian/rules >> debian/rules

$OLD_PWD/addchangelogrelease.pl $VERSION $UNIXTIME \
   < $OLD_PWD/$ABC_DIR/CHANGELOG \
   | $OLD_PWD/makechangelogs.pl debian \
   > debian/changelog

# doesn't work for now
# dpkg-buildpackage -rfakeroot -uc -us

cd $PACKAGE_DIR
for d in abc_$VERSION.dsc abc_$VERSION.tar.gz abc_${VERSION}_all.deb \
         abc_${VERSION}_i386.changes
   do mv tmp/$d .
done
rm -rf tmp

mkdir -p $DISTS_DIR

rm -rf $DISTS_DIR/$VERSION
mkdir -p $DISTS_DIR/$VERSION/files
for root in abc-$VERSION-bin abc-$VERSION-src soot-dev-$DATE \
            polyglot-dev-$DATE jasmin-dev-$DATE
 do cp $root.tar.gz $root.zip $DISTS_DIR/$VERSION/files/
done

mkdir $DISTS_DIR/$VERSION/files/lib
cp abc-$VERSION/*/lib/*.jar $DISTS_DIR/$VERSION/files/lib/
cp /usr/local/src/xact/java/xact-complete.jar \
    $DISTS_DIR/$VERSION/files/lib/xact-complete-$XACT.jar
cp /usr/local/src/jedd-dev/runtime/lib/jedd-runtime.jar \
    $DISTS_DIR/$VERSION/files/lib/jedd-runtime-$JEDD.jar
cp /usr/local/src/paddle-dev/lib/paddle-custom.jar \
    $DISTS_DIR/$VERSION/files/lib/paddle-$PADDLE.jar
cp /usr/local/src/resources/JavaBDD/javabdd_$JAVABDD.jar \
    $DISTS_DIR/$VERSION/files/lib/javabdd_$JAVABDD.jar
cp /usr/local/src/resources/javabdd_src_$JAVABDD.tar.gz \
    $DISTS_DIR/$VERSION/files/lib/javabdd_src_$JAVABDD.tar.gz

mkdir $DISTS_DIR/$VERSION/files/bin
cp abc-$VERSION/abc/bin/abc $DISTS_DIR/$VERSION/files/bin
cp abc-$VERSION/abc/bin/abc.bat $DISTS_DIR/$VERSION/files/bin

mkdir $DISTS_DIR/$VERSION/files/doc
cp abc-$VERSION/abc/doc/usage.ps $DISTS_DIR/$VERSION/files/doc
cp abc-$VERSION/abc/doc/usage.pdf $DISTS_DIR/$VERSION/files/doc

mkdir $DISTS_DIR/$VERSION/files/debian
for d in abc_$VERSION.dsc abc_$VERSION.tar.gz abc_${VERSION}_all.deb \
         abc_${VERSION}_i386.changes
   do cp $d $DISTS_DIR/$VERSION/files/debian
done

cp -a abc-$VERSION/abc/runtime-javadoc $DISTS_DIR/$VERSION/files/

echo "<!--#set var=\"version\" value=\"$VERSION\"-->" \
    > $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"sootversion\" value=\"dev-$DATE\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"jasminversion\" value=\"dev-$DATE\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"polyglotversion\" value=\"dev-$DATE\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"jeddversion\" value=\"$JEDD\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"paddleversion\" value=\"$PADDLE\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"javabddversion\" value=\"$JAVABDD\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#set var=\"xactversion\" value=\"$XACT\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml
echo "<!--#include file=\"package-body-v$PACKAGE.shtml\"-->" \
    >> $DISTS_DIR/$VERSION/package.shtml

cd $DISTS_DIR/$VERSION/
ln -sf ../../includes/package-body-v$PACKAGE.shtml
ln -sf ../../includes/footer.shtml
ln -sf ../../includes/header.shtml

cd ..

chmod -R g+w $VERSION

echo Made $VERSION $UNIXTIME.
