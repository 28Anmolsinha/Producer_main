package com.backend.demo.controller;

import com.backend.demo.entity.Complaint;
import com.backend.demo.service.RabbitMQProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
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
}
