#!/bin/bash

cd $WORKDIR

wget http://www.jboss.org/jbosstm/downloads/$NARAYANA_VERSION/binary/narayana-full-$NARAYANA_VERSION-bin.zip

unzip narayana-full-$NARAYANA_VERSION-bin.zip

cp narayana-full-$NARAYANA_VERSION/rts/lra/lra-coordinator-swarm.jar ./

rm -rf narayana-full-$NARAYANA_VERSION-bin.zip

rm -rf narayana-full-$NARAYANA_VERSION
 
