package io.agora.base;

public interface ThrowableCallback<T> extends Callback<T> {
    void onFailure(Throwable throwable);
}
