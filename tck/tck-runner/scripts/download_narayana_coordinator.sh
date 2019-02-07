#!/bin/bash

BASEDIR=.
NARAYANA_VERSION=5.9.2.Final

if [ -f $BASEDIR/lra-coordinator-swarm.jar ]; then
    echo "Coordinator already downloaded at $BASEDIR/lra-coordinator-swarm.jar"
    exit 0
fi

cd $BASEDIR

wget http://www.jboss.org/jbosstm/downloads/$NARAYANA_VERSION/binary/narayana-full-$NARAYANA_VERSION-bin.zip

unzip narayana-full-$NARAYANA_VERSION-bin.zip

cp narayana-full-$NARAYANA_VERSION/rts/lra/lra-coordinator-swarm.jar ./

rm -rf narayana-full-$NARAYANA_VERSION-bin.zip

rm -rf narayana-full-$NARAYANA_VERSION


