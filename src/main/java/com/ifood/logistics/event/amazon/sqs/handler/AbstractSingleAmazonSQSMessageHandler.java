package com.lorem.logistics.event.amazon.sqs.handler;

import com.amazonaws.services.sqs.model.Message;
import com.lorem.event.logs.TransactionLogHelper;
import com.newrelic.api.agent.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * This class represents cases where should handle a message per time.
 */
public abstract class AbstractSingleAmazonSQSMessageHandler implements AmazonSQSMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSingleAmazonSQSMessageHandler.class);

    /**
     * Set the right transaction name before handle the message by calling
     * AbstractSingleAmazonSQSMessageHandler::handleMessage.
     *
     * @param messages A list of {@link Message} to be handle
     * @return a Flux of list of {@link MessageDecorator}
     */
    @Trace(dispatcher = true)
    @Override
    public Flux<List<MessageDecorator>> handle(final List<Message> messages) {
        TransactionLogHelper.overrideTransactionName(getTransactionName());

        return Flux.fromIterable(messages).flatMap(this::createMessageDecorator).buffer();
    }

    private Mono<MessageDecorator> createMessageDecorator(final Message message) {
        return handleMessage(message)
                .thenReturn(MessageDecorator.withSuccess(message))
                .doOnError(e -> LOGGER.error("Error while handling the message.", e))
                .onErrorReturn(MessageDecorator.withFail(message));
    }

    private String getTransactionName() {
        return getClass().getName() + "/handle";
    }

    /**
     * Process a message.
     *
     * @param message A message to be processed
     * @return Mono to continue flux.
     */
    protected abstract Mono<Void> handleMessage(Message message);

}
