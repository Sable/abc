JastAddModules is an implementation of the module type system
presented in the paper "Modules as Object-Oriented Types" submitted
for ICSE 2009. It is a module-aware Java compiler written in JastAdd.

The binary distribution of JastAddModules can be found in
	JastAddModules-bin.jar

An ant task is also provided as part of the distribution. The following shows
an example of the use of the jastaddmodules task:
	<taskdef name="jastaddmodules" classname="jastadd.JastAddModulesTask" classpath="JastAddModules-bin.jar"/>		

	<jastaddmodules outdir="${classdir}"
		verbose="false"
		jastaddframework="false"
		debug="false"
		instancemodule="org.jhotdraw.samples.draw"
		mainclass="org.jhotdraw.samples.draw::org.jhotdraw.samples.draw.Main"
		classpath="${libdir}/quaqua.jar:${libdir}/MRJAdapter.jar:${libdir}/java_30.zip:${libdir}/swing-layout.jar"
	>
		<fileset dir="${srcdir}">
			<include name="**/*.java"/>
			<include name="**/*.module"/>
			<include name="net/n3/nanoxml/*.java"/> 
		</fileset>
    </jastaddmodules>

Attributes:
	outdir - directory where the class files are placed.
	verbose - enables/disables verbose compiler output. Default is false.
	jastaddframework - enables/disables support for jastadd features. Should be false.
	debug - enables/disables module debug information. Default is false.
	instancemodule - selects the module to be used as the root instance. Only the
		dependencies of the instance module are instantiated. This is a required
		attribute if there are any module files in the compilation.
	mainclass - the module qualified name of the Main class. This is used to generate
		the "run" script. The run script itself will be placed in outdir. This attribute
		is optional.
	classpath - classpath to be used in the compilation. Both : and ; path separators are allowed.
	fileset - the set of files that are to be included in the compilation.

A small example of the use of the jastaddmodules task is also provided in

	examples/ICSE09PAPER_MODULE_WEAK_INTERFACES

The jastaddmodules ant task is tested with ant 1.6.5.

Limitations:

The current implementation requires all classes that belong to a
module to be in source, not a class file. 

No support for reflection is provided for classes inside a module. This will be supported
when the runtime component of the module system is implemented using classloaders.

	