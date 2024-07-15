package com.sergeineretin.currencyExchange.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ServletUtils {
    public final static String CODE_REGEX = "^[A-Z]{3}$";
    public final static String CODES_REGEX = "^[A-Z]{6}$";

    private ServletUtils() {
    }

    public static boolean validateCode(String currencyCode) {
        if (!isStringValid(currencyCode)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile(CODE_REGEX);
            Matcher matcher = pattern.matcher(currencyCode);
            return matcher.find();
        }
    }

    public static boolean isStringValid(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isValidBigDecimal(String str) {
        if (str == null) {
            return false;
        }
        try {
            new BigDecimal(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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
