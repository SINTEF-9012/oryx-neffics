Setting up development environment
=======================

This file explains how to setup the development environment for the Oryx editor.


Install the following (if not done already):
--------------------------------

#### Summary:



Tool                                                        |Version|Development|Deployment (server) | Client (user)
------------------------------------------------------------- | :---: | :-------: | :---------------: | :-----------:
[Firefox](http://www.mozilla.org)                             |       | YES   |      | YES
[Firebug addon](https://addons.mozilla.org/firefox/addon/firebug/)| | good to have (debugging)
[Java JDK](http://java.sun.com/javase/downloads/index.jsp)_*_ | >= 6  | YES   | YES (JRE only)
[Git hub](http://www.github.com)_*_ (account & programs)      |       | YES
[SmartGit](http://www.syntevo.com/smartgit/index.html)_*_     |       | YES
[eclipse](http://www.eclipse.org/downloads/)                  |       | YES
[tomcat](http://www.eclipse.org/downloads/)_*_                |  6    | YES   | YES
[python](http://www.python.org)                            | see below| YES   | YES
[PostGreSQL](http://www.enterprisedb.com/products-services-training/pgdownload) _*_|see below|YES|YES

 _*_ means some instructions are provided here. Otherwise installation is rather straightforward and uses default settings.


_Remarks_:

* The browser need to support SVG, currenlty only Firefox does that.
* You can use any other version control tool with github. SmartGit is nice though.
* PostGreSQL and Python:
	* on Linux, you can use version 9.0 (and above??) and python 2.6 (unsure for 2.7).
	* on Windows, use only one of the following[^1]:
		* PostGreSQL 8.4 and [Python 2.6.x](http://www.python.org/download/releases/2.6.6/)
		 (*32 bit version only*, even on Windows 64)
		* PostGreSQL 8.3 and [Python 2.5.x](http://www.python.org/download/releases/2.5.4/)
		(*32 bit version only*, even on Windows 64)

[^1]: The reason for this is because on Windows, PostGreSQL 9.x is linked to Python 3.x (people packaging for windows have different opinions than the ones packaging for Linux); PostGreSQL 8.4 and 8.3 are linked to Python 2.6 and 2.5 (respectively). and as they exist only on 32 bit versions the Python libraries must also be 32 bit versions. If you want other versions of those software the only solutions is to (1) recompile PostGreSQL yourself, enabling the desired PL/Python, or (2) move Oryx to Python 3.0 (or another language).



#### Java

* Ensure that ``JAVA_HOME`` Enviroment variable is set to java installation dir: ``C:\Program Files\Java\jdk1.7.0_07`` for instance)
	* on Windows: right click on *Computer*, then *Properties*, *Advanced system settings*, *Environment Variables*


#### GitHub

* Create a Github user on [GitHub](http://www.github.com)
* [Set up Git](https://help.github.com/articles/set-up-git):
	* Perform the step *Download and install Git* from the webpage
 	* Launch ``Git Bash`` (in windows menu *Start -> All Programs -> Git*)to have a usable shell
 	* Continue setting up Git as explained on the webpage.



#### SmartGit

* During installation, choose *Non-commercial use only*
* You might have to enter your GitHub credentials.



#### Tomcat

* From the download page, it is easier to use the *Windows Service Installer* (wizard installer)
	* it is better to provide and remember the *Tomcat Administrator login*. However for development it seems you can do without.
* Launch `` Monitor Tomcat ``, <u>running as administrator</u>. It will then appear as an icon in the task bar (possibly hidden).
	* Right click on the tomcat icon to start / stop the service.
	* Right click on the tomcat icon and choose *Configure…*
		* go to "General" and under "Startup type" and choose if you want tomcat to be started at login or not
		* If you choose to have tomcat to run at login, go to *C:/Program Files/Apache Software Foundation/Tomcat 6.0/bin*, select *Tomcat6*, right click, *Properties*, tab *Compatbility*, check *Run as administrator*. Do the same for *Tomcat6w*. 
* On the development machine, check you have permissions to *C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps*:
	* Right click on the folder, *Properties*, tab *Security*, click *Edit* and add *Full Control* for yourself.
* Test if Tomcat has been correctly installed by visiting <http://localhost:8080>
* (windows only) Check also that Tomcat writes a log in *C:/Program Files/Apache Software Foundation/Tomcat 6.0/logs*, file *catalina.out*
	* If not, change the *bin/startup.bat* file and replace the line
	
			call "%EXECUTABLE%" start %CMD_LINE_ARGS% 

		by:

			call "%EXECUTABLE%" run %CMD_LINE_ARGS% 1> ../logs/catalina.out 2>1







#### PostGreSQL

* Remember the password of postgres, you'll need it later (usually it is *postgres*)
* It is no useful to launch *Stack Builder* at then end of the installation.






Set up development environment for Oryx
----------------------


#### Download ('pull') the code in a local repository

* Open SmartGit; Close the window "Welcome to SmartGit" in case it appeared.
* In the menu, choose *Project -> Open Repository*. Then:
	* Choose a folder where the sources will be downloaded, for instance ``.../workspace/oryx-neffics``, click *Next*
	* Choose *Git* as the type of repository, then click *Finish*.
* In the menu, choose *Remote -> Manage remotes*. Then:
	* Click *Add*
	* Fill the following: _Name_:``oryx-neffic``, _URL_: ``https://github.com/SINTEF-9012/oryx-neffics.git``
		* you can choose to access the repository with SSH as well (``git@github.com:SINTEF-9012/oryx-neffics.git``). You'll need to set up private/public keys: <https://help.github.com/articles/generating-ssh-keys>.
	* Close the window.
* Click on *Pull*, then *OK*, and wait that SmartGit downloads the code.
* Click on *Switch* and choose the ``oryx-neffics/neffics`` branch (use default settings in the next windows).





#### Setup: variables

* In eclipse, import the sources (i.e. import a General Project)
* Choose the Java perspective (better)
* In the file ``oryx-neffics/build.properties``, change the lines as needed (don't put trailing spaces! Examples are for tomcat 6 and PostGreSQL 8.4):

	- deploymentdir = ``C:/Program Files/Apache Software Foundation/Tomcat 6.0/webapps``
	- postgresql-username = ``poem``	
	- postgresql-bin-dir = ``C:/Program Files (x86)/PostgreSQL/8.4/bin``
	
	
* In the file ``oryx-neffics/poem-jvm/etc/hibernate.cfg.xml,`` change the line (if you have a different password to access the database, notably on the server):

	- 	< property name="connection.password" >``poem`` < /property >


#### Setup: database (development AND deployment site)

* Start a shell[^2]
* change to the folder containing the binaries of PostGreSQL
	* on Windows, it should be *C:/Program Files (x86)/PostgreSQL/8.4/bin*
* Run the following commands:
	* ``createuser -U postgres -e -P -E -S -d -r poem`` (see *createuser --help* for corresponding long options)
		* enter the password for *poem* twice (the one that is specified in *oryx-neffics/poem-jvm/etc/hibernate.cfg.xml*)
		* enter the password for user *postgres* (windows only(?))
	* ``createdb -U postgres -e -E utf8 -O poem poem``
		* enter the password for user *postgres*
	* ``psql -U postgres poem <  C:/Users/.…../workspace_folder/oryx-neffics/poem-jvm/data/database/db_schema.sql``
		* enter the password for user *postgres*
		* verify that no error occured (possible errors might be complains about PL/python because of missing or incompatible libraries)
			* it might complain that *language "plpgsql" already exists*, but this is fine.
		* It might be that the terminal has not enough space to display the whoel output. In that case redirect the output to a file. For instance:å
			* createdb -U postgres -e -E utf8 -O poem poem1> log.txt 2>1

[^2]: on Linux, you might have to start a shell as user *postgres*. On windows it's also possible to do that but it's then cumbersome to access the schema file (default permissions don't allow it)


### Everyday tasks: compile, debug, deploy, commit


#### Compile

* Right click on the file ``oryx-neffics/build.xml``, *Run As -> External Tool Configurations*
* Click on the *Targets* tab
* choose the following, in that order (or reorder afterwards):

	Development                       | Deployment
	--------------------------------- | ----------
	undeploy-all                      | clean-all
	clean-all                         | build-all
	build-all                         |
	build-with-script-files-flag      |
	build-with-xhtml-test-files-flag  |
	deploy-all                        |

* The WAR file are created in ``oryx-neffics/war``: ``backend.war`` and ``oryx.war``. On the development machine, they are also deployed in Tomcat




#### Debug

* On the development machine, start Firefox and access the tool at <http://localhost:8080/backend/poem/repository>
	* You may open Firebug before accessing Oryx: *Tools -> Web Developer -> Firebug -> Open Firebug*
* You need an OpenID to login (upper right). Create one for instance at <http://www.myid.net>
* See also <http://code.google.com/p/oryx-editor/wiki/SetupDevelopmentEnvironment#Enabling_Debugging> to debug in tomcat with eclipse.


##### Hints for debugging:
* If you made some changes on the client side (javascript, stencil set,…) you may need to clear the cache. 
* Uncomment line 6 in build.properties in order to have line numbers when printing stacktraces:
	* java-debug = on
* In *editor/client/scripts/oryx.js*, change line 39 to enable debug log:
	* var ORYX_LOGLEVEL = ``4``;
	* __Be careful when committing and deploying: change back to ``1``__
* If the javasript is compressed in Firebug, open *editor/server/src/org/oryxeditor/server/EditorHandler.java* and change line 184:
	* " < script src=\"" + oryx_path + "profiles/oryx.core.``uncompressed``.js\" type=\"text/javascript\" />"
	* __Be careful when committing and deploying: change back to ``oryx.core.js``__






#### Deploy

* Login to the tomcat manager
* Deploy the files ``backend.war`` and ``oryx.war`` (found in *oryx-neffics/war*)
* You need an OpenID to login (upper right). Create one for instance at <http://www.myid.net> 



#### Commit

* Always check the differences with GitHub and provide the appropriate commit message.
* Be careful when committing the following files:

	File                                            | Sensible data
	----------------------------------------------- | --------
	oryx-neffics/poem-jvm/etc/``hibernate.cfg.xml`` | password of SQL database
	oryx-neffics/``build.properties``               | username of SQL database; folders where 	Tomcat and PostGreSQL are installed

	If necessary, remove the sensible data (with dummy data) and then do the commit.



##### Most usefull commands with SmartGit:

* __Pull__: fetch recent changes on github. 
* __Commit__: commit your current changes to your local repository. Github is not informed.
* __Push__: push the new commits from your local repository to GitHub. You may check on github that your changes are there.





Misc
=====

* Some people on github have moved Oryx to Maven: <https://github.com/yuanqixun/oryx-editor> maven and tomcat 7.0.14
* Using old versions of PostGreSQL and Python on windows is not future-proof. We should either move to another language supported in PostGreSQL 9.x on Windows, or move to Python 3 (both in Oryx and in the server)
* <http://code.google.com/p/oryx-editor/wiki/SetupDevelopmentEnvironment> is the original document for setting up Oryx.







