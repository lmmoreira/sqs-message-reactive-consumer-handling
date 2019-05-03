package com.lorem.logistics.event.amazon.sqs.handler;

import com.amazonaws.services.sqs.model.Message;
import com.lorem.logistics.event.Handler;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AmazonSQSMessageHandler extends Handler<List<Message>, Flux<List<MessageDecorator>>> {
}
