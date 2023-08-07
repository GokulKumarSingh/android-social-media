package com.example.firechat.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firechat.R;
import com.example.firechat.adapter.MainUserAdapter;
import com.example.firechat.databinding.FragmentHomeBinding;
import com.example.firechat.listener.OnUserClickedListener;
import com.example.firechat.model.User;
import com.example.firechat.util.Utils;
import com.example.firechat.viewmodel.UserViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnUserClickedListener {
    
    String TAG = "log9999";
    String selfUuid;
    private FragmentHomeBinding binding;
    Context context;
    ArrayList<User> userArrayList;

    public HomeFragment(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        Log.d(TAG, "onCreateView: start");

        selfUuid = getArguments().getString("self_uuid");
        userArrayList = new ArrayList<>();

        // TODO: 11/04/23 ask for all the users

        binding.mainRecycleView.setLayoutManager(new LinearLayoutManager(context));
        MainUserAdapter adapter = new MainUserAdapter(context, userArrayList, this);
        binding.mainRecycleView.setAdapter(adapter);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getUsersArray().observe(getActivity(), userArray -> {
            Log.d("log99999", "onCreateView: user recieved");
            for (int i = 0; i < userArray.size(); i++) {
                Log.d("log99999", "onCreateView: " + userArray.get(i).toString());
                if(!isUserAllReadyExist(userArray.get(i).getUuid())){
                    Log.d(TAG, "onCreateView: add");
                    userArrayList.add(userArray.get(i));
                }else{
                    Log.d(TAG, "onCreateView: remove");
                }
            }
            adapter.notifyDataSetChanged();
        });

        userViewModel.setCallForUsers();

        return binding.getRoot();
    }

    @Override
    public void onUserClicked(String uuid, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("self_uuid", selfUuid);
        bundle.putString("person_uuid", uuid);
        bundle.putString("person_name", name);
        ChatFragment chatFragment = new ChatFragment(context);
        chatFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.frameLayout, chatFragment).addToBackStack(null).commit();
    }

    private boolean isUserAllReadyExist(String uuid){
        for (int i = 0; i < userArrayList.size(); i++) {
            if(userArrayList.get(i).getUuid().equals(uuid)) return true;
        }
        return false;
    }
}