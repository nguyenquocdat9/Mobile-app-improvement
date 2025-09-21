package com.example.myapplication.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class PaymentMethodFragment extends Fragment {
    private Button nextButton;
    private String propertyId;
    private String hostId;
    private String checkInDay;
    private String checkOutDay;
    private double totalPrice;
    private String selectedPaymentMethod;
    private RadioGroup paymentOptionsGroup;

    public PaymentMethodFragment() {
        super(R.layout.fragment_payment_method);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
    }

    private void initializeViews(View view) {
        paymentOptionsGroup = view.findViewById(R.id.payment_options_group);
        nextButton = view.findViewById(R.id.next_button);

        // Set default payment method
        if (paymentOptionsGroup.getCheckedRadioButtonId() != -1) {
            RadioButton selectedButton = view.findViewById(paymentOptionsGroup.getCheckedRadioButtonId());
            selectedPaymentMethod = selectedButton.getText().toString();
        }
    }

    private void setupListeners() {
        if (paymentOptionsGroup != null) {
            paymentOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedButton = getView().findViewById(checkedId);
                if (selectedButton != null) {
                    selectedPaymentMethod = selectedButton.getText().toString();
                }
            });
        }

        if (nextButton != null) {
            nextButton.setOnClickListener(v -> navigateToConfirmPayment());
        }
    }

    private void navigateToConfirmPayment() {
        if (selectedPaymentMethod == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        if (getArguments() != null) {
            args.putAll(getArguments());  // Get all data from RoomBookingFragment
        }
        args.putString("paymentMethod", selectedPaymentMethod);

        ConfirmPaymentFragment confirmFragment = new ConfirmPaymentFragment();
        confirmFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, confirmFragment)
                .addToBackStack(null)
                .commit();
    }
}