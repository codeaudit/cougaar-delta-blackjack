@echo OFF

set MYDOMAINS=-Dorg.cougaar.domain.glm=org.cougaar.glm.ldm.GLMDomain
set MYCLASSES=org.cougaar.core.node.Node

set MYPROPERTIES=%MYDOMAINS% -Dorg.cougaar.install.path=%COUGAAR_INSTALL_PATH% -Ddelta.plugin.properties=%COUGAAR_INSTALL_PATH%\delta\data\SamplePluginProperties.ini 
set MYPROPERTIES=%MYPROPERTIES% -Dorg.cougaar.core.cluster.persistence.enable=false -Djdbc.drivers=oracle.jdbc.driver.OracleDriver

set MYMEMORY=-Xms100m -Xmx300m

