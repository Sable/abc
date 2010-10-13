@setlocal

set TARGET=dist
if %1a==a goto doneArgs
set TARGET=%1
:doneArgs

%ANT_HOME%\bin\ant -buildfile build.xml %TARGET%

@endlocal
