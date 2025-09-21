package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.misc.Post;
import com.example.myapplication.ui.misc.WishlistFolder;

import java.util.List;

public class WishlistFolderAdapter extends RecyclerView.Adapter<WishlistFolderAdapter.FolderViewHolder> {
    private List<WishlistFolder> folders;
    private OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(WishlistFolder folder);
    }

    public WishlistFolderAdapter(List<WishlistFolder> folders, OnFolderClickListener listener) {
        this.folders = folders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        WishlistFolder folder = folders.get(position);
        holder.folderName.setText(folder.getName());

        // Show first 4 house images in a grid
        List<Post> posts = folder.getPosts();
        for (int i = 0; i < 3; i++) {
            if (i < posts.size()) {
                Glide.with(holder.itemView.getContext())
                        .load(posts.get(i).getImageResId()) // nếu là URL, nên đổi tên hàm thành getImageUrl()
                        .placeholder(R.drawable.photo1)
                        .error(R.drawable.photo1)
                        .into(holder.previewImages[i]);

            } else {
                holder.previewImages[i].setImageResource(R.color.divider_gray); // hoặc ảnh placeholder
            }
        }


        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView[] previewImages;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.nameFolder);
            previewImages = new ImageView[]{
                    itemView.findViewById(R.id.image1),
                    itemView.findViewById(R.id.image2),
                    itemView.findViewById(R.id.image3)
            };
        }
    }
}