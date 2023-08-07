package com.example.firechat.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firechat.model.Message;

import java.io.File;
import java.util.ArrayList;

public class FileViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<File>> fileArrayForActivity = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<File>> fileArrayForFragment = new MutableLiveData<>();

    private final MutableLiveData<Boolean> done = new MutableLiveData<>();

    public void setFileArrayForActivity(ArrayList<File> files) {fileArrayForActivity.setValue(files);}
    public LiveData<ArrayList<File>> getFileArrayForActivity() {return fileArrayForActivity;}

    public void setFileArrayForFragment(ArrayList<File> files) {fileArrayForFragment.setValue(files);}
    public LiveData<ArrayList<File>> getFileArrayForFragment() {return fileArrayForFragment;}

    public void setDone(Boolean isDone) {done.setValue(isDone);}
    public LiveData<Boolean> getDone() {return done;}
}
