package com.example.mutualssl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MyController {

    @Value("${backend.server}")
    private String backendServer;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/hello")
    public String sayHello(){

        ResponseEntity<String> response = restTemplate.getForEntity(backendServer, String.class);
        return response.getBody();
    }
}
