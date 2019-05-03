package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.metrics.EventMetrics;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.time.Duration.ofSeconds;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AmazonSQSSourceUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSQSSourceUnitTest.class);

    private AmazonSQSSource amazonSQSSource;
    private AmazonSQSProvider amazonSQSProvider;
    private EventMetrics metrics;

    @Before
    public void setup() {
        amazonSQSProvider = mock(AmazonSQSProvider.class);
        metrics = mock(EventMetrics.class);

        final AmazonSQSProperties amazonSQSProperties = mock(AmazonSQSProperties.class);
        when(amazonSQSProperties.getRetries()).thenReturn(3);
        when(amazonSQSProperties.getRetryBackoffMillis()).thenReturn(8);
        amazonSQSSource = new AmazonSQSSource(amazonSQSProvider, amazonSQSProperties);
    }

    @Test
    public void testWaitTimeSecondsResponseInfiniteFlux() {
        final Message expectedMessage = new Message().withMessageId("1");
        final VirtualTimeScheduler vts = VirtualTimeScheduler.create();
        when(amazonSQSProvider.receive()).thenAnswer(i -> {
            vts.advanceTimeBy(ofSeconds(1L));
            return new ReceiveMessageResult().withMessages(expectedMessage);
        });
        final StepVerifier.Step<List<Message>> listStep = StepVerifier
                .withVirtualTime(
                    () -> amazonSQSSource.create(() -> true, metrics)
                            .doOnNext(messages -> LOGGER.info("Received {} message(s).", messages.size()))
                            .log(),
                    () -> vts,
                    Long.MAX_VALUE)
                .expectSubscription();
        IntStream.range(0, 100)
                .forEach(i -> listStep.thenAwait(ofSeconds(20L))
                        .expectNextCount(1L)
                        .expectNext(Collections.singletonList(expectedMessage)));
        listStep.thenCancel().verify(ofSeconds(2L));
    }


    @Test
    public void testFinishRepeatCondition() {
        doAnswer(invocationOnMock -> receivedMessage(ofSeconds(0))).when(amazonSQSProvider).receive();

        final AtomicInteger atomicInteger = new AtomicInteger(10);

        final Flux<List<Message>> sourceFlux =
            amazonSQSSource.create(() -> atomicInteger.decrementAndGet() > 0, metrics)
                    .doOnNext(messages -> LOGGER.info("Received {} message(s).", messages.size()));

        final StepVerifier.Step<List<Message>> listStep =
            StepVerifier.withVirtualTime(sourceFlux::log).expectSubscription();

        listStep.expectComplete().verify();
        assertEquals("", 0, atomicInteger.get());
    }

    private CompletableFuture error(Throwable throwable) {
        return Mono.error(throwable).toFuture();
    }

    private ReceiveMessageResult receivedMessage(final Duration delayUntilResponse, final Message... messages) {
        return Mono.delay(delayUntilResponse)
                .thenReturn(new ReceiveMessageResult().withMessages(messages))
                .block(ofSeconds(1));
    }

}
