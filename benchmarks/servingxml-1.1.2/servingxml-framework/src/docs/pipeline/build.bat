call xslt1.0 servingxml-1.xml ..\docbook\docbook-custom.xsl ..\..\..\..\docs\servingxml-1.html 
rem xslt1.0 flat-file-to-xml.xml ..\docbook\docbook-custom.xsl > ..\..\..\..\docs\flat-file-to-xml.html servingxml-2.xml ..\docbook\docbook-custom.xsl > ..\..\..\..\docs\servingxml-2.html 
call xslt1.0 record-structure.xml ..\docbook\docbook-custom.xsl ..\..\..\..\docs\record-structure.html 
call xslt1.0 flat-file-to-xml.xml ..\docbook\docbook-custom.xsl ..\..\..\..\docs\flat-file-to-xml.html 
call xslt1.0 xml-to-xml.xml ..\docbook\docbook-custom.xsl ..\..\..\..\docs\xml-to-xml.html 
call xslt1.0 servingxml-3.xml ..\docbook\docbook-custom.xsl ..\..\..\..\docs\servingxml-3.html 

