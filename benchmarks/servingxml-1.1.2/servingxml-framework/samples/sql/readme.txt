The batch file analysts2xml.cmd uses the resources script resources.xml to 
convert the results of a SQL query to an XML file in the ouput directory.  

The java run time needs to be able to find the database driver.  The 
sample in the consoleapp/sql directory is set up for the Oracle driver, 
oracle.jdbc.driver.OracleDriver.  To configure Presenting XML to recognize 
this driver, copy the Oracle classes12.jar file (not distributed) to the 
root-level lib/local directory.  

You can, of course, specify a different driver, making the appropriate 
changes in the sample resources script, copying the right jar file to 
lib/local.  

Once you have copied the jar file to lib/local, rebuild the Presenting XML.  

Then you can go back in the deploy tree and run

  analysts2xml
  employees2xml

