package io.agora.base.callback;

public interface ThrowableCallback<T> extends Callback<T> {
    void onFailure(Throwable throwable);
}
