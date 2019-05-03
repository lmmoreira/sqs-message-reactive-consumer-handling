package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.newrelic.api.agent.Trace;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class AmazonSQSProvider {

    private static final String ALL_MESSAGE_ATTRIBUTES = "All";

    private final AmazonSQS amazonSQS;
    private final AmazonSQSProperties amazonSQSProperties;

    /**
     * Create an instance of class.
     * 
     * @param amazonSQS The SQS client
     * @param amazonSQSProperties Properties related to to the context of the consumer
     */
    AmazonSQSProvider(final AmazonSQS amazonSQS, final AmazonSQSProperties amazonSQSProperties) {
        this.amazonSQS = amazonSQS;
        this.amazonSQSProperties = amazonSQSProperties;
    }

    /**
     * It receive message from SQS.
     *
     * @return Received message result
     */
    @Trace(dispatcher = true)
    ReceiveMessageResult receive() {
        return amazonSQS.receiveMessage(getReceiveMessageRequest());
    }

    private ReceiveMessageRequest getReceiveMessageRequest() {
        return new ReceiveMessageRequest().withQueueUrl(amazonSQSProperties.getQueueName())
                .withMaxNumberOfMessages(amazonSQSProperties.getMaxNumberOfMessages())
                .withWaitTimeSeconds(amazonSQSProperties.getWaitTimeSeconds())
                .withMessageAttributeNames(ALL_MESSAGE_ATTRIBUTES);
    }

    /**
     * It will delete SQS messages in batch.
     *
     * @param messages List of messages to remove.
     * @return Delete messages in batch result.
     */
    @Trace(dispatcher = true)
    DeleteMessageBatchResult delete(List<Message> messages) {
        return amazonSQS.deleteMessageBatch(getDeleteMessageBatchRequest(messages));
    }

    /**
     * Usually called after message handling, it creates a SQS delete batch request to notify AWS to
     * remove these messages.
     *
     * @param messages List of messages.
     * @return SQS delete batch request for these messages.
     */
    private DeleteMessageBatchRequest getDeleteMessageBatchRequest(Collection<Message> messages) {
        return new DeleteMessageBatchRequest().withQueueUrl(amazonSQSProperties.getQueueName())
                .withEntries(messages.stream()
                        .map(message -> new DeleteMessageBatchRequestEntry().withId(message.getMessageId())
                                .withReceiptHandle(message.getReceiptHandle()))
                        .collect(Collectors.toSet()));
    }
}
