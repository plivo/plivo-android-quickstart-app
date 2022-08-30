package com.plivo.plivosimplequickstart;

import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    private static final String TAG = "AppPlivo";

    private PlivoBackEnd backend;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        Utils.options.put("context",getApplicationContext());
        Utils.options.put("sharedContext",getApplicationContext());
        backend = PlivoBackEnd.newInstance();
        backend.setContext(this);
        backend.init(BuildConfig.DEBUG);

        writeLogFile();

    }

    private void writeLogFile() {
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( getApplicationContext().getCacheDir() + "/MyPersonalAppFolder" );
            File logDirectory = new File( appDirectory + "/logs" );
            File logFile = new File( logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );
            boolean p = false;
            boolean x = false;
            if ( !appDirectory.exists() ) {
                x = appDirectory.mkdir();
            }

            if ( !logDirectory.exists() ) {
                p = logDirectory.mkdir();
            }


            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }
    }

    public PlivoBackEnd backend() {
        return backend;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }
}
