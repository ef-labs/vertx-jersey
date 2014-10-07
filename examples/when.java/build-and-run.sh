#!/bin/sh
mvn -q clean install
mvn vertx:runMod -Dvertx.langs.java=com.englishtown~vertx-mod-hk2~1.7.0:com.englishtown.vertx.hk2.HK2VerticleFactory