package com.lorem.logistics.event.amazon.sqs.configuration;

/**
 * This class is a default implementation of {@link AmazonSQSProperties}. The follow properties are
 * set with default values:
 * <ul>
 * <li>maxNumberOfMessages = 10;</li>
 * <li>waitTimeSeconds = 20;</li>
 * <li>retries = 3;</li>
 * <li>retryBackoffMillis = 8000;</li>
 * <li>numberOfPublishers = 4 and</li>
 * <li>autoStart = true</li>
 * </ul>
 */
public class GenericAmazonSQSProperties implements AmazonSQSProperties {

    private String queueName;
    private Integer maxNumberOfMessages = 10;
    private Integer waitTimeSeconds = 20;
    private Integer retries = 3;
    private Integer retryBackoffMillis = 8000;
    private Integer numberOfPublishers = 4;
    private Boolean autoStart = Boolean.TRUE;

    /**
     * Check properties values, if a property is not a valid it will throw an
     * {@link IllegalArgumentException}. The follow rules are check:
     *
     * <ul>
     * <li>queueName, cannot be null or blank;</li>
     * <li>maxNumberOfMessages, cannot be null, less than 1 and greater than 10;</li>
     * <li>waitTimeSeconds, cannot be null, less than 1 and greater than 20;</li>
     * <li>retries, cannot be null, less than 0;</li>
     * <li>retryBackoffMillis, cannot be null, less than 0;</li>
     * <li>numberOfPublishers, cannot be null, less than 1;</li>
     * <li>Boolean.TRUE, cannot be null;</li>
     * </ul>
     */
    @Override
    public void validate() {
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException("queueName must have value");
        }
        if (maxNumberOfMessages == null || maxNumberOfMessages < 1 || maxNumberOfMessages > 10) {
            throw new IllegalArgumentException("maxNumberOfMessages be between 1 and 10");
        }
        if (waitTimeSeconds == null || waitTimeSeconds < 1 || waitTimeSeconds > 20) {
            throw new IllegalArgumentException("waitTimeSeconds be between 1 and 20");
        }
        if (retries == null || retries < 0) {
            throw new IllegalArgumentException("retries must be greater than or equals to 0");
        }
        if (retryBackoffMillis == null || retryBackoffMillis < 0) {
            throw new IllegalArgumentException("retryBackoffMillis must be greater than or equals to 0");
        }
        if (numberOfPublishers == null || numberOfPublishers < 1) {
            throw new IllegalArgumentException("numberOfPublishers must be greater than 0");
        }
        if (autoStart == null) {
            throw new IllegalArgumentException("autoStart must have value");
        }
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public Integer getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    @Override
    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    @Override
    public Integer getRetries() {
        return retries;
    }

    @Override
    public Integer getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    @Override
    public Integer getNumberOfPublishers() {
        return numberOfPublishers;
    }

    @Override
    public Boolean getAutoStart() {
        return autoStart;
    }

    public void setQueueName(final String queueName) {
        this.queueName = queueName;
    }

    public void setMaxNumberOfMessages(final Integer maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public void setWaitTimeSeconds(final Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public void setRetries(final Integer retries) {
        this.retries = retries;
    }

    public void setRetryBackoffMillis(final Integer retryBackoffMillis) {
        this.retryBackoffMillis = retryBackoffMillis;
    }

    public void setNumberOfPublishers(final Integer numberOfPublishers) {
        this.numberOfPublishers = numberOfPublishers;
    }

    public void setAutoStart(final Boolean autoStart) {
        this.autoStart = autoStart;
    }
}
