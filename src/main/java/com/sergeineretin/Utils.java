package com.sergeineretin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class Utils {
    public static String getDatabaseUrl() {
        return "jdbc:sqlite:D:/PROJECTS/untitled9/src/main/resources/currencyExchange.db";
    }
    public static <T> void write(HttpServletResponse resp, T value) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.println(new ObjectMapper().writeValueAsString(value));
    }
}
