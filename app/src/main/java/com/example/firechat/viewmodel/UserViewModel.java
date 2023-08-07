package com.example.firechat.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firechat.model.User;

import java.util.ArrayList;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<User>> usersArray = new MutableLiveData<>();
    private final MutableLiveData<User> callForUsers = new MutableLiveData<>();

    public void setUsersArray(ArrayList<User> userArrayList) {usersArray.setValue(userArrayList);}
    public MutableLiveData<ArrayList<User>> getUsersArray() {return usersArray;}

    public void setCallForUsers() {callForUsers.setValue(new User());}
    public MutableLiveData<User> getCallForUsers() {return callForUsers;}
}
