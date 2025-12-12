package com.example.springbootagrolink.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(HttpServletRequest req, Exception ex, Model model) {
        logger.error("Unhandled exception for request [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        // Agregar un mensaje simple a la vista de error o redirigir al inicio
        model.addAttribute("errorMessage", "Ocurrió un error interno. Intenta nuevamente.");
        // Redirigir a la página de inicio para evitar la Whitelabel; puedes cambiar a una vista de error custom
        return "redirect:/";
    }
}

