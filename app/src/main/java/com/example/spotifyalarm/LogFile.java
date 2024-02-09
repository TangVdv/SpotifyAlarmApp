package com.example.spotifyalarm;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile extends AppCompatActivity {
    private static final String TAG = "LogFile";
    private static final String FILENAME = "Log_SpotifyAlarm.txt";
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private FileOutputStream writer;
    private File file;

    public LogFile(Context context) {
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FILENAME);
        Log.i(TAG, file.getPath());
        try {
            if (file.exists()) {
                Log.i(TAG, "File already exists");
            } else {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) {
                    Log.e(TAG, "Unable to create file at specified path. It already exists");
                }
            }
            Log.i(TAG, writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String title, String message) {
        Date date = new Date();
        String content = dateFormat.format(date) + "\t" + title + "\t" + message + "\n";
        try {
            writer = new FileOutputStream(file, true); // Append mode
            writer.write(content.getBytes());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}