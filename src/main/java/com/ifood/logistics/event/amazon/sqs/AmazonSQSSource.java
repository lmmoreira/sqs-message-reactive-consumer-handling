package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.metrics.EventMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.function.BooleanSupplier;

import static java.time.Duration.ofMillis;

class AmazonSQSSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSQSSource.class);
    private static final String RECEIVE_MESSAGE_CALL_METRIC = "SQS receive message API call";
    private static final String RECEIVED_MESSAGES_METRIC = "SQS received messages";

    private final AmazonSQSProvider amazonSQSProvider;
    private final AmazonSQSProperties amazonSQSProperties;

    AmazonSQSSource(final AmazonSQSProvider amazonSQSProvider, final AmazonSQSProperties amazonSQSProperties) {
        this.amazonSQSProvider = amazonSQSProvider;
        this.amazonSQSProperties = amazonSQSProperties;
    }

    /**
     * It creates a new message source that will repeatedly emit messages if the predicate returns true
     * after completion of the previous subscription. <br>
     * A retry is applied using exponential backoff as configured on consumer properties.
     *
     * @param repeatCondition Predicate condition to repeat source emission.
     * @param eventMetrics Event metrics.
     * @return Continuous until predicate message source.
     */
    Flux<List<Message>> create(final BooleanSupplier repeatCondition, final EventMetrics eventMetrics) {
        LOGGER.debug("Creating a Publisher for {}", amazonSQSProperties.getQueueName());
        return Flux.<List<Message>>create(sink -> emitter(sink, eventMetrics))
                .retryBackoff(amazonSQSProperties.getRetries(), ofMillis(amazonSQSProperties.getRetryBackoffMillis()))
                .filter(messages -> !messages.isEmpty())
                .repeat(repeatCondition);
    }

    private void emitter(final FluxSink<List<Message>> sink, final EventMetrics eventMetrics) {
        try {
            final ReceiveMessageResult receiveMessageResult = amazonSQSProvider.receive();
            eventMetrics.incrementMetric(RECEIVE_MESSAGE_CALL_METRIC);
            LOGGER.debug("Received {} message(s) from {}.",
                receiveMessageResult.getMessages().size(),
                amazonSQSProperties.getQueueName());
            eventMetrics.addToMetric(RECEIVED_MESSAGES_METRIC, receiveMessageResult.getMessages().size());
            sink.next(receiveMessageResult.getMessages());
            sink.complete();
        } catch (Exception e) {
            LOGGER.warn("Error while request messages", e);
            sink.error(e);
        }
    }

}
