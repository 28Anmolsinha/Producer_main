package com.backend.demo.config;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class LoggingOtlpSpanExporter implements SpanExporter {

    private static final Logger logger = LogManager.getLogger(LoggingOtlpSpanExporter.class);
    private final SpanExporter delegate;
    private final String collectorAddress;

    public LoggingOtlpSpanExporter(SpanExporter delegate, String collectorAddress) {
        this.delegate = delegate;
        this.collectorAddress = collectorAddress;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            logger.info("Trying to Exporting span '{}' (TraceId: {}) to collector at {}",
                    span.getName(),
                    span.getTraceId(),
                    collectorAddress
            );
        }

        CompletableResultCode result = delegate.export(spans);

        result.whenComplete(() -> {
            if (result.isSuccess()) {
                logger.info("Span export successful to collector at {}", collectorAddress);
            } else {
                logger.error("Span export failed to collector at {}", collectorAddress);
            }
        });

        return result;
    }


    @Override
    public CompletableResultCode flush() {
        return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return delegate.shutdown();
    }
}
