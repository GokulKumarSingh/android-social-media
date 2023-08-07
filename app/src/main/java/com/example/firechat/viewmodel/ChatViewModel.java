package com.example.firechat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firechat.model.Message;
import com.example.firechat.model.ReceiveMessage;

import java.util.ArrayList;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Message>> messagesList = new MutableLiveData<>();
    private final MutableLiveData<ReceiveMessage> receiveMessage = new MutableLiveData<>();

    public void setMessagesList(ArrayList<Message> messageArray) {messagesList.setValue(messageArray);}
    public LiveData<ArrayList<Message>> getMessagesList() {return messagesList;}

    public void setReceiveMessage(ReceiveMessage message) {receiveMessage.setValue(message);}
    public LiveData<ReceiveMessage> getReceiveMessage() {return receiveMessage;}
}
