package com.example.myapplication.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.ui.activities.AIFindActivity;
import com.example.myapplication.ui.activities.SearchActivity;
import com.example.myapplication.ui.misc.Post;
import com.example.myapplication.ui.adapters.PostAdapter;
import com.example.myapplication.utils.PostConverter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;


public class ExploreFragment extends Fragment {
    private EditText searchBar;
    private RecyclerView recyclerView;
    private ImageButton aiSeachButton;
    // Store property data from backend
    private PropertyRepository propertyRepository;
    // List to hold UI post items
    private List<Post> postList;
    // Adapter to display posts in RecyclerView
    private PostAdapter postAdapter;

    private List<Post> fullPostList = new ArrayList<>(); // chứa toàn bộ dữ liệu gốc (không lọc)


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ... UI code:
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        searchBar = view.findViewById(R.id.search_bar);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        aiSeachButton = view.findViewById(R.id.aiSearch);
        aiSeachButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AIFindActivity.class);
            startActivity(intent);
        });

        // Initialize empty list and adapter
        postList = new ArrayList<>();
        fullPostList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, false);
        recyclerView.setAdapter(postAdapter);

        // Create repository instance to interact with Firebase
        propertyRepository = new PropertyRepository(requireContext());
        fetchBackendData();

        // Bắt sự kiện khi người dùng nhập vào search bar
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void fetchBackendData() {
        // Call repository method to get all properties from Firestore
        propertyRepository.getAllProperties(
                // Success callback - receives List<Property> from Firebase
                new OnSuccessListener<List<Property>>() {
                    @Override
                    public void onSuccess(List<Property> properties) {
                        // Clear existing posts
                        postList.clear();
                        fullPostList.clear();
                        fullPostList.addAll(PostConverter.convertPropertiesToPosts(properties));
                        postList.addAll(PostConverter.convertPropertiesToPosts(properties));
                        // Notify adapter to refresh RecyclerView
                        postAdapter.notifyDataSetChanged();
                    }
                },
                // Failure callback - shows error toast
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(),
                                "Failed to fetch data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /*ham tim kiem don gian
    private void filterPosts(String query) {
        postList.clear();
        if (query.isEmpty()) {
            postList.addAll(fullPostList); // Nếu rỗng thì hiển thị tất cả
        } else {
            String lowerQuery = query.toLowerCase();
            for (Post post : fullPostList) {
                if (post.getTitle().toLowerCase().contains(lowerQuery) ||
                        post.getLocation().toLowerCase().contains(lowerQuery) ||
                        post.getDetail().toLowerCase().contains(lowerQuery)) {
                    postList.add(post);
                }
            }
        }
        postAdapter.notifyDataSetChanged();
    }

     */

}