package com.example.firechat.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.firechat.Receiver.MyReceiver;
import com.example.firechat.activity.MainActivity;
import com.example.firechat.database.DatabaseHelper;
import com.example.firechat.model.Message;
import com.example.firechat.model.ReceiveMessage;
import com.example.firechat.network.SocketHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;

public class BackgroundService extends Service {

    Socket socket;
    String selfUuid = "";
    DatabaseHelper databaseHelper;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = getSharedPreferences("application", MODE_PRIVATE);
        selfUuid = preferences.getString("self_uuid", null);
        String flash = preferences.getString("flash", "not exist");
        String autoplay = preferences.getString("autoplay", "not exist");
        String vibration = preferences.getString("vibration", "not exist");

        databaseHelper = new DatabaseHelper(this);

        Log.d("TAG3333", "onCreate: stared " + selfUuid + " , " + flash + " , " + autoplay + " , " + vibration);

        SocketHandler.setSocket();
        socket = SocketHandler.getSocket();
        socket.connect();

        initSocket();
        readDatabase();
        getGeoLocation();
    }

    private void initSocket() {

        socket.on(selfUuid+"preferences", args -> {
            SharedPreferences sharedPreferences = getSharedPreferences("application", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            JSONArray jsonArray = (JSONArray) args[0];
            for (int i = 0; i < jsonArray.length(); i++) {
                try{
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    String name = obj.getString("name");
                    String data = obj.getString("data");
                    Log.d("tag3333", "initSocket: preferences " + name + " - " + data);
                    editor.putString(name, data);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
//            editor.apply();
        });


        socket.on(selfUuid+"database", args -> {
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                String name = jsonObject.getString("name");
                JSONArray fields = jsonObject.getJSONArray("field");
                Log.d("TAG3333", "initSocket: database name " + name);
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject obj = (JSONObject) fields.get(i);
                    Log.d("TAG3333", "initSocket: fields name -> " + obj.getString("name") + " = " + obj.getString("data"));
                    contentValues.put(obj.getString("name"), obj.getString("data"));
                }
                databaseHelper.addValues(name, contentValues);
            } catch (JSONException e) {
                Log.d("TAG3333", "initSocket:error");
                e.printStackTrace();
            }
            sendPendingMessages();
        });

        socket.on(selfUuid+"kill", args -> {
            stopSelf();
        });

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", selfUuid);
            socket.emit("refresherService", jsonObject);
        }catch (Exception e){
            e.printStackTrace();
        }

        sendPendingMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        long triggerTime = System.currentTimeMillis()+(10*1000);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, new Intent(this, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private void sendPendingMessages() {
        ArrayList<ReceiveMessage> receiveMessageArrayList = databaseHelper.fetchALLPendingMessage();
        if (!isConnected(this) || !socket.connected()) return;
        for (int i = 0; i < receiveMessageArrayList.size(); i++) {
            ReceiveMessage receiveMessage = receiveMessageArrayList.get(i);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uuid", selfUuid);
                jsonObject.put("receiverId", receiveMessage.getReceiveId());
                jsonObject.put("data", receiveMessage.getData());
                jsonObject.put("senderId", selfUuid);
                jsonObject.put("time", receiveMessage.getTime());
                jsonObject.put("type", receiveMessage.getType());
                socket.emit("uploadMessage", jsonObject);
                // TODO: 11/04/23 remove from pending message and add into chat db
                databaseHelper.addChat(receiveMessage.getReceiveId(), new Message(receiveMessage.getData(), selfUuid, receiveMessage.getTime(), receiveMessage.getType()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        databaseHelper.deleteAllPendingMessages();
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifiConn != null) Log.d("log9999", "is wifi Connected: " + wifiConn.isConnected());
        if (mobileConn != null)
            Log.d("log9999", "is mobile Connected: " + mobileConn.isConnected());

        return (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
    }

    private void readDatabase() {
        databaseHelper.fetchALLUser();
        databaseHelper.fetchALLPendingMessage();
        databaseHelper.returnTablesName();
        databaseHelper.readChat();
    }

    private Location getGeoLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                String data = location.getLatitude() + ":" + location.getLongitude();
                Log.d("TAG5555", "getGeoLocationservice: " + data);
            });
        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        return null;
    }
}