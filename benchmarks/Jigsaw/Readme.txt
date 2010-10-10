Jigsaw release 2.2.5
-------------------------

Due to this original architecture, Jigsaw's configuration process is
not the one you are used to see in file-based servers, this is why
this README starts by this warning: 

    Read the documentation first !

Running the server:
-------------------

To read the documentation, you will have to first run Jigsaw, the
simplest way of doing so is the following:

0) Get a Java interpreter and install it. As of Jigsaw/1.0beta, Java
   version 1.1 is required.
   As of Jigsaw 2.1.0, jdk1.2 or higher is required (it has been tested
   with jdk up to jdk1.4.2). To use SSL, jdk 1.4.0 and higher are recommended.
   To get the best, try to upgrade to a 1.2 jdk, or at least, download
   the latest swing.
   You can get Sun's Java intepreter for free from:
     http://www.javasoft.com

1) try the install program
	go to the <instdir> (where README is)
	On windows:  go to scripts and click on install.bat 
	On unix: scripts/install.sh (do NOT go to scripts)
 
   if it fails, try to do it by hand:
        1) Set your CLASSPATH environment variable in order to include
           Jigsaw's classes. To do this:
           
             On Windows
               set CLASSPATH=<instdir>\Jigsaw\classes\jigsaw.jar;<instdir>\Jigsaw\classes\sax.jar;<instdir>\Jigsaw\classes\xp.jar;<instdir>\Jigsaw\classes\servlet.jar;.
             On UNIX
               export CLASSPATH=<instdir>/Jigsaw/classes/jigsaw.jar:<instdir>/Jigsaw/classes/sax.jar:<instdir>/Jigsaw/classes/xp.jar:<instdir>/Jigsaw/classes/servlet.jar

           Where <instdir> should be substitued with the absolute path of the
           directory in which you unpacked Jigsaw.
           NOTE: If you use jdk1.2, you may have to add the tools.jar provided 
           in the jdk1.2 distribution. It is required for pagecompilation.
        
        2) Check that your PATH setting allow you to run the Java
           interpreter. This will usually be the case if you have installed
           Sun's JDK. 

        3) Build the right property files
             Go to <instdir>/Jigsaw/Jigsaw (Windows: <instdir>\Jigsaw\Jigsaw)
             and execute:
               java Install
        
2) Run the server:

     If you have successfully setup the server using the install scripts, you may 
     use directly the start scripts provided:
     On Windows
       scripts\jigsaw.bat
     On Unix
       scripts/jigsaw.sh

     If you chose the manual way, after setting your CLASSPATH:
     On Windows
       java org.w3c.jigsaw.Main -root <instdir>\Jigsaw\Jigsaw
     On UNIX
       java org.w3c.jigsaw.Main -root <instdir>/Jigsaw/Jigsaw

3) Point your browser to the server's home page. At step 2), the
   server will have emited the full URL it is listening on, just point
   your browser to this URL and follow the instructions.

4) To administer the server, you can use the provided scripts (needs jdk1.2 or swing)
     On Windows:
	scripts\jigadmin.bat
     On Unix:
	scripts/jigadmin.sh
   Or see the documentation available on the server you just set up!
   Note that you must add jigadmin.jar in your CLASSPATH to start the admin
   interface without the scripts.

   NOTE: The default username/password for the admin server is admin/admin
         It is strongly recommended to change it as soon as possible!

Submiting bugs, giving feedback:
--------------------------------

A mailing list is available for Jigsaw related discussions:

    www-jigsaw@w3.org

To subscribe, send mail to www-jigsaw-request@w3.org, with subject "subscribe"

Enjoy !
Jigsaw Team <jigsaw@w3.org>

      
