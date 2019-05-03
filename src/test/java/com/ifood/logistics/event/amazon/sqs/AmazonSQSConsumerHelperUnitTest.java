package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResultEntry;
import com.amazonaws.services.sqs.model.Message;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.amazon.sqs.handler.AmazonSQSMessageHandler;
import com.lorem.logistics.event.amazon.sqs.handler.MessageDecorator;
import com.lorem.logistics.event.metrics.EventMetrics;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AmazonSQSConsumerHelperUnitTest {

    private AmazonSQSProperties properties;
    private AmazonSQSMessageHandler handler;
    private AmazonSQSProvider provider;
    private AmazonSQSSource source;
    private AmazonSQSConsumerHelper helper;
    private EventMetrics metrics;

    @Before
    public void before() {
        properties = mock(AmazonSQSProperties.class);
        handler = mock(AmazonSQSMessageHandler.class);
        provider = mock(AmazonSQSProvider.class);
        source = mock(AmazonSQSSource.class);
        metrics = mock(EventMetrics.class);

        helper = new AmazonSQSConsumerHelper(properties, handler, provider, source, metrics);
    }

    @Test
    public void shouldCreateSources() {
        int numberOfPublishes = 4;
        when(properties.getNumberOfPublishers()).thenReturn(numberOfPublishes);

        List<Message> receivedMessages = Collections.singletonList(new Message().withMessageId("fake message"));
        when(source.create(any(), any(EventMetrics.class))).thenReturn(Flux.just(receivedMessages));

        Scheduler publisherScheduler = Schedulers.newParallel("anyName", numberOfPublishes);
        BooleanSupplier repeat = () -> true;
        StepVerifier.create(helper.createSources(repeat, publisherScheduler).log())
                .expectSubscription()
                .as("subscription")
                .expectNextCount(numberOfPublishes * receivedMessages.size())
                .as("total of messages emitted")
                .verifyComplete();
    }

    @Test
    public void shouldCreateConsumer() {
        var fakeMessage1 = MessageDecorator.withSuccess(new Message().withMessageId("fake message1"));
        var fakeMessage2 = MessageDecorator.withSuccess(new Message().withMessageId("fake message2"));
        var receivedMessages = Arrays.asList(fakeMessage1.getMessage(), fakeMessage2.getMessage());
        var processedMessages = Arrays.asList(fakeMessage1, fakeMessage2);
        var sources = Flux.just(receivedMessages).parallel(1);
        var processedMessagesFlux = Flux.just(processedMessages).parallel(1);

        when(handler.handle(any())).thenReturn(processedMessagesFlux.sequential());
        final DeleteMessageBatchResult deleteMessageBatchResult = mock(DeleteMessageBatchResult.class);
        when(deleteMessageBatchResult.getSuccessful())
                .thenReturn(Arrays.asList(new DeleteMessageBatchResultEntry(), new DeleteMessageBatchResultEntry()));
        when(provider.delete(anyList())).thenReturn(deleteMessageBatchResult);


        StepVerifier.create(helper.createConsumer(sources).log())
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

        verify(handler, times(1)).handle(any());
        verify(provider, times(1)).delete(anyList());
        verify(metrics, times(1)).addToMetric(anyString(), anyLong());
    }

}
