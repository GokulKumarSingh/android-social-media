package com.example.firechat.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.firechat.R;
import com.example.firechat.activity.CallingActivity;
import com.example.firechat.adapter.MessageAdapter;
import com.example.firechat.databinding.FragmentChatBinding;
import com.example.firechat.model.Files;
import com.example.firechat.model.Message;
import com.example.firechat.model.ReceiveMessage;
import com.example.firechat.util.Utils;
import com.example.firechat.viewmodel.ChatViewModel;

import org.parceler.Parcels;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private static final String TAG = "log9999";
    Context context;
    private FragmentChatBinding binding;
    String self_uuid;
    String person_uuid;
    String person_name;

    ChatViewModel chatViewModel;
    ArrayList<Message> messageArrayList;
    MessageAdapter adapter;

    Dialog dialog;

    String SEND_TYPE = "text";

    public ChatFragment(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);

        init();
        // TODO: 12/04/23 send get message from database command
        chatViewModel.setReceiveMessage(new ReceiveMessage(null, person_uuid, null, null));
        chatViewModel.getMessagesList().observe(getActivity(), messages -> {
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getSenderId().equals(self_uuid) || messages.get(i).getSenderId().equals(person_uuid)) {
                    messageArrayList.addAll(messages);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        binding.chatSendBtn.setOnClickListener(v -> {
            String msg = binding.edtMessage.getText().toString();
            binding.edtMessage.setText("");
            Message message = new Message(msg, self_uuid, String.valueOf(System.currentTimeMillis()), SEND_TYPE);
            messageArrayList.add(message);
            adapter.notifyDataSetChanged();
            chatViewModel.setReceiveMessage(new ReceiveMessage(msg, person_uuid, String.valueOf(System.currentTimeMillis()), SEND_TYPE));

        });

        binding.videoCall.setOnClickListener(v -> {
            //go to video call activity
            Intent intent = new Intent(getActivity(), CallingActivity.class);
            intent.putExtra("person_uuid", person_uuid);
            startActivity(intent);
        });

        binding.chatAttach.setOnClickListener(v -> {
            chatAttach();
        });

        return binding.getRoot();
    }

    private void chatAttach() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayoutCompat photos = dialog.findViewById(R.id.photos);
        LinearLayoutCompat videos = dialog.findViewById(R.id.videos);
        LinearLayoutCompat location = dialog.findViewById(R.id.location);
        LinearLayoutCompat document = dialog.findViewById(R.id.documents);
        LinearLayoutCompat audio = dialog.findViewById(R.id.audios);
        LinearLayoutCompat contact = dialog.findViewById(R.id.contacts);

        photos.setOnClickListener(view -> {
            SEND_TYPE = "photos";
            Log.d(TAG, "chatAttach: photo");
            chooseFile("photos");
        });
        videos.setOnClickListener(view -> {
            SEND_TYPE = "videos";
            chooseFile("videos");
        });
        location.setOnClickListener(view -> {
            SEND_TYPE = "location";
            sendLocation();
//            chooseFile("location");
        });
        document.setOnClickListener(view -> {
            SEND_TYPE = "documents";
            chooseFile("documents");
        });
        audio.setOnClickListener(view -> {
            SEND_TYPE = "audios";
            chooseFile("audios");
        });
        contact.setOnClickListener(view -> {
            SEND_TYPE = "contacts";
//            chooseFile("contacts");
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendLocation() {
        messageArrayList.add(new Message("Your live location has been send", self_uuid, String.valueOf(System.currentTimeMillis()), "location"));
        adapter.notifyDataSetChanged();
        chatViewModel.setReceiveMessage(new ReceiveMessage("", person_uuid, String.valueOf(System.currentTimeMillis()), "location"));
    }

    private void chooseFile(String type) {
        dialog.dismiss();
        Log.d(TAG, "chooseFile: " + type);
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bundle.putString("path", System.getenv("EXTERNAL_STORAGE"));
        bundle.putParcelable("files", Parcels.wrap(new Files()));
        FileFragment fileFragment = new FileFragment(context);
        fileFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, fileFragment).addToBackStack(null).commit();
    }

    private void init() {
        messageArrayList = new ArrayList<>();

        self_uuid = getArguments().getString("self_uuid");
        person_uuid = getArguments().getString("person_uuid");
        person_name = getArguments().getString("person_name");

        binding.chatUserName.setText(person_name);
        binding.chatBackBtn.setOnClickListener(v -> getActivity().onBackPressed());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(context, messageArrayList, self_uuid);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);
        binding.chatRecyclerView.setAdapter(adapter);

        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
    }
}