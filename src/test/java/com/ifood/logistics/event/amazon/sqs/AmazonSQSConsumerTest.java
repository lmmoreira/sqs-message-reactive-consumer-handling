package com.lorem.logistics.event.amazon.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.lorem.logistics.event.amazon.sqs.configuration.AmazonSQSProperties;
import com.lorem.logistics.event.amazon.sqs.handler.AmazonSQSMessageHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AmazonSQSConsumerTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCheckAmazonSQSParameter() {
        expectedException("AmazonSQS ");

        new AmazonSQSConsumer(null, null, null);
    }

    @Test
    public void shouldCheckAmazonSQSPropertiesParameter() {
        expectedException("AmazonSQSProperties ");

        final AmazonSQS amazonSQS = mock(AmazonSQS.class);
        new AmazonSQSConsumer(amazonSQS, null, null);
    }

    @Test
    public void shouldCheckAmazonSQSMessageHandlerParameter() {
        expectedException("AmazonSQSMessageHandler ");

        final AmazonSQS amazonSQS = mock(AmazonSQS.class);
        final AmazonSQSProperties properties = mock(AmazonSQSProperties.class);
        new AmazonSQSConsumer(amazonSQS, properties, null);
    }

    @Test
    public void shouldCheckPropertiesValues() {
        final AmazonSQS amazonSQS = mock(AmazonSQS.class);
        final AmazonSQSProperties properties = mock(AmazonSQSProperties.class);
        final AmazonSQSMessageHandler handler = mock(AmazonSQSMessageHandler.class);
        new AmazonSQSConsumer(amazonSQS, properties, handler);

        verify(properties, times(1)).validate();
    }

    private void expectedException(final String substring) {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(substring);
    }

}
