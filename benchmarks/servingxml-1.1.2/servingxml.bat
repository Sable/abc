@echo off

set ERROR_CODE=0

if not "%SERVINGXML_HOME%"=="" goto valServingXMLHome
set SERVINGXML_HOME=%~dp0
if not "%SERVINGXML_HOME%"=="" goto valServingXMLHome
echo.
echo ERROR: SERVINGXML_HOME not found in your environment.
echo Please set the SERVINGXML_HOME variable in your environment to match the
echo location of the ServingXML installation
echo.
goto error

:valServingXMLHome

rem echo %SERVINGXML_HOME%
rem echo  %SERVINGXML_HOME%\lib\..

set COMMAND_LINE_ARGS=%*

rem echo %COMMAND_LINE_ARGS%

set JAVAOPTS=-Denv.windir=%WINDIR%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%JAVACMD%" == "" set JAVACMD=%JAVA_HOME%\bin\java
:noJavaHome
if "%JAVACMD%" == "" set JAVACMD=java


set LOCAL_CLASSPATH=%SERVINGXML_HOME%servingxml.jar
for %%i in (%SERVINGXML_HOME%lib\*.jar) do call %SERVINGXML_HOME%scripts\lcp.bat %%i
set LOCAL_CLASSPATH=%LOCAL_CLASSPATH%;%SERVINGXML_HOME%classes
set LOCAL_CLASSPATH=%LOCAL_CLASSPATH%;%SERVINGXML_HOME%config
rem echo %LOCAL_CLASSPATH%

rem echo on                     

"%JAVACMD%" %JAVAOPTS% -cp "%LOCAL_CLASSPATH%" com.servingxml.app.consoleapp.ConsoleApp %COMMAND_LINE_ARGS%

goto end

:error
set ERROR_CODE=1
:end
exit /B %ERROR_CODE%

