
package com.digitalbank.loans.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoansController {

    @Value("${banking.loans.demo-message:CONFIG_NOT_LOADED}")
    private String configMessage;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "loans",
                "status", "UP"
        );
    }

    @GetMapping("/config-test")
    public String configTest() {
        return configMessage;
    }
}
