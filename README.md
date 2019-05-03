# README

This project provide a reactive way for consume AWS SQS messages.

### Using 

To use, we should instance the class `AmazonSQSConsumer` providing an `AmazonSQS`, 
`AmazonSQSProperties` and `AbstractAmazonSQSMessageHandler`.

```java
@Configuration
@EnableConfigurationProperties
public class IdpOrderTrackingConfig {
    
    @Bean("idpSqsConsumerExecutor")
    public AmazonSQSConsumer idpSqsConsumerExecutor(AmazonSQS amazonSQS,
                                                    IdpSqsConsumerProperties properties,
                                                    IdpOrderTrackingMessageHandler handler) {
        return new AmazonSQSConsumer(amazonSQS, properties, handler);
    }
    
}

```
#### AmazonSQS
Is part of Amazon SDK and used to access SQS.

#### IdpSqsConsumerProperties
Is an `AmazonSQSProperties` implementation, and contains configurations needed to run the consumer.
```java
public class IdpSqsConsumerProperties implements AmazonSQSProperties {

    private String queue;
    private Integer maxNumberOfMessages;
    private Integer waitTimeSeconds;
    private Integer retries = 3;
    private Integer retryBackoffMillis = 8000;
    private Integer publishers;
    private Boolean autoStart;

    public String getQueueName() { return queue; }

    public Integer getMaxNumberOfMessages() { return maxNumberOfMessages; }

    public Integer getWaitTimeSeconds() { return waitTimeSeconds; }

    public Integer getRetries() { return retries; }

    public Integer getRetryBackoffMillis() { return retryBackoffMillis; }

    public Integer getNumberOfPublishers() { return publishers; }

    public Boolean getAutoStart() { return autoStart; }
}
```
An alternative is to use `GenericAmazonSQSProperties` which come with some default values:
    
* maxNumberOfMessages = 10
* waitTimeSeconds = 20
* retries = 3
* retryBackoffMillis = 8000
* numberOfPublishers = 4
* autoStart = Boolean.TRUE
    
An example of use can be as below
```java
@Configuration
@EnableConfigurationProperties
public class IdpOrderTrackingConfig {
    
    @Bean(value = "orderTrackingProperties", initMethod = validate)
    @ConfigurationProperties(prefix = "fleet.telemetry.order.tracking.consumer")
    public GenericAmazonSQSProperties orderTrackingProperties() {
        return new GenericAmazonSQSProperties();
    }
    
}
```
`GenericAmazonSQSProperties` implements `validate` method which is called by the `Consumer`. This method perform the follow rules:
* queueName, cannot be null or blank;
* maxNumberOfMessages, cannot be null, less than 1 and greater than 10;
* waitTimeSeconds, cannot be null, less than 1 and greater than 20;
* retries, cannot be null, less than 0;
* retryBackoffMillis, cannot be null, less than 0;
* numberOfPublishers, cannot be null, less than 1;
* Boolean.TRUE, cannot be null;


#### IdpOrderTrackingMessageHandler
It is an `AmazonSQSMessageHandler` implementation which contains the logic to be process for the messages received.
There are 2 abstract class to help implement this interface, `AbstractSingleAmazonSQSMessageHandler`and `AbstractMultipleAmazonSQSMessageHandler`.

`AbstractSingleAmazonSQSMessageHandler` is used when the handle will process 1 Message at time and while `AbstractMultipleAmazonSQSMessageHandler` is used to process a list of Message. The above code is processing only one Message at time. 

```java
@Component("idpOrderTrackingMessageHandler")
public class IdpOrderTrackingMessageHandler extends AbstractSingleAmazonSQSMessageHandler {

    @Autowired
    private FleetTelemetryDriverDTOConverter converter;

    @Autowired
    private FleetTelemetryIdpService fleetTelemetryIdpService;

    @Override
    protected void handleMessage(final Message message) {
        FleetTelemetryDriverDTO dto = Objects.requireNonNull(converter.convert(message.getBody()));
        fleetTelemetryIdpService.send(dto);
    }
}
```

#### Metrics

The `Consumer` interface exposes a method called `getMetrics` by which execution metrics can be retrieved and logged.
Also, the class `ConsumerMetricsLogger`, that receives a `Consumer` as its constructor's parameter can be used for
logging the event metrics of them. An example of use can be a scheduled logger, as follows:

```java
@Configuration
@EnableScheduling
public class ApplicationConfig implements SchedulingConfigurer {

    private static final long INTERVAL_METRICS_LOG = 60000;

    @Autowired
    private List<Consumer> consumers;

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addFixedDelayTask(new ConsumerMetricsLogger(consumers), INTERVAL_METRICS_LOG);
    }

}
```# sqs-message-reactive-consumer-handling
