package com.sergeineretin;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Utils {
    private Utils () {}
    public static String getDatabasePath() {
        URL resource = Utils.class.getClassLoader().getResource("CurrencyExchange.db");
        if (resource == null) {
            throw new IllegalArgumentException("Database file not found!");
        }
        try {
            URI uri = resource.toURI();
            Path path = Paths.get(uri);
            return path.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert URL to URI", e);
        }
    }

}
