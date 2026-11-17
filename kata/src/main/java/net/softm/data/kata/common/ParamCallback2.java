package net.softm.data.kata.common;

//public interface ParamCallback {
//    void onResult(Object param);
//}



public interface ParamCallback2<T,S> {
    void onResult(T result,S result2);
    default void onError(Exception exception) {
    }
}
