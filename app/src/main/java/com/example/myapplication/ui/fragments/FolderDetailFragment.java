package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.ui.adapters.PostAdapter;
import com.example.myapplication.ui.misc.WishlistFolder;

public class FolderDetailFragment extends Fragment {
    private WishlistFolder folder;

    private boolean isTitleVisible = false;
    private int showThresholdPx;

    public static FolderDetailFragment newInstance(WishlistFolder folder) {
        FolderDetailFragment fragment = new FolderDetailFragment();
        fragment.folder = folder;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_detail, container, false);

        ImageButton backButton = view.findViewById(R.id.btnBack);
        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );


        TextView folderHeaderText = view.findViewById(R.id.headerTitle);
        folderHeaderText.setText(folder.getName());
        TextView folderTopText = view.findViewById(R.id.topTitle);
        folderTopText.setText(folder.getName());

        RecyclerView recyclerView = view.findViewById(R.id.housesRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        PostAdapter adapter = new PostAdapter(getContext(), folder.getPosts(), false);
        recyclerView.setAdapter(adapter);

        //Header animation setup
        NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollView);
        View headerLine = view.findViewById(R.id.headerLine);

        // Ngưỡng tính bằng pixels (100dp)
        float dpThreshold = 50f;
        showThresholdPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpThreshold, getResources().getDisplayMetrics()
        );

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > 0) {
                        headerLine.setVisibility(View.VISIBLE);
                    } else {
                        headerLine.setVisibility(View.GONE);
                    }

                    if (scrollY > showThresholdPx && !isTitleVisible) {
                        isTitleVisible = true;
                        folderHeaderText.animate().alpha(1f).setDuration(150).start();
                    } else if (scrollY <= showThresholdPx && isTitleVisible) {
                        isTitleVisible = false;
                        folderHeaderText.animate().alpha(0f).setDuration(50).start();
                    }
                }
        );

        return view;
    }
}