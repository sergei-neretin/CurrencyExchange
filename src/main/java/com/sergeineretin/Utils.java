package com.sergeineretin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
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
    public static <T> void write(HttpServletResponse resp, T value) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.println(new ObjectMapper().writeValueAsString(value));
    }
}
