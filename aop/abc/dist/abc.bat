@echo off
setlocal

REM *****************************************************************
REM * abc - the AspectBench compiler                                *
REM * ==============================                                *
REM * Please modify the definition of ABC_HOME below to the         *
REM * actual installation path of abc.                              *
REM *****************************************************************
IF "%ABC_HOME%" == "" set ABC_HOME=e:\temp\abc-0.1.0

REM *** You shouldn't need to change anything below this line. ***
set CLASSPATH="%ABC_HOME%\lib\abc-complete.jar;%CLASSPATH%"

set ABC_JAVA_ARGS=-classpath %CLASSPATH% -Xmx256M -Dabc.home="%ABC_HOME%" abc.main.Main %*

REM By default, java should be in the path.
IF "%JAVA_HOME%" == "" java %ABC_JAVA_ARGS%

IF NOT "%JAVA_HOME%" == "" "%JAVA_HOME%\bin\java" %ABC_JAVA_ARGS%

endlocal
