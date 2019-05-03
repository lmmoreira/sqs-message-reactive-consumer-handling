package com.lorem.logistics.event.amazon.sqs.handler;

import com.amazonaws.services.sqs.model.Message;

import java.util.Objects;

/**
 * A decorator of {@link Message} carrying the result of the process made by the
 * {@link AmazonSQSMessageHandler} implementation.
 */
public class MessageDecorator {

    private final boolean successfullyProcessed;
    private final Message message;

    private MessageDecorator(final boolean successfullyProcessed, final Message message) {
        this.successfullyProcessed = successfullyProcessed;
        this.message = message;
    }

    public boolean isSuccessfullyProcessed() {
        return successfullyProcessed;
    }

    public Message getMessage() {
        return message;
    }

    public static MessageDecorator withSuccess(Message message) {
        return new MessageDecorator(true, message);
    }

    public static MessageDecorator withFail(Message message) {
        return new MessageDecorator(false, message);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MessageDecorator that = (MessageDecorator) o;
        return successfullyProcessed == that.successfullyProcessed && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successfullyProcessed, message);
    }
}
