#!/bin/bash

if [ $(nodetool -Dcom.sun.jndi.rmiURLParsing=legacy statusgossip) = "running" ]
then
	exit 0
fi
	exit 1
