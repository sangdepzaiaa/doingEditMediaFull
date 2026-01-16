package com.example.myapplication.reactlibrary.utils;

public interface ISaveEffectCallback {
    public void onSaving();

    public void onSuccess(String path);

    public void onError();
}
