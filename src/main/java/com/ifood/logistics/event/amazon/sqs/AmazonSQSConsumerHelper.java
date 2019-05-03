package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.Message;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.amazon.sqs.handler.AmazonSQSMessageHandler;
import com.lorem.logistics.event.amazon.sqs.handler.MessageDecorator;
import com.lorem.logistics.event.metrics.EventMetrics;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class AmazonSQSConsumerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSQSConsumerHelper.class);
    private static final String DELETED_MESSAGES_METRIC = "SQS deleted messages";

    private final AmazonSQSProperties amazonSQSProperties;
    private final AmazonSQSMessageHandler amazonSQSMessageHandler;
    private final AmazonSQSProvider amazonSQSProvider;
    private final AmazonSQSSource amazonSQSSource;
    private final EventMetrics eventMetrics;

    AmazonSQSConsumerHelper(final AmazonSQSProperties amazonSQSProperties,
            final AmazonSQSMessageHandler amazonSQSMessageHandler, final AmazonSQSProvider amazonSQSProvider,
            final AmazonSQSSource amazonSQSSource, final EventMetrics eventMetrics) {
        this.amazonSQSProperties = amazonSQSProperties;
        this.amazonSQSMessageHandler = amazonSQSMessageHandler;
        this.amazonSQSProvider = amazonSQSProvider;
        this.amazonSQSSource = amazonSQSSource;
        this.eventMetrics = eventMetrics;
    }

    Flux<DeleteMessageBatchResult> createConsumer(final ParallelFlux<List<Message>> sources) {
        return sources.flatMap(amazonSQSMessageHandler::handle)
                .map(this::filterAndMapMessageDecorators)
                .filter(m -> !m.isEmpty())
                .flatMap(messages -> Flux.just(amazonSQSProvider.delete(messages)))
                .sequential()
                .doOnNext(deleteMessageBatchResult -> {
                    eventMetrics.addToMetric(DELETED_MESSAGES_METRIC, deleteMessageBatchResult.getSuccessful().size());
                    LOGGER.debug("{} messages was deleted in batch after processing.",
                        deleteMessageBatchResult.getSuccessful().size());
                })
                .onErrorContinue((throwable, o) -> {
                    NewRelic.noticeError(throwable);
                    LOGGER.error("An unexpected error occurred while deleting messages in batch.", throwable);
                });
    }

    private List<Message> filterAndMapMessageDecorators(final List<MessageDecorator> messageDecorators) {
        return messageDecorators.stream()
                .filter(MessageDecorator::isSuccessfullyProcessed)
                .map(MessageDecorator::getMessage)
                .collect(Collectors.toList());
    }

    ParallelFlux<List<Message>> createSources(final BooleanSupplier repeat, final Scheduler publisherScheduler) {
        return Flux.range(0, amazonSQSProperties.getNumberOfPublishers())
                .flatMap(i -> amazonSQSSource.create(repeat, eventMetrics).subscribeOn(publisherScheduler))
                .parallel(amazonSQSProperties.getNumberOfPublishers())
                .runOn(Schedulers.elastic());
    }
}
