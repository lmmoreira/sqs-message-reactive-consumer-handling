package com.lorem.logistics.event;

public interface Handler<S, T> {

    T handle(S source);

}
