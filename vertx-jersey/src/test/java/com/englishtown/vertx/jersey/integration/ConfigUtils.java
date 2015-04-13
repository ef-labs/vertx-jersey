package com.englishtown.vertx.jersey.integration;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Config file utilities
 */
public class ConfigUtils {

    public static JsonObject loadConfig() {
        return loadConfig("/config.json");
    }

    public static JsonObject loadConfig(String name) {

        try (InputStream is = ConfigUtils.class.getResourceAsStream(name)) {
            try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
                return scanner.hasNext() ? new JsonObject(scanner.next()) : new JsonObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
