package com.wildLive.secondScreen;

public interface SignalRCallback<T> {
    void onSuccess(T var1);
    void onError(Error var1);
}

interface RegisterSignalRCallback<T> {
    void register(T device, T type);
}