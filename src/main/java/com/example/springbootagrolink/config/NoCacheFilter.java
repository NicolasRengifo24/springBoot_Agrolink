package com.example.springbootagrolink.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Aplicar headers de no-cache solo a las rutas protegidas
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/cliente/") ||
            requestURI.startsWith("/admin/") ||
            requestURI.startsWith("/transportista/") ||
            requestURI.startsWith("/productos/") ||
            requestURI.startsWith("/servicio/")) {

            // Headers para prevenir caché en navegador
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }

        chain.doFilter(request, response);
    }
}

