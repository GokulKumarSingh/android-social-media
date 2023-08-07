package com.example.firechat.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firechat.R;
import com.example.firechat.listener.OnFileSelectedListener;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    String type;
    Context context;
    ArrayList<File> fileList;
    OnFileSelectedListener listener;

    public FileAdapter(String type, Context context, ArrayList<File> fileList, OnFileSelectedListener listener) {
        this.type = type;
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileAdapter.FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_row_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.fileName.setText(fileList.get(position).getName());

        int item = 0;
        if(fileList.get(position).isDirectory()){
            holder.fileImage.setImageResource(R.drawable.folder);
            File[] allFiles = fileList.get(position).listFiles();
            if(allFiles!=null){
                for(File singleFile : allFiles){
                    if(!singleFile.isHidden()){
                        item++;
                    }
                }
            }
            holder.fileName.append(" - " + item);
        }else{
            if(type.equals("photos")){
                holder.fileImage.setImageResource(R.drawable.gallery);
            }else if(type.equals("videos")){
                holder.fileImage.setImageResource(R.drawable.clapperboard);
            }else if(type.equals("audios")){
                holder.fileImage.setImageResource(R.drawable.audio);
            }else {
                holder.fileImage.setImageResource(R.drawable.doc);
            }
            holder.fileName.append(" - " + Formatter.formatFileSize(context, fileList.get(position).length()));
        }

        holder.itemView.setOnClickListener(v -> {
            if(!fileList.get(position).isDirectory()){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.appColorBlue));
            }
            listener.onFileClicked(fileList.get(position));
        });

        holder.itemView.setOnLongClickListener(v->{
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.appColorBlue));
            listener.onFileLongClicked(fileList.get(position), position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder{
        ImageView fileImage;
        TextView fileName;
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImage = itemView.findViewById(R.id.fileImage);
            fileName = itemView.findViewById(R.id.fileNameText);
        }
    }
}
