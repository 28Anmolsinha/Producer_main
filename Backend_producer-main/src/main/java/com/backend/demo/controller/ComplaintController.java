package com.backend.demo.controller;

import com.backend.demo.entity.Complaint;
import com.backend.demo.service.RabbitMQProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private static final Logger logger = LogManager.getLogger(ComplaintController.class);
    private final RabbitMQProducer rabbitMQProducer;
    private final Tracer tracer;

    public ComplaintController(RabbitMQProducer rabbitMQProducer, Tracer tracer) {
        this.rabbitMQProducer = rabbitMQProducer;
        this.tracer = tracer;
    }

    @PostMapping
    public ResponseEntity<String> createComplaint(@RequestBody Complaint complaint) {
        Span span = tracer.spanBuilder("Backend1 - Create Complaint").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if (complaint.getId() != null) {
                span.setAttribute("user.id", complaint.getId());
            }

            logger.info("Received complaint creation request: {}", complaint);
            rabbitMQProducer.sendComplaint(complaint);
            logger.info("Complaint submitted successfully!");
            return ResponseEntity.ok("Complaint submitted successfully!");
        } catch (Exception e) {
            // Record the exception in the trace and mark it as error
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error while submitting complaint");
            span.setAttribute("error.message", e.getMessage());

            logger.error("Error occurred while processing complaint", e);
            return ResponseEntity.status(500).body("Failed to submit complaint: " + e.getMessage());
        } finally {
            span.end();
        }
    }
}
