#!/bin/csh -f
# should be sourced, not executed!
# set COUGAAR_INSTALL_PATH=/alpine/demo98

set _sed=`which sed`

if (! $?COUGAAR3RDPARTY) then
    setenv COUGAAR3RDPARTY /opt/alp-jars
endif

set p=( \
        $COUGAAR_INSTALL_PATH/lib/alp-core.jar \
        $COUGAAR_INSTALL_PATH/lib/alpine.jar \
        $COUGAAR_INSTALL_PATH/lib/contrib.jar \
        $COUGAAR_INSTALL_PATH/lib/xerces.jar \
        $COUGAAR_INSTALL_PATH/lib/jcchart400K.jar \
        $COUGAAR_INSTALL_PATH/lib/xygraf.jar \
        $COUGAAR3RDPARTY/fesi-111.jar \
        $COUGAAR3RDPARTY/jess44.jar \
        $COUGAAR3RDPARTY/classes111.zip \
        $COUGAAR3RDPARTY/jconn2.jar \
        $COUGAAR3RDPARTY/xerces.jar \
        $COUGAAR3RDPARTY/qslink.jar \
    )

#        $COUGAAR_INSTALL_PATH/plugins/alpicis.jar \
#        $COUGAAR_INSTALL_PATH/plugins/cgi.jar \
#        $COUGAAR_INSTALL_PATH/plugins/tops.jar \
#        $COUGAAR_INSTALL_PATH/plugins/tops_ga.jar \
#        $COUGAAR_INSTALL_PATH/plugins/delta.jar \
#        $COUGAAR_INSTALL_PATH/plugins/sra.jar \
#        $COUGAAR_INSTALL_PATH/plugins/jam.jar \
#        $COUGAAR_INSTALL_PATH/plugins/jamgp.jar \

if ($?COUGAAR_DEV_PATH) then
    set p = ( $COUGAAR_DEV_PATH $p)
endif

set LIBPATHS=`echo $p | $_sed 's/\ /:/g'`
unset p
unset _sed
