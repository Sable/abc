set SERVINGXML_HOME=..\..\..\servingxml
set CLASSPATH=%SERVINGXML_HOME%\servingxml.jar;%SERVINGXML_HOME%\classes
echo %CLASSPATH%

java -classpath %CLASSPATH% embed.SampleRecordReaderApp
