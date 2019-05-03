package com.lorem.logistics.event.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Wrapper for event metrics. <br />
 * Metrics can be accumulated via {@link #incrementMetric(String)} and
 * {@link #addToMetric(String, long)} methods. Then, they can be retrieved and/or reset by sum's and
 * log's methods.
 */
public class EventMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventMetrics.class);

    private final ConcurrentMap<String, LongAdder> metrics;

    private final String name;

    private LocalDateTime metricsStart;

    /**
     * Initialize the event metrics with the given name.
     * 
     * @param name The name to be assigned.
     */
    public EventMetrics(String name) {
        this.name = requireNonNull(name, "Name can't be null");
        this.metrics = new ConcurrentHashMap<>();
        resetMetricsStart();
    }

    private void resetMetricsStart() {
        this.metricsStart = LocalDateTime.now();
    }

    /**
     * Increment the value of the {@code metricName}. Equivalent to {@code addToMetric(metricName, 1L)}.
     * 
     * @param metricName Metric's name.
     */
    public void incrementMetric(String metricName) {
        metrics.computeIfAbsent(metricName, key -> new LongAdder()).increment();
    }

    /**
     * It adds the given {@code value} for {@code metricName}.
     * 
     * @param metricName Metric's name.
     * @param value Value to add.
     */
    public void addToMetric(String metricName, long value) {
        metrics.computeIfAbsent(metricName, key -> new LongAdder()).add(value);
    }

    /**
     * Get a map containing all metrics and their accumulated values.
     * 
     * @return Metrics map.
     */
    public Map<String, Long> sum() {
        synchronized (metrics) {
            return metrics.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue().sum()));
        }
    }

    /**
     * Log accumulated metrics at INFO ({@link Logger}) level.
     */
    public void log() {
        LOGGER.info("{} metrics since {}: {}", name, metricsStart, sum());
    }

}
