package com.backend.demo.service;

import com.backend.demo.config.RabbitMQConfig;
import com.backend.demo.entity.Complaint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {
    private static final Logger logger = LogManager.getLogger(RabbitMQProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendComplaint(Complaint complaint) {
        String routingKey = switch (complaint.getIssueType()) {
            case "infrastructure" -> RabbitMQConfig.INFRASTRUCTURE_ROUTING_KEY;
            case "electricity" -> RabbitMQConfig.ELECTRICITY_ROUTING_KEY;
            case "legal" -> RabbitMQConfig.LEGAL_ROUTING_KEY;
            default -> throw new IllegalArgumentException("Unknown issue type: " + complaint.getIssueType());
        };

        logger.info("Sending complaint to RabbitMQ with routing key {}: {}", routingKey, complaint);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, complaint);
    }
}
