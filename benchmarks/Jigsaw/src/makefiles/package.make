# package.make
# $Id: package.make,v 1.9 1999/08/05 14:01:52 bmahe Exp $
# COPYRIGHT

# This makefile should be included in all packages Makefiles. To use it, define
# the PACKAGES variable to the set of packages defined in your directory,
# and the PACKAGE variable to this package name.
# So, if you have a 'foo' package, included in 'w3c' and containing 'bar1'
# and 'bar2' sub packages, your Makefile should look like this:
# ----------
# PACKAGE=w3c.foo
# PACKAGES=bar1 bar2
# TOP=../..
# include $(TOP)/makefiles/package.make
# ----------
#
# This make file defines the following targets:
# all:	 to build the class files from the java files.
# clean: to clean all sub packages
# doc:   to build the appropriate documentation files from the source
# The 'doc' target uses DESTDIR variable that should point to the absolute 
# path of the target directory (in which doc files will be created).

MAKECMD=gmake -s

all::
	@@for p in $(PACKAGES); do \
		echo 'building ' $(PACKAGE).$$p; \
		if [ -z "$(TARGET)" ]; then \
			(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR)); \
		else \
			(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR) VPATH=$(VPATH)/$$p); \
		fi \
	done

jigsaw::
	@@for p in $(JIGSAWPACKAGES); do \
		echo 'building ' $(PACKAGE).$$p; \
		if [ -z "$(TARGET)" ]; then \
			(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR) jigsaw ); \
		else \
			(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR) VPATH=$(VPATH)/$$p jigsaw); \
		fi \
	done

dumpjigsawpackages::
	@@for p in $(JIGSAWPACKAGES); do \
		(cd $$p ; $(MAKECMD) CL_DIR=$(CL_DIR) dumpjigsawpackages); \
	done

test::
	@@for p in $(JIGSAWPACKAGES); do \
		(cd $$p ; $(MAKECMD) CL_DIR=$(CL_DIR) test); \
	done

doc::
	@@for p in $(PACKAGES); do \
		echo 'doc ' $(PACKAGE).$$p; \
		(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR) DESTDIR=$(DESTDIR) doc); \
	done

clean::
	@@for p in $(PACKAGES); do \
		echo 'cleaning ' $(PACKAGE).$$p; \
		(cd $$p ; $(MAKECMD) clean) ; \
	done

jigsawtag::
	@@if [ "" != "$(TAG)" ]; then \
		echo 'Tagging in package ' $(PACKAGE).$$p; \
		for p in $(JIGSAWPACKAGES); do \
			(cd $$p; $(MAKECMD) CL_DIR=$(CL_DIR) TAG=$(TAG) jigsawtag); \
		done ;\
		cvs tag $(TAG) Makefile; \
	else \
		echo 'set the TAG variable!'; \
	fi
