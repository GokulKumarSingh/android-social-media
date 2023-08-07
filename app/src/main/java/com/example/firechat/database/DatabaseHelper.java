package com.example.firechat.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.firechat.model.Contact;
import com.example.firechat.model.Message;
import com.example.firechat.model.ReceiveMessage;
import com.example.firechat.model.User;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private String TAG = "logtag9999";

    public DatabaseHelper(Context context) {
        super(context, "firechat", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contact (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT)");
        db.execSQL("CREATE TABLE user (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, name Text, number TEXT, image TEXT, room INTEGER)");
        db.execSQL("CREATE TABLE pendingmessage (id INTEGER PRIMARY KEY AUTOINCREMENT, receiver TEXT, data TEXT, time TEXT, type TEXT)");
        db.execSQL("CREATE TABLE chat (id INTEGER PRIMARY KEY AUTOINCREMENT, person TEXT, data TEXT, senderId TEXT, time TEXT, type TEXT)");
//        new db called files
//        db.execSQL("CREATE TABLE files (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, path TEXT)");
//        new db called chat
//        "CREATE TABLE room" + personUuid + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, senderId TEXT, time TEXT, type TEXT)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS contact");
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS pendingmessage");
        onCreate(db);
    }

    //chat
    public void addChat(String personId, Message message){
//        return;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("person", personId);
        contentValues.put("data", message.getData());
        contentValues.put("senderId", message.getSenderId());
        contentValues.put("time", message.getTime());
        contentValues.put("type", message.getType());
        database.insert("chat", null, contentValues);
        database.close();
    }

    public ArrayList<Message> fetchChat(String personId){
        ArrayList<Message> messageArrayList = new ArrayList<>();
//        return messageArrayList;
        SQLiteDatabase database = this.getReadableDatabase();
        // TODO: 13/04/23 solve try
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM chat WHERE person = " + personId, null);
            while (cursor.moveToNext()) {
                Message message = new Message();
                message.setData(cursor.getString(2));
                message.setSenderId(cursor.getString(3));
                message.setTime(cursor.getString(4));
                message.setType(cursor.getString(5));
                messageArrayList.add(message);
            }
            database.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return messageArrayList;
    }

    public void readChat(){
        SQLiteDatabase database = this.getReadableDatabase();
        Log.d("TAG0000", "readChat: start");
        Cursor cursor = database.rawQuery("SELECT * FROM chat", null);
        while(cursor.moveToNext()){
            Message message = new Message();
            message.setData(cursor.getString(1));
            message.setSenderId(cursor.getString(2));
            message.setTime(cursor.getString(3));
            message.setType(cursor.getString(4));
            Log.d("TAG0000", "readChat: " + message.toString());
        }
        database.close();
    }


    //    contact
    public void addContact(String name, String number) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("number", number);
        database.insert("contact", null, contentValues);
        database.close();
    }

    public ArrayList<Contact> fetchAllContacts() {
        ArrayList<Contact> userArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user", null);
        while (cursor.moveToNext()) {
            Contact contact = new Contact();
            contact.setName(cursor.getString(2));
            contact.setNumber(cursor.getString(3));
            userArrayList.add(contact);
            Log.d(TAG, "fetchALLContact: -> " + contact.toString());
        }
        database.close();
        return userArrayList;
    }

    public Contact getContact(String number) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE number = " + number, null);
        if (cursor.getCount() == 0) return null;
        Contact contact = new Contact();
        while (cursor.moveToNext()) {
            contact.setName(cursor.getString(2));
            contact.setNumber(cursor.getString(3));
            break;
        }
        database.close();
        return contact;
    }

    //    user
    public void addUser(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", user.getUuid());
        contentValues.put("name", user.getName());
        contentValues.put("number", user.getNumber());
        contentValues.put("image", user.getImage());
        contentValues.put("room", 0);
        database.insert("user", null, contentValues);
        database.close();
    }

    public User getUser(String number) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE number = " + number, null);
        if (cursor.getCount() == 0) return null;
        User user = new User();
        while (cursor.moveToNext()) {
            user.setUuid(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setNumber(cursor.getString(3));
            user.setImage(cursor.getString(4));
            user.setRoom(cursor.getInt(5));
            break;
        }
        database.close();
        return user;
    }

    public ArrayList<User> fetchALLUser() {
        ArrayList<User> userArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM user", null);
        while (cursor.moveToNext()) {
            User user = new User();
            user.setUuid(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setNumber(cursor.getString(3));
            user.setImage(cursor.getString(4));
            user.setRoom(cursor.getInt(5));
            userArrayList.add(user);
            Log.d(TAG, "fetchALLUser: -> " + user.toString());
        }
        database.close();
        Log.d(TAG, "fetchALLUser: size -> " + userArrayList.size());
        return userArrayList;
    }

    //    todo pending message
    public void addPendingMessage(ReceiveMessage receiveMessage) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("receiver", receiveMessage.getReceiveId());
        contentValues.put("data", receiveMessage.getData());
        contentValues.put("time", receiveMessage.getTime());
        contentValues.put("type", receiveMessage.getType());
        database.insert("pendingmessage", null, contentValues);
        database.close();
    }

    public ArrayList<ReceiveMessage> fetchALLPendingMessage() {
        ArrayList<ReceiveMessage> receiveMessageArrayList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM pendingmessage", null);
        while (cursor.moveToNext()) {
            ReceiveMessage receiveMessage = new ReceiveMessage();
            receiveMessage.setReceiveId(cursor.getString(1));
            receiveMessage.setData(cursor.getString(2));
            receiveMessage.setTime(cursor.getString(3));
            receiveMessage.setType(cursor.getString(4));

            receiveMessageArrayList.add(receiveMessage);
            Log.d("log9999", "fetchALLUser: -> " + receiveMessage.toString());
        }
        database.close();
        return receiveMessageArrayList;
    }

    public void deleteAllPendingMessages() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("pendingmessage", null, null);
    }

    //    todo custom room
    public ArrayList<Message> readChat(String personUuid) {
        ArrayList<Message> messageArrayList = new ArrayList<>();
        // TODO: 04/04/23 problem in database
//        SQLiteDatabase database = this.getReadableDatabase();
//        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = " + personUuid, null);
//        if (cursor.getCount() == 0) Log.d(TAG, "readChat: no user found" + personUuid);
//
//        int room = 0;
//        while (cursor.moveToNext()) {
//            room = cursor.getInt(5);
//            Log.d(TAG, "readChat: room -_ " + room);
//            break;
//        }
//        if (room == 1) {
//            Cursor c = database.rawQuery("SELECT * FROM room" + personUuid, null);
//            Log.d(TAG, "readChat: count - " + c.getCount());
//            while (c.moveToNext()) {
//                Message message = new Message(c.getString(1), c.getString(2), c.getString(3), c.getString(4));
//                Log.d(TAG, "readChat: -> " + message.toString());
//                messageArrayList.add(message);
//            }
//        }
//        database.close();
        return messageArrayList;
    }

    public void writeChat(Message message, String personUuid) {
        return;
//        Log.d(TAG, "writeChat: => uuid " + personUuid);
//        SQLiteDatabase database = this.getWritableDatabase();
//        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE uuid = " + personUuid, null);
//        int room = 0;
//        while (cursor.moveToNext()) {
//            room = cursor.getInt(5);
//            Log.d(TAG, "readChat: room -_ " + room);
//            break;
//        }
//        //
//        if (room == 0) {
//            String CREATE_TABLE = "CREATE TABLE room" + personUuid + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, senderId TEXT, time TEXT, type TEXT)";
//            database.execSQL(CREATE_TABLE);
//            ContentValues cv = new ContentValues();
//            cv.put("room", 1);
//            database.update("user", cv, "uuid = " + personUuid, null);
//        }
//
//        ContentValues contentValues = new ContentValues();
//        new Message();
//        contentValues.put("data", message.getData());
//        contentValues.put("senderId", message.getSenderId());
//        contentValues.put("timeStamp", message.getTime());
//        contentValues.put("type", message.getType());
//        database.insert("room" + personUuid, null, contentValues);
//        database.close();
    }

    //    todo extra
    public void createNewTable(String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        String CREATE_TABLE = "CREATE TABLE " + name + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, senderId TEXT, timeStamp TEXT, type TEXT)";
        database.execSQL(CREATE_TABLE);
        database.close();
    }

    public void returnTablesName() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.d("log8888", "returnDatabasesName: - Table Name => " + c.getString(0));
                c.moveToNext();
            }
        }
        database.close();
    }

    public void addValues(String name, ContentValues contentValues) {
        try {
            Log.d("TAG3333", "addValues: good" + name);
            SQLiteDatabase database = this.getWritableDatabase();
            database.insert(name, null, contentValues);
            database.close();
        }catch (Exception e){
            Log.d("TAG3333", "addValues: bad" + name);
            e.printStackTrace();
        }
    }
}