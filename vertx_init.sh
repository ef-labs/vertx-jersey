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

cd ${DIRNAME}/examples/chunked
mvn vertx:init

cd ${DIRNAME}/examples/filter
mvn vertx:init

cd ${DIRNAME}/examples/inject-securitycontext
mvn vertx:init

cd ${DIRNAME}/examples/inject-vertxrequesthandler
mvn vertx:init

cd ${DIRNAME}/examples/jersey-inject-vertx
mvn vertx:init

cd ${DIRNAME}/examples/vertx-jersey-jackson
mvn vertx:init
