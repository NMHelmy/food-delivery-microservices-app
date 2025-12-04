package com.gamestore.testservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> showHeaders(HttpServletRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Collect headers
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }

        response.put("message", "Headers received successfully");
        response.put("headers", headers);

        // Info injected by gateway
        response.put("gateway.user-id", request.getHeader("X-User-Id"));
        response.put("gateway.user-role", request.getHeader("X-User-Role"));
        response.put("gateway.original-auth", request.getHeader(HttpHeaders.AUTHORIZATION));

        // Request details
        response.put("method", request.getMethod());
        response.put("request-uri", request.getRequestURI());
        response.put("remote-ip", request.getRemoteAddr());

        return ResponseEntity.ok(response);
    }
}
