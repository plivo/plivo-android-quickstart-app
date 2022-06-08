package com.plivo.plivosimplequickstart;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class App extends Application {

    private PlivoBackEnd backend;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.options.put("context",getApplicationContext());
        Utils.options.put("sharedContext",getApplicationContext());
        backend = PlivoBackEnd.newInstance();
        backend.setContext(this);
        backend.init(BuildConfig.DEBUG);


        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/MyPersonalAppFolder" );
            File logDirectory = new File( appDirectory + "/logs" );
            File logFile = new File( logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );
            boolean p = false;
            boolean x = false;
            // create app folder
            if ( !appDirectory.exists() ) {

                x = appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {

                p = logDirectory.mkdir();
            }


            Log.d("TAG", "onCreate: "+x +""+p);

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
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

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
