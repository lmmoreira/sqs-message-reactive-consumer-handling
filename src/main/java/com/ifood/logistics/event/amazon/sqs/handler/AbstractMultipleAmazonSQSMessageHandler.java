package com.lorem.logistics.event.amazon.sqs.handler;

import com.amazonaws.services.sqs.model.Message;
import com.lorem.event.logs.TransactionLogHelper;
import com.newrelic.api.agent.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents cases where should handle a list of Message.
 */
public abstract class AbstractMultipleAmazonSQSMessageHandler implements AmazonSQSMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMultipleAmazonSQSMessageHandler.class);

    /**
     * Set the right transaction name before handle the message by calling
     * AbstractMultipleAmazonSQSMessageHandler::handleMessages.
     *
     * @param messages A list of {@link Message} to be handle
     * @return A Flux of a list of {@link MessageDecorator}
     */
    @Trace(dispatcher = true)
    @Override
    public Flux<List<MessageDecorator>> handle(final List<Message> messages) {
        TransactionLogHelper.overrideTransactionName(getTransactionName());

        return Flux.from(createMessageDecorators(messages));
    }

    private Mono<List<MessageDecorator>> createMessageDecorators(final List<Message> messages) {
        return handleMessages(messages) //
                .thenReturn(createSuccessDecorators(messages))
                .doOnError(throwable -> LOGGER.error("Error while handling the messages.", throwable))
                .onErrorReturn(createFailDecorators(messages));
    }

    private List<MessageDecorator> createSuccessDecorators(final List<Message> messages) {
        return messages.stream() //
                .map(MessageDecorator::withSuccess)
                .collect(Collectors.toList());
    }

    private List<MessageDecorator> createFailDecorators(final List<Message> messages) {
        return messages.stream().map(MessageDecorator::withFail).collect(Collectors.toList());
    }

    private String getTransactionName() {
        return getClass().getName() + "/handle";
    }

    protected abstract Mono<Void> handleMessages(final List<Message> messages);

}
