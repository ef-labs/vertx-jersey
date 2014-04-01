#!/bin/sh

DIRNAME=$(dirname $0)
cd ${DIRNAME}
DIRNAME=${PWD}
pwd

# Set VERTX_MODS
#export VERTX_MODS=${DIRNAME}/mods

# Run init in each module to create vertx_classpath.txt and module.link files

cd ${DIRNAME}/vertx-mod-jersey
mvn vertx:init

cd ${DIRNAME}/vertx-mod-jerseymetrics
mvn vertx:init
