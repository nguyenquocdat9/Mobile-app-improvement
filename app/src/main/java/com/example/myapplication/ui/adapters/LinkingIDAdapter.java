package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;

import java.util.ArrayList;
import java.util.List;

public class LinkingIDAdapter extends RecyclerView.Adapter<LinkingIDAdapter.LinkingViewHolder>{
    private List<String> linkingList;
    private OnLinkingRemoveListener listener;

    public LinkingIDAdapter(List<String> linkingList, OnLinkingRemoveListener listener) {
        this.linkingList = linkingList;
        if (this.linkingList == null) this.linkingList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public LinkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_linking, parent, false);
        return new LinkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkingViewHolder holder, int position) {
        String id = linkingList.get(position);
        holder.bind(id);
    }

    @Override
    public int getItemCount() {
        return linkingList.size();
    }


    public interface OnLinkingRemoveListener {
        void onRemoveLinking();
    }
    class LinkingViewHolder extends RecyclerView.ViewHolder {
        private TextView idText;
        private ImageButton removeButton;

        public LinkingViewHolder(@NonNull View itemView) {
            super(itemView);
            idText = itemView.findViewById(R.id.textLinkingID);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        void bind(String id) {
            idText.setText(id);
            removeButton.setOnClickListener(v -> listener.onRemoveLinking());
        }
    }
}
