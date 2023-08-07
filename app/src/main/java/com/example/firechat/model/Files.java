package com.example.firechat.model;

import org.parceler.Parcel;

import java.io.File;
import java.util.ArrayList;

@Parcel
public class Files {
    ArrayList<File> fileArrayList = new ArrayList<>();

    public Files() {
    }

    public Files(ArrayList<File> fileArrayList) {
        this.fileArrayList = fileArrayList;
    }

    public ArrayList<File> getFileArrayList() {
        return fileArrayList;
    }

    public void setFileArrayList(ArrayList<File> fileArrayList) {
        this.fileArrayList = fileArrayList;
    }
}
