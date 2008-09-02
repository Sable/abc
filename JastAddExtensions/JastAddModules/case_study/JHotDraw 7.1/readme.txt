-------------------
Quick instructions:
-------------------

The build files for the examples are in the directory

	JHotDraw 7.1/jhotdraw7
	
There are 7 samples that can be built:

	build-draw-jastadd.xml
	build-net-jastadd.xml
	build-odg-jastadd.xml
	build-pert-jastadd.xml
	build-simple-jastadd.xml
	build-svg-jastadd.xml
	build-teddy-jastadd.xml

There are also cygwin versions of the above build files (just insert -cygwin before .xml).

The source for the jhotdraw case study can be found in

	JHotDraw 7.1/jhotdraw7/src/main/java
	
This directory should also contain the .module files used as examples in
the paper.  

To build a sample, do:

	ant -f <buildfile>

Make sure that you use the appropriate version of the build file for your OS.
	
Make sure that your ANT_OPTS environment variable is set to at least:

	-Xmx512M
	
Builds are tested and should work with the Java 1.6 SDK on cygwin (winxp). It
should also work on Linux machines.
	
The builds should create the class files in the directory

	JHotdraw 7.1/jhotdraw7/jastadd-build/classes
	
This directory should also contain the script "run", which when
executed with bash should execute the Main class of the JHotdraw sample. To execute
it using bash:

	bash run
	
If you do not use bash as your shell, you may need to modify the run script to run the 
sample application.

--------------------------------------
A short description of the case study:
--------------------------------------

The JHotdraw source is composed of the following major components:
	
	JHotdraw core (org.jhotdraw)
	org.apache.batik (3 versions: org.jhotdraw.batikfragment, org.apache.batik1_6 and org.apache.batik1_8pre)
	net.n3.nanoxml (patch: net.n3.nanoxmlv2_2_1_4patch)
	sample applications:
		draw (org.jhotdraw.samples.draw)
		net (org.jhotdraw.samples.net)
		odg (org.jhotdraw.samples.odg)
		pert (org.jhotdraw.samples.pert)
		simple (org.jhotdraw.samples.simple)
		svg (org.jhotdraw.samples.svg)
		teddy (org.jhotdraw.samples.teddy)
		
The JHotdraw core depends on batik and nanoxml. All the sample applications are in
turn dependent on the JHotdraw core.

The module files for these components are named appropriately, with the component's name
suffixed with .module. The module files can be found in 
	JHotdraw 7.1/jhotdraw7/src/main/java

---------------
The Components:
---------------

The JHotdraw core (org.jhotdraw) uses the libraries org.apache.batik and net.n3.nanoxml.
It uses the version of batik provided with jhotdraw (org.jhotdraw.batikfragment), and
a patched version of nanoxml (net.n3.nanoxmlv2_2_1_4patch).

Nanoxml, an xml parser, (net.n3.nanoxmlv2_2_1_4patch) is a patched version of 
net.n3.nanoxml made specifically for JHotdraw.

Batik (org.apache.batik) is a library for scalable vector graphics support. There are
three version present in the case study: org.jhotdraw.batikfragment, which is a fragment
of batik that was provided with JHotdraw 7.1; org.apache.batik1_6, which is a fragment
of the 1.6 release of batik; and org.apache.batik1_8pre, which is a fragment of the 
snapshot of batik dated 2008-06-16. These are used extensively to demonstrate how
replace and merge can be used to update old dependecies.

Version classes have been added to jhotdraw core, nanoxml and batik to make it 
easier to demonstrate how the module system changes the way class lookup is done.

The sample applications (except for simple) all derive from the default sample
(org.jhotdraw.samples.defaultsample). The default sample uses an extended version
of the JHotdraw core (org.jhotdraw.samples.defaultsample.defaultjhotdraw) which
modifies the AboutAction class to add the batik version to the About dialog text.
The source for the modified AboutAction can be found in
	src/main/java/org/jhotdraw/samples/default/app/action/AboutAction.java
The overriden class is in
	src/main/java/org/jhotdraw/app/action/AboutAction.java
This shows how module extension can be used to change the behavior of an existing
module.

The simple example (org.jhotdraw.samples.simple) shows a basic example of how 
modules can be used. It directly imports an instance of org.jhotdraw, and gets the
versions of its dependencies. The source for the sample can be found in
	src/main/java/org/jhotdraw/samples/simple/Main.java

The draw sample (org.jhotdraw.samples.draw) provides the quintessential JHotdraw
sample application, with support for most of the features in the JHotdraw core. In
the case study, it uses its own modified version of JHotdraw core 
(org.jhotdraw.samples.draw.drawjhotdraw), again modifying the AboutAction to add 
additional text. The source for the modified AboutAction can be found in  
	src/main/java/org/jhotdraw/samples/draw/drawjhotdraw/AboutAction.java
The changes to the AboutAction class can be verified by using the Help->About
menu item when running the draw sample.

The pert and teddy samples are samples that implement a simple PERT chart and a text
editor, respectively. In the case study, they provide baselines for the other samples. They are
subtypes of the default sample, and do not replace any of JHotdraw's dependencies.
When they are run, the About box (accessed by Help->About) should show the batik version
to be "org.jhotdraw batik".
	
The net example (org.jhotdraw.samples.net) implements a simple network diagram editor.
In the case study, this is used to demonstrate replacing an overriden module that is
not present in the build. The net example extends netold (org.jhotdraw.samples.netold),
which replaces Jhotdraw's batik dependency with batik1_7, which is not in the build.
The net sample, in turn, replaces the same dependency with batik1_8pre, which is in the
build. The change in batik version can be verified by triggering the About box (Help->About)
when the net sample is run. The batik version shown should be
	"org.apache.batik v1.8pre"

The svg sample (org.jhotdraw.samples.svg) adds support for writing and reading drawings
in SVG format. It also depends on batik and nanoxml. In the case study, the svg module
is used to demonstrate the use of merge when dealing with common dependencies. It merges
its own org.apache.batik1_6 import to that of its JHotdraw import.

There are also two classes in the old decomposition (org.jhotdraw.samples.svg.Gradient, 
org.jhotdraw.samples.svg.SVGAttributeKeys) that led to a cyclic dependency between
JHotdraw core and svg. These have been declared to belong to JHotdraw core to remove
the cycle. This demonstrates how the module system allows for easier detection of possibly
bad cyclic dependencies between components.

The odg sample (org.jhotdraw.samples.odg) adds basic support for reading OpenDocument
ODG files. This sample depends on the svg sample, and in the case study the odg module
imports svg, merges its own JHotdraw instance with that of svg, while also updating
svg's batik dependency to org.apache.batik1_8pre.