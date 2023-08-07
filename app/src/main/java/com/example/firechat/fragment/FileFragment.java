package com.example.firechat.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firechat.R;
import com.example.firechat.adapter.FileAdapter;
import com.example.firechat.databinding.FragmentFileBinding;
import com.example.firechat.listener.OnFileSelectedListener;
import com.example.firechat.viewmodel.FileViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FileFragment extends Fragment implements OnFileSelectedListener {

    private static final String TAG = "log9999";
    FragmentFileBinding binding;
    Context context;
    File storage;
    String type = "documents";
    ArrayList<File> fileList;
    ArrayList<File> selectedFileList;

    FileViewModel fileViewModel;

    String self_uuid;
    String person_uuid;
    String person_name;

    public FileFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileBinding.inflate(inflater, container, false);

        fileList = new ArrayList<>();
        selectedFileList = new ArrayList<>();
        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);
        fileViewModel.setFileArrayForActivity(null);
        fileViewModel.getFileArrayForFragment().observe(getActivity(), files -> {
            Log.d("TAG0000", "onCreateView: " + files.size());
            selectedFileList = files;
        });

        storage = new File(Objects.requireNonNull(System.getenv("EXTERNAL_STORAGE")));
        //sd card
        if(false) {
            String cardStorage = "";
            File[] externalCacheDirs = context.getExternalCacheDirs();
            for (File file : externalCacheDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    cardStorage = file.getPath().split("/Android")[0];
                    break;
                }
            }
            storage = new File(cardStorage);
        }
        String argPath = getArguments().getString("path");
        type = getArguments().getString("type");

        self_uuid = getArguments().getString("self_uuid");
        person_uuid = getArguments().getString("person_uuid");
        person_name = getArguments().getString("person_name");

        Log.d(TAG, "onCreateView: path " + argPath);
        Log.d("TAG8888", "onCreateView: type " + type);

        if (argPath != null && !argPath.equals("")) storage = new File(argPath);
        if (type != null && !type.equals("")) type = "documents";

        Log.d(TAG, "onCreateView: storage - " + storage.getAbsolutePath());
        displayFiles();

        binding.doneButton.setOnClickListener(view -> {
            for (File singleFile : selectedFileList) {
                Log.d("TAG1111", "initViewModel: frag file - " + singleFile.getAbsolutePath());
            }
            Log.d(TAG, "onCreateView: " + selectedFileList.size());
            fileViewModel.setFileArrayForActivity(selectedFileList);
            fileViewModel.setDone(true);
            Bundle bundle = new Bundle();
            bundle.putString("self_uuid", self_uuid);
            bundle.putString("person_uuid", person_uuid);
            bundle.putString("person_name", person_name);
            ChatFragment chatFragment = new ChatFragment(context);
            chatFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, chatFragment).addToBackStack(null).commit();
        });
        return binding.getRoot();
    }

    private void displayFiles() {
        binding.fileRecycleView.setHasFixedSize(true);
        binding.fileRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        fileList = findFiles(storage);
        binding.fileRecycleView.setAdapter(new FileAdapter(type, getContext(), fileList, this));
    }

    private ArrayList<File> findFiles(File path) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = path.listFiles();
        if(files==null) return arrayList;
        for (File singleFile : files) {
            Log.d(TAG, "findFiles: files - " + singleFile.getAbsolutePath());
            // TODO: 09/04/23 remove is hidden parameter
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.add(singleFile);
            } else {
                arrayList.add(singleFile);
                switch (getArguments().getString("type")) {
                    case "photos":
                        Log.d(TAG, "findFiles: Photos");
                        if (singleFile.getName().toLowerCase().endsWith(".jpeg") ||
                                singleFile.getName().toLowerCase().endsWith(".jpg") ||
                                singleFile.getName().toLowerCase().endsWith(".png") ||
                                singleFile.getName().toLowerCase().endsWith(".gif") ||
                                singleFile.getName().toLowerCase().endsWith(".tiff") ||
                                singleFile.getName().toLowerCase().endsWith(".raw")) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "videos":
                        Log.d(TAG, "findFiles: videos");
                        if (singleFile.getName().toLowerCase().endsWith(".mp4") || singleFile.getName().toLowerCase().endsWith(".mov") || singleFile.getName().toLowerCase().endsWith(".wmv") || singleFile.getName().toLowerCase().endsWith(".avi") || singleFile.getName().toLowerCase().endsWith(".avchd") || singleFile.getName().toLowerCase().endsWith(".flv") || singleFile.getName().toLowerCase().endsWith(".f4v") || singleFile.getName().toLowerCase().endsWith(".swf") || singleFile.getName().toLowerCase().endsWith(".mkv") || singleFile.getName().toLowerCase().endsWith(".webm") || singleFile.getName().toLowerCase().endsWith(".html5") || singleFile.getName().toLowerCase().endsWith(".mpeg-2")) {
                            arrayList.add(singleFile);
                        }
                        break;
                    case "audios":
                        Log.d(TAG, "findFiles: audio");
                        if (singleFile.getName().toLowerCase().endsWith(".mp3") || singleFile.getName().toLowerCase().endsWith(".wav"))
                        arrayList.add(singleFile);
                        break;
                    case "contacts":
                        Log.d(TAG, "findFiles: contacts");
                        break;
                    case "location":
                        Log.d(TAG, "findFiles: location");
                        break;
                    default:
                        Log.d(TAG, "findFiles: documents");
                        arrayList.add(singleFile);
                }
            }
        }
        for (File file : arrayList) {
            Log.d(TAG, "findFiles: file name - " + file.getName());
        }
        return arrayList;
    }


    @Override
    public void onFileClicked(File file) {
        Log.d(TAG, "onFileClicked: " + file.getAbsolutePath());
        if(file.isDirectory()){
            fileViewModel.setFileArrayForActivity(selectedFileList);
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putString("path", file.getAbsolutePath());
            bundle.putString("self_uuid", self_uuid);
            bundle.putString("person_uuid", person_uuid);
            bundle.putString("person_name", person_name);
            FileFragment fileFragment = new FileFragment(context);
            fileFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, fileFragment).addToBackStack(null).commit();
        }else{
            selectedFileList.add(file);
        }
    }

    @Override
    public void onFileLongClicked(File file, int position) {
        selectedFileList.add(file);
    }
}