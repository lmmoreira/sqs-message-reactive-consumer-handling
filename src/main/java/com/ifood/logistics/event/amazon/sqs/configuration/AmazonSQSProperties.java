package com.lorem.logistics.event.amazon.sqs.configuration;

/**
 * Holder for SQS consumer properties.
 */
public interface AmazonSQSProperties {

    String getQueueName();

    Integer getMaxNumberOfMessages();

    Integer getWaitTimeSeconds();

    Integer getRetries();

    Integer getRetryBackoffMillis();

    Integer getNumberOfPublishers();

    Boolean getAutoStart();

    void validate();
}
