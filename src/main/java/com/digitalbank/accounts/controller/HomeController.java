
package com.digitalbank.accounts.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "Digital Banking Microservices",
                "service", "accounts",
                "status", "UP",
                "message", "Welcome to Digital Banking Accounts API"
        );
    }
}
