package com.backend.demo.controller;

import com.backend.demo.entity.Complaint;
import com.backend.demo.service.RabbitMQProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {

    private static final Logger logger = LogManager.getLogger(ComplaintController.class);
    private final RabbitMQProducer rabbitMQProducer;

    public ComplaintController(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping
    public ResponseEntity<String> createComplaint(@RequestBody Complaint complaint) {
        logger.info("Received complaint creation request: {}", complaint);

        // Send the complaint data to RabbitMQ
        rabbitMQProducer.sendComplaint(complaint);

        logger.info("Complaint submitted successfully!");
        return ResponseEntity.ok("Complaint submitted successfully!");
    }

    // Correctly handle preflight OPTIONS requests
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:5173");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.add("Access-Control-Allow-Credentials", "true");
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}