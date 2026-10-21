
package DigitalBankin.accounts.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "accounts",
                "status", "UP",
                "message", "Digital Banking Accounts Service is running"
        );
    }
}
