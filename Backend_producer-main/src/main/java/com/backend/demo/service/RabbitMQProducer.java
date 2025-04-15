package com.backend.demo.service;

import com.backend.demo.config.RabbitMQConfig;
import com.backend.demo.entity.Complaint;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {
    private static final Logger logger = LogManager.getLogger(RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final Tracer tracer;
    private final TextMapPropagator propagator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RabbitMQProducer(RabbitTemplate rabbitTemplate, Tracer tracer, OpenTelemetry openTelemetry) {
        this.rabbitTemplate = rabbitTemplate;
        this.tracer = tracer;
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendComplaint(Complaint complaint) {

        Span span = tracer.spanBuilder("RabbitMQ").startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Add user attributes to span
            span.setAttribute("user.id", complaint.getId());
            span.setAttribute("complaint.issueType", complaint.getIssueType());


            String routingKey = switch (complaint.getIssueType()) {
                case "infrastructure" -> RabbitMQConfig.INFRASTRUCTURE_ROUTING_KEY;
                case "electricity" -> RabbitMQConfig.ELECTRICITY_ROUTING_KEY;
                case "legal" -> RabbitMQConfig.LEGAL_ROUTING_KEY;
                default -> throw new IllegalArgumentException("Unknown issue type: " + complaint.getIssueType());
            };

            // Serialize complaint to JSON
            byte[] jsonBody = objectMapper.writeValueAsBytes(complaint);

            // Prepare message properties
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setHeader("user-id", complaint.getId());

            // Inject trace context into headers
            TextMapSetter<MessageProperties> setter = (carrier, key, value) -> carrier.setHeader(key, value);
            propagator.inject(Context.current(), properties, setter);

            // Log the trace headers and trace ID
            logger.info("Injecting trace headers: {}", properties.getHeaders());
            logger.info("TraceId (Backend1): {}", span.getSpanContext().getTraceId());

            // Send the message to RabbitMQ
            Message message = new Message(jsonBody, properties);
            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
        } catch (Exception e) {
            // Mark the span as error and record exception details
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to send to RabbitMQ");
            span.setAttribute("error.message", e.getMessage());
            logger.error("Error sending complaint to RabbitMQ", e);
        } finally {
            span.end(); // End span in all cases
        }
    }
}
