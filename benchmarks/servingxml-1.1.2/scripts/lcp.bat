@set CLASSPATH_COMPONENT=%1
if ""%1""=="""" goto gotAllArgs
shift

:argCheck
if ""%1""=="""" goto gotAllArgs
@set CLASSPATH_COMPONENT=%CLASSPATH_COMPONENT% %1
shift
goto argCheck

:gotAllArgs
@set LOCAL_CLASSPATH=%LOCAL_CLASSPATH%;%CLASSPATH_COMPONENT%
@set CLASSPATH_COMPONENT=

