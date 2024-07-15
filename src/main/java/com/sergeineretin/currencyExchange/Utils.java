package com.sergeineretin.currencyExchange;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Utils {
    private Utils() {
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        BufferedReader br = null;
        Map<String, String> dataMap = null;
        try {
            InputStreamReader reader = new InputStreamReader(request.getInputStream());
            br = new BufferedReader(reader);
            String data = br.readLine();
            dataMap = splitQuery(data);
            return dataMap;
        } catch (IOException ex) {
            log.error("Error reading request input stream: {}", ex.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    log.error("Error closing BufferedReader: {}", ex.getMessage());
                }
            }
        }
        return dataMap;
    }

    private static Map<String, String> splitQuery(String query) {
        Map<String, String> queryPairs = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = idx > 0 ? pair.substring(0, idx) : pair;
                    String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
                    queryPairs.put(key, value);
                } catch (Exception e) {
                    System.err.println("Error processing query pair: " + pair);
                }
            }
        }
        return queryPairs;
    }
}
