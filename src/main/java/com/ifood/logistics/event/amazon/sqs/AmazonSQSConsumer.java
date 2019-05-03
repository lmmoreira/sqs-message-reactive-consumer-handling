package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.lorem.logistics.event.Consumer;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.amazon.sqs.handler.AmazonSQSMessageHandler;
import com.lorem.logistics.event.metrics.EventMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Reactive executor of SQS consumers.
 */
public class AmazonSQSConsumer implements Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSQSConsumer.class);

    private static final String PARALLEL_PUBLISHER = "ParallelPublisher";

    private final Scheduler publisherScheduler;
    private final AmazonSQSConsumerHelper helper;
    private final EventMetrics eventMetrics;

    private Disposable publisherDisposable;

    /**
     * It constructs a SQS consumer executor based on its properties and other parameters.
     *
     * @param amazonSQS SQS Async client.
     * @param amazonSQSProperties Properties of SQS consumer.
     * @param amazonSQSMessageHandler SQS message handler.
     */
    public AmazonSQSConsumer(final AmazonSQS amazonSQS, final AmazonSQSProperties amazonSQSProperties,
            final AmazonSQSMessageHandler amazonSQSMessageHandler) {
        this.validate(amazonSQS, amazonSQSProperties, amazonSQSMessageHandler);
        this.eventMetrics = new EventMetrics(getEventMetricsName(amazonSQSProperties));

        final AmazonSQSProvider amazonSQSProvider = new AmazonSQSProvider(amazonSQS, amazonSQSProperties);
        final AmazonSQSSource amazonSQSSource = new AmazonSQSSource(amazonSQSProvider, amazonSQSProperties);
        this.helper = new AmazonSQSConsumerHelper(amazonSQSProperties, amazonSQSMessageHandler, amazonSQSProvider,
            amazonSQSSource, eventMetrics);

        this.publisherScheduler = Schedulers.newParallel(PARALLEL_PUBLISHER,
            Math.max(amazonSQSProperties.getNumberOfPublishers(), Runtime.getRuntime().availableProcessors()));

        if (amazonSQSProperties.getAutoStart()) {
            start();
        }
    }

    private void validate(final AmazonSQS amazonSQS, final AmazonSQSProperties amazonSQSProperties,
                          final AmazonSQSMessageHandler amazonSQSMessageHandler) {
        if (Objects.isNull(amazonSQS)) {
            throw new IllegalArgumentException("AmazonSQS cannot be null");
        }
        if (Objects.isNull(amazonSQSProperties)) {
            throw new IllegalArgumentException("AmazonSQSProperties cannot be null");
        }
        if (Objects.isNull(amazonSQSMessageHandler)) {
            throw new IllegalArgumentException("AmazonSQSMessageHandler cannot be null");
        }
        amazonSQSProperties.validate();
    }

    private String getEventMetricsName(final AmazonSQSProperties amazonSQSProperties) {
        return amazonSQSProperties.getQueueName() + "'s consumer";
    }

    @Override
    public void start() {
        LOGGER.debug("Starting to consume");
        this.publisherDisposable =
            helper.createConsumer(helper.createSources(this::isRunning, publisherScheduler)).subscribe();
    }

    @Override
    public void stop() {
        publisherDisposable.dispose();
        publisherScheduler.dispose();
    }

    @Override
    public EventMetrics getMetrics() {
        return eventMetrics;
    }

    @Override
    public boolean isRunning() {
        return !ofNullable(publisherDisposable).orElse(Disposables.never()).isDisposed();
    }

}
