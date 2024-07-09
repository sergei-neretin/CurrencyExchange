package com.sergeineretin.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        httpResp.setContentType("text/json");
        httpResp.setCharacterEncoding("UTF-8");

        filterChain.doFilter(req, resp);
    }
}
