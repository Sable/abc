The batch file pulp2mail.cmd uses the resources script resources.xml to transform
an XML document to XSL-FO, format the result as PDF, and send the PDF document as
an attachment in a mail message.

This example requires the mail component.  In the build.properties file, verify that
the following line is present:

include.components.mail=true

Also verify that the extensions/mail/lib directory contains the mail jar files.


Once you have everything configured, rebuild the Presenting XML jar file
in the servingxml directory.

You will need to configure the mail sample resources.xml file with your mail
settings.    

Then run

  pulp2xml
  
to produce the email message, and also the file output/pulp.pdf. 

