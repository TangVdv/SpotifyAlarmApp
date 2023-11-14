package com.example.spotifyalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyalarm.databinding.ActivityMainBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONObject;

import java.io.Serializable;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    public Context context;
    public String playlist_uri;
    public SpotifyAPI spotifyAPI;

    public ArrayAdapter<String> spinnerAdapter;
    public List<PlaylistModel> playlistModelList;

    private ActivityMainBinding binding;
    private MaterialTimePicker timePicker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private SharedPreferences sharedPreferences;

    private int hour;
    private int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        context = this;

        startActivity();

        hour = sharedPreferences.getInt("time_hour", 0);
        minute = sharedPreferences.getInt("time_minute", 0);

        setCalendar();
        setBtnSetTimeText();


        binding.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPlaylist();
            }
        });

        binding.btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTime();
            }
        });

        binding.setAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setAlarm();
                }
                else{
                    cancelAlarm();
                }
            }
        });
    }

    public void startActivity(){
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(context.getString(R.string.client_id), AuthorizationResponse.Type.TOKEN, context.getString(R.string.redirect_uri));

        builder.setScopes(new String[]{"streaming", "playlist-read-private"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, context.getResources().getInteger(R.integer.request_code) ,request);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == context.getResources().getInteger(R.integer.request_code)) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                spotifyAPI = new SpotifyAPI(this, response.getAccessToken());
                spotifyAPI.getUserPlaylist(new SpotifyAPI.PlaylistCallBack() {
                    @Override
                    public void onSuccess(List<PlaylistModel> list) {
                        if(list != null && !list.isEmpty()){
                            ArrayList<String> playListName = new ArrayList<String>(list.size());
                            playlistModelList = list;
                            list.forEach((p) -> playListName.add(p.getName()));

                            spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, playListName);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerPlaylist.setAdapter(spinnerAdapter);
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("MainActivity", error);
                    }
                });
            }
        }
    }

    public void selectPlaylist(){
        PlaylistModel playlistModel = playlistModelList.get((int) binding.spinnerPlaylist.getSelectedItemId());
        playlist_uri = playlistModel.getSpotifyId();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("playlist_uri", playlist_uri);
        editor.apply();
    }

    public void selectTime(){
        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(sharedPreferences.getInt("time_hour", 0))
                .setMinute(sharedPreferences.getInt("time_minute", 0))
                .setTitleText("Select Alarm Time")
                .build();


        timePicker.show(getSupportFragmentManager(), "UwU");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
                editor.putInt("time_hour", timePicker.getHour());
                editor.putInt("time_minute", timePicker.getMinute());
                editor.apply();

                setBtnSetTimeText();

                setCalendar();
            }
        });
    }

    public void setBtnSetTimeText(){
        binding.btnSetTime.setText(hour + ":" + minute);
    }

    public void setCalendar(){
        calendar = calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if(calendar.getTimeInMillis() < System.currentTimeMillis()){
            calendar.add(Calendar.DATE, 1);
        }
    }

    public void setAlarm(){
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        createText(
                new SimpleDateFormat("HH:mm:ss").format(calendar.getTime()) + " ; " + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime().getTime())
        );
        createText(calendar.getTimeInMillis() + " ; " + System.currentTimeMillis());

        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm(){
        if(pendingIntent != null){
            if(alarmManager == null){
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            }
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    public void createText(String text){
        TextView textView = new TextView(this);
        textView.setText(text);
        binding.testLayout.addView(textView);
    }
}