echo RUN THIS FIRST - build.bat

@set LOCAL_SERVINGXML_HOME=..\..

@call %LOCAL_SERVINGXML_HOME%\servingxml -o output/books.html -r resources.xml books category=F

