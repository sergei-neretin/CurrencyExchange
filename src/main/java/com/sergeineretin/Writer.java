package com.sergeineretin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class Writer {
    ObjectMapper objectMapper;
    public Writer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public <T> void write(HttpServletResponse resp, T o) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.println(objectMapper.writeValueAsString(o));
    }
}
