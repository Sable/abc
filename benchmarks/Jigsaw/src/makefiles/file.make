# file.make
# $Id: file.make,v 1.10 1999/08/05 13:50:03 bmahe Exp $
# COPYRIGHT

# This makefile should be included in all leaves packages directory. It uses
# the FILES variable to know what are the files to be compiled.
# For example, if you have a package 'foo' containing 'a.java' and 'b.java'
# your Makefile should look like this:
# ----------
# PACKAGE=foo
# FILES=a.java b.java
# TOP=../..
# include $(TOP)/makefiles/files.make
# ----------
#
# This file defines the following targets:
# all:	 to build the class files from the java files.
# clean: to clean all sub packages
# doc:   to build the appropriate documentation files from the source
# The 'doc' target uses DESTDIR variable that should point to the absolute 
# path of the target directory (in which doc files will be created).

ifndef CL_DIR
	CL_DIR=classes
endif

JAVAC=javac -classpath $(TOP)/$(CL_DIR):$$CLASSPATH

.SUFFIXES: .java .class

.java.class:
	@@echo "Compiling " $< ; \
	if [ -z "$(TARGET)" ]; then \
		$(JAVAC) $(JAVAFLAGS) $<; \
	else \
		$(JAVAC) -d $(TARGET) $(JAVAFLAGS) $<; \
	fi

all:: $(FILES:.java=.class)

jigsaw:: $(JIGSAWFILES:.java=.class)

dumpjigsawpackages::
	@@echo $(PACKAGE)	

test::
	@@for f in $(JIGSAWFILES); do \
		echo $(PACKAGE).$$f; \
	done

doc::
	javadoc -sourcepath $(TOP)/$(CL_DIR) -d $(DESTDIR) $(FILES)

clean::
	@@rm -rf *~ *.class

jigsawtag::
	@@if [ "" != "$(TAG)" ]; then \
		for f in $(JIGSAWFILES); do \
			cvs tag $(TAG) $$f; \
		done \
	fi	
