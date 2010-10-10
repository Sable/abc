/*
        REXX Start-/Stopskript to use with Jigsaw
        TKaemmerer@Integrata.de, December 1999
        usage: JigSaw START|STOP
        The JVM (aka java.exe and needed DLLs) must be in the PATH and LIBPATH!
*/
call RxFuncAdd 'SysLoadFuncs', 'RexxUtil', 'SysLoadFuncs'
call SysLoadFuncs
'@echo off'

parse SOURCE . type .

if type = "COMMAND" then
	parse upper arg Arg1 Arg2 Arg3
else
   parse upper arg Arg1, Arg2, Arg3

/*
        home, adm, pw and ctrl are local dependend!
*/
jig_home = 'H:\JAVA\Jigsaw'
jig_main = 'org.w3c.jigsaw.Main'
jig_kill = 'org.w3c.jigsaw.admin.JigKill'
jig_opt  = '-root ' || jig_home || '\Jigsaw'
jig_adm  = 'admin'
jig_pw   = 'admin'
jig_ctrl = 'http://os2baer:8009/'

/*
        The basis to use the JVM --- local dependend
*/
classPathBase = 'J:\JAVA11\LIB\CLASSES.ZIP;.;.\.;J:\JAVA11\LIB\SecMa.jar;'
/*
        I'm usind DB2 as Database --- local dependend
*/
classPathJDBC = 'J:\DATABASE\DB2\SQLLIB\JAVA\DB2JAVA.ZIP;'
/*
        The Servlet Container and JSP --- local dependend
*/
classPathSRVL = 'H:\JAVA\jswdk-1.0.1\lib\servlet.jar;H:\JAVA\gnujsp-1.0.0\lib\gnujsp10.jar;'
/*
        XML is fun --- local dependend
*/
classPathXML  = 'H:\JAVA\jswdk-1.0.1\lib\xml.jar;H:\JAVA\Cocoon\Cocoon_1_5.jar;'
/*
        Additional nice to have --- local dependend
*/
classPathOPT  = 'H:\JAVA\spfc-0.2.0\bin\spfc.jar;'
/*
        Your own Application Classes --- local dependend
*/
classPathAPPL = ''

/*
        Building the final Classpath
*/
classPath = jig_home || '\classes\jigsaw.zip;' || classPathBase || classPathJDBC || classPathSRVL || classPathXML || classPathOPT || ClassPathAPPL

select
   when Arg1 = "START" then
		'start "JigSaw Server" /min /c java.exe -classpath ' || classPath || ' ' || jig_main || ' ' || jig_opt
   when Arg1 ="STOP" then
      'start "Stopping JigSaw" /min /c java.exe -classpath ' || classPath || ' ' || jig_kill || ' -u ' jig_adm || ' -p ' || jig_pw || ' ' || jig_ctrl
end

exit
