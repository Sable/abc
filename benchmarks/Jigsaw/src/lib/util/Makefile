
###############################################################################################
# 
# Makefile for libUnix.so and Unix*.class for Unix-specific 
# jigsaw features
#
###
#
# written by gisburn (Roland.Mainz@informatik.med.uni-giessen.de)
# created       4.2.1999
# last modified 4.2.1999
#
### 
# test configuration:
# uname -a      == SunOS puck 5.7 Generic sun4m sparc SUNW,SPARCstation-5
# java -version == Solaris VM (build Solaris_JDK_1.2_01_dev06_fcsV, native threads, sunwjit)
# gcc 2.8.1
# GNU Make version 3.77
# GNU bash, version 2.02.1(1)-release
# and so on...
#


###############################################################################################
# user paths
JNIINCLUDE=/usr/java/include
JNIINC=-I ${JNIINCLUDE} -I ${JNIINCLUDE}/solaris
CLASSPATH=/export/home/gisburn/java/classes


## think twice before editing the stuff below...


###############################################################################################
# CC (tested with gcc 2.8.1)
CC     = gcc
CFLAGS = ${JNIINC} -fpic -O2 -Wall

###############################################################################################
# JAVA stuff
JAVAC      = javac
JAVACFLAGS = -deprecation -classpath ${CLASSPATH}
JAVAH      = javah
JAVAHFLAGS = -classpath ${CLASSPATH}

###############################################################################################
# All required targets
all: Unix.class UnixException.class libUnix.so 

###############################################################################################
# single project targets...	
org_w3c_util_Unix.h: Unix.class 
	${JAVAH} ${JAVAHFLAGS} org.w3c.util.Unix

libUnix.o: org_w3c_util_Unix.h libUnix.c
	${CC} ${CFLAGS}	-c libUnix.c 
	
libUnix.so: libUnix.o
	ld -r -o libUnix.so libUnix.o
	
Unix.class: Unix.java
	${JAVAC} ${JAVACFLAGS} Unix.java
	
# this is normally build by Unix.class -> Unix.java, but...	
UnixException.class: UnixException.java
	${JAVAC} ${JAVACFLAGS} UnixException.java			

###############################################################################################
# misc. development targets	
clean: 
	-rm Unix.class UnixException.class org_w3c_util_Unix.h libUnix.so libUnix.o	

###############################################################################################		
# done.
		