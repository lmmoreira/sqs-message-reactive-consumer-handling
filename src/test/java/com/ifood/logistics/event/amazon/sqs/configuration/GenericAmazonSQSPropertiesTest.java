package com.lorem.logistics.event.amazon.sqs.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GenericAmazonSQSPropertiesTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    // queueName
    @Test
    public void shouldBeValidQueueName() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("queue-name-sqs-test");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenQueueNameIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName(null);

        expectedException("queueName");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenQueueNameIsEmpty() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("");

        expectedException("queueName");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenQueueNameIsBlank() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName(" ");

        expectedException("queueName");

        properties.validate();
    }

    // maxNumberOfMessages
    @Test
    public void shouldBeValidMaxNumberOfMessages() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setMaxNumberOfMessages(10);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenMaxNumberOfMessagesIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setMaxNumberOfMessages(null);

        expectedException("maxNumberOfMessages");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenMaxNumberOfMessagesIsLessThanOne() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setMaxNumberOfMessages(0);

        expectedException("maxNumberOfMessages");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenMaxNumberOfMessagesIsGreaterThanTen() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setMaxNumberOfMessages(11);

        expectedException("maxNumberOfMessages");

        properties.validate();
    }

    // waitTimeSeconds
    @Test
    public void shouldBeValidWaitTimeSeconds() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setWaitTimeSeconds(10);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenWaitTimeSecondsIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setWaitTimeSeconds(null);

        expectedException("waitTimeSeconds");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenWaitTimeSecondsIsLessThanOne() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setWaitTimeSeconds(0);

        expectedException("waitTimeSeconds");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenWaitTimeSecondsIsGreaterThanTwenty() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setWaitTimeSeconds(25);

        expectedException("waitTimeSeconds");

        properties.validate();
    }

    // retries
    @Test
    public void shouldBeValidRetries() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetries(10);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenRetriesIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetries(null);

        expectedException("retries");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenRetriesIsLessThanZero() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetries(-1);

        expectedException("retries");

        properties.validate();
    }

    // retryBackoffMillis
    @Test
    public void shouldBeValidRetryBackoffMillis() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetryBackoffMillis(10);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenRetryBackoffMillisIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetryBackoffMillis(null);

        expectedException("retryBackoffMillis");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenRetryBackoffMillisIsLessThanZero() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setRetryBackoffMillis(-1);

        expectedException("retryBackoffMillis");

        properties.validate();
    }

    // numberOfPublishers
    @Test
    public void shouldBeValidNumberOfPublishers() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setNumberOfPublishers(10);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenNumberOfPublishersIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setNumberOfPublishers(null);

        expectedException("numberOfPublishers");

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenNumberOfPublishersIsLessThanOne() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setNumberOfPublishers(0);

        expectedException("numberOfPublishers");

        properties.validate();
    }

    // autoStart
    @Test
    public void shouldBeValidAutoStart() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setAutoStart(false);

        properties.validate();
    }

    @Test
    public void shouldBeInvalidWhenAutoStartIsNull() {
        GenericAmazonSQSProperties properties = new GenericAmazonSQSProperties();
        properties.setQueueName("any-queue-name");
        properties.setAutoStart(null);

        expectedException("autoStart");

        properties.validate();
    }

    private void expectedException(final String substring) {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(substring);
    }
}
