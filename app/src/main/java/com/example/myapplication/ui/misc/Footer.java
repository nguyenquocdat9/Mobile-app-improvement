package com.example.myapplication.ui.misc;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.widget.Button;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragments.ExploreFragment;
import com.example.myapplication.ui.fragments.MessagesFragment;
import com.example.myapplication.ui.fragments.ProfileFragment;
import com.example.myapplication.ui.fragments.TripsFragment;
import com.example.myapplication.ui.fragments.WishlistFragment;

public class Footer {
    public static void setupFooterNavigation(FragmentActivity activity) {
        Button buttonExplore = activity.findViewById(R.id.button_explore);
        Button buttonWishlists = activity.findViewById(R.id.button_wishlists);
        Button buttonTrips = activity.findViewById(R.id.button_trips);
        Button buttonMessages = activity.findViewById(R.id.button_messages);
        Button buttonProfile = activity.findViewById(R.id.button_profile);

        Button[] buttons = new Button[]{
                buttonExplore, buttonWishlists, buttonTrips, buttonMessages, buttonProfile
        };

        buttonExplore.setOnClickListener(v -> loadFragment(activity, new ExploreFragment(), buttons, buttonExplore));
        buttonWishlists.setOnClickListener(v -> loadFragment(activity, new WishlistFragment(), buttons, buttonWishlists));
        buttonTrips.setOnClickListener(v -> loadFragment(activity, new TripsFragment(), buttons, buttonTrips));
        buttonMessages.setOnClickListener(v -> loadFragment(activity, new MessagesFragment(), buttons, buttonMessages));
        buttonProfile.setOnClickListener(v -> loadFragment(activity, new ProfileFragment(), buttons, buttonProfile));

        // Set initial selection
        buttonExplore.setSelected(true);
    }

    private static void loadFragment(FragmentActivity activity, Fragment fragment, Button[] buttons, Button selectedButton) {
        // Update button states
        for (Button button : buttons) {
            button.setSelected(button == selectedButton);
        }

        // Load fragment
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}