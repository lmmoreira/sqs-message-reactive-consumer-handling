package com.lorem.logistics.event;

import com.lorem.logistics.event.metrics.EventMetrics;

public interface Consumer {

    /**
     * Start consume from the source.
     */
    void start();

    /**
     * Stop consume the source.
     */
    void stop();

    /**
     * Return consumer's event metrics.
     * 
     * @return Consumer's event metrics.
     */
    EventMetrics getMetrics();

    /**
     * Return if the consumer is consuming.
     * 
     * @return If the consumer is consuming return <code>true</code> else <code>false</code>.
     */
    boolean isRunning();
}
