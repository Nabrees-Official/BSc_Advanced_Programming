package com.roomreservation.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // ✅ Allow login/logout pages without login
        if (path.endsWith("login.html") || path.endsWith("/login") || path.endsWith("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Check session
        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("username") != null);

        if (!loggedIn) {
            // redirect to login page
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        chain.doFilter(request, response);
    }
}