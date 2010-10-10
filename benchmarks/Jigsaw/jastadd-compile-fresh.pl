#!/usr/bin/perl
#
my $DESTDIR = "./jastadd.fresh.build";
#my $ADD_SOURCES = "./Jigsaw/Install.java";

my $CLASSPATH = $ENV{CLASSPATH};
my $JAVA_HOME = $ENV{JAVA_HOME}; #sun j2sdk
my $TOOLSJAR = "$JAVA_HOME/lib/tools.jar";
my $TIDYJAR = "./classes/Tidy.jar";
my $SERVLETJAR = "./classes/servlet.jar";
my $OROJAR = "./classes/jakarta-oro-2.0.8.jar";
my $ADD_FLAGS = "";
my $ADD_CP = "./benchmark:";

my $XALANJAR = "./classes/xerces.jar";

my $JSDKJARS = "$JAVA_HOME/jre/lib/charsets.jar:$JAVA_HOME/jre/lib/jce.jar:$JAVA_HOME/jre/lib/jsse.jar:$JAVA_HOME/jre/lib/plugin.jar:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/jre/lib/sunrsasign.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/htmlconverter.jar:$JAVA_HOME/lib/tools.jar:";
my $ALT_JSDKJARS = "$ALT_JAVA_HOME/jre/lib/charsets.jar:$ALT_JAVA_HOME/jre/lib/jce.jar:$ALT_JAVA_HOME/jre/lib/jsse.jar:$ALT_JAVA_HOME/jre/lib/plugin.jar:$ALT_JAVA_HOME/jre/lib/rt.jar:$ALT_JAVA_HOME/jre/lib/sunrsasign.jar:$ALT_JAVA_HOME/lib/dt.jar:$ALT_JAVA_HOME/lib/htmlconverter.jar:$ALT_JAVA_HOME/lib/tools.jar:";

my $cmdline="jastadd_javac -classpath \"$JSDKJARS:$ALT_JSDKJARS:$XALANJAR:$OROJAR:$TIDYJAR:$SERVLETJAR:$TOOLSJAR:$CLASSPATH:$ADD_CP\" -sourcepath src/classes -d $DESTDIR $ADD_FLAGS $ADD_SOURCES @ARGV";

print "Executing: $cmdline\n";

system $cmdline;
