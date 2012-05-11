Open Source Rule Module

Setup from the rule module (DELTAOpen) zip file.

1) Unzip to a directory of your choice. Don't pick a name with a space in it.
Whereever you see "DELTAInst" in the directions, substitute the name of 
your directory. 

2) Copy necessary jar files to DELTAInst/lib. You will need tomcat_33.jar,
webserver.jar, webtomcat.jar, servlet.jar, log4j.jar and xerces.jar (available from Cougaar). 
You will need core.jar, glm.jar, planserver.jar, toolkit.jar
from Cougaar. Additionally, you will need classes12.jar, jess50.jar,
and swingall.jar. 
for classes12.jar
http://otn.oracle.com/software/htdocs/distlic.html?/software/tech/java/sqlj_jdbc/htdocs/winsoft.html
for jess50.jar
http://herzberg.ca.sandia.gov/jess/
for swingall.jar
http://java.sun.com/products/jfc/download-103.html



Setting up the Oracle tables

1) Use DELTAInst/deltaopen/install/sql/qual_rule_tables.sql to create the
necessary rule tables. 

2) Use DELTAInst/deltaopen/install/sql/data/sample_accessor.ctl
and sample_operators.ctl with sqlloader to populate the rule tables with some
sample accessors and operators. 

3) Use DELTAInst/deltaopen/install/sql/data/sample_rules.sql
to populate the tables with two sample rules.



Setting up the web server

Specific instruction for using JRun 2.3.3. Other web servers may be used,
but setup will differ.

1) Copy the deltaopen.jar from the DELTAInst/lib directory into the
JRun/jsm-default/services/jws/htdocs/applets directory. Rename the
copy as openruleedit.jar.

2) Check that you have SampleRuleSearchForm.html,
SampleRuleEdit.html, SampleRuleCreate.html, and SampleButton.html in your
DELTAInst/deltaopen/data/servlet/templates directory.

3) Register the rule servlet as an alias in jse and jseweb. Start JRun administrator. 
Highlight jsm-default, and press configure. Highlight jse and press service config.
Go to the Aliases tab, add an entry. The name is OpenRuleEdit, the class
name is org.cougaar.delta.servlet.RuleEditServlet. Double click on the init
arguments and add two entries. The first entry name is org.cougaar.install.path and
the value is C:\DELTAInst. The second entry name is delta.servlet.properties and the 
value is C:\DELTAInst\deltaopen\data\servlet\SampleServletProperties.ini. 
Do the same thing for the service config of jseweb.

3a) The example shows tooltip messages as a demonstration of how help messages
can be added to the UI. If you were implementing these messages, you would register
another servlet as an alias, the ExplanationServlet. The procedure would be
the same as above, except for the name and class of the servlet.

4) Add the necessary jar files to the java class path of your web server.
Start JRun administrator, choose jsm-default configure, go to the general tab, 
go to the java tab, add C:\DELTAInst\lib\deltaopen.jar;C:\DELTAInst\lib\classes12.zip;C:\DELTAInst\lib\core.jar;
C:\DELTAInst\lib\glm.jar;C:\DELTAInst\lib\xerces.jar;C:\DELTAInst\lib\xmlparserv2.jar 
to the java class path.

5) Add the necessary javascript and cascading style sheet.
Copy DELTAInst\deltaopen\data\servlet\htdocs\data_validation.js to the
JRun\jsm-default\services\jws\htdocs\javascript directory. 
Copy DELTAInst\deltaopen\data\servlet\htdocs\nny.css to the
JRun\jsm-default\services\jws\htdocs\css directory.



Using the rule editor

1) Start JRun, view http://localhost/servlet/OpenRuleEdit.

2) For more instructions, see the user guide section on the rule editor.



Running the sample society

1) Modify properties file with your jdbc connection information.
In DELTAInst/deltaopen/data/SamplePluginProperties.ini and 
DELTAInst/deltaopen/data/servlet/SampleServletProperties.ini fill in your jdbc connect
string. Example:
jdbc.connect.String=jdbc:oracle:thing:@<host>:<port>:<SID>,<username>,<password>
For SamplePluginProperties, also set the reportpersist.oracle.ConnectionString
to the same string.

2) Go to DELTAInst/configs/delta_config

3) Make sure you have Sample.ini, SampleCluster.ini, SampleCluster-prototype-ini.dat in
your DELTAInst/configs/delta_config directory.

4) Set COUGAAR_INSTALL_PATH=C:\DELTAInst in your environment.

5) Execute the command ../../bin/Node Sample.
In less than a minute you should see some qualifications printing to the screen.



Viewing the sample results

1) The society must be running. 

2) View http://localhost:8800/$SampleCluster/viewqualified from any web browser.

