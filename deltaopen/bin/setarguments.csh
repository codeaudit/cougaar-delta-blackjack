#!/bin/csh -f
# should be sourced, not executed!

set MYDOMAINS="-Dorg.cougaar.domain.alp=org.cougaar.glm.ldm.GLMDomain"
set MYPROPERTIES="$MYDOMAINS -Dorg.cougaar.install.path=$COUGAAR_INSTALL_PATH -Duser.timezone=GMT"
set MYMEMORY=""

