package net.softm.data.kata.common;

//public interface ParamCallback {
//    void onResult(Object param);
//}



public interface ParamCallback<T> {
    void onResult(T result);
    default void onError(Exception exception) {
    }
}
