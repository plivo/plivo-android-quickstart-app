package com.plivo.plivosimplequickstart;

import android.app.Application;

public class App extends Application {

    private PlivoBackEnd backend;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.options.put("context",getApplicationContext());
        Utils.options.put("sharedContext",getApplicationContext());
        backend = PlivoBackEnd.newInstance();
        backend.init(BuildConfig.DEBUG,getApplicationContext());
    }

    public PlivoBackEnd backend() {
        return backend;
    }
}
