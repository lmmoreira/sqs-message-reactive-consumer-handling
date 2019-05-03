package com.lorem.logistics.event.metrics;

import com.lorem.logistics.event.Consumer;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * {@link Runnable} responsible for logging all given consumers metrics.
 */
public class ConsumerMetricsLogger implements Runnable {

    private final Collection<Consumer> consumers;

    /**
     * Initializes the runnable with desired consumers.
     * 
     * @param consumers Consumers.
     */
    public ConsumerMetricsLogger(Collection<Consumer> consumers) {
        this.consumers = requireNonNull(consumers);
    }

    @Override
    public void run() {
        this.consumers.forEach(consumer -> consumer.getMetrics().log());
    }
}
