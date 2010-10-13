@setlocal

set TARGET=dist
if %1a==a goto doneArgs
set TARGET=%1
:doneArgs

echo "JAVA_HOME is: " %JAVA_HOME%
echo "ANT_HOME is: " %ANT_HOME%

"%ANT_HOME%\bin\ant" -buildfile build.xml %TARGET%

@endlocal
