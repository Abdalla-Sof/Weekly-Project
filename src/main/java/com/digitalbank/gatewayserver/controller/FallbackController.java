
package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/loans")
    public Mono<ResponseEntity<Map<String, String>>> loansFallback() {

        Map<String, String> response = Map.of(
                "service", "loans",
                "status", "UNAVAILABLE",
                "message", "Loan services are temporarily unavailable. Please try again later."
        );

        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(response)
        );
    }
}
