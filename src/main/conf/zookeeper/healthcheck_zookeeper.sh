#!/bin/bash

if [ $(echo ruok | nc 127.0.0.1 2181) = "imok" ]
then
	exit 0
fi
	exit 1
