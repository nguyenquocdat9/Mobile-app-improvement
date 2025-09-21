package com.example.myapplication.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

public class NumberSelectorView extends LinearLayout {

    private ImageButton btnIncrease, btnDecrease;
    private TextView tvCount;
    private int count = 0;
    private int min = 0;
    private int max = 99;

    public void SetMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public NumberSelectorView(Context context) {
        super(context);
        init(context);
    }

    public NumberSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        // Set orientation (QUAN TRỌNG khi dùng merge)
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.customview_number_selector, this, true);
        btnIncrease = findViewById(R.id.plusButton);
        btnDecrease = findViewById(R.id.minusButton);
        tvCount = findViewById(R.id.numText);

        System.out.println("Init Called in selector");

        tvCount.setText(String.valueOf(count));

        CheckButtonEnable();

        btnIncrease.setOnClickListener(v -> {
            if (count < max) {
                count++;
                tvCount.setText(String.valueOf(count));
            }

            CheckButtonEnable();
        });

        btnDecrease.setOnClickListener(v -> {
            if (count > min) {
                count--;
                tvCount.setText(String.valueOf(count));
            }

            CheckButtonEnable();
        });
    }

    private void CheckButtonEnable() {
        btnDecrease.setEnabled(count > min);
        btnDecrease.setAlpha(count > min ? 1f : 0.2f);
        btnIncrease.setEnabled(count < max);
        btnIncrease.setAlpha(count < max ? 1f : 0.2f);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (count >= min && count <= max) {
            this.count = count;
            tvCount.setText(String.valueOf(count));

            CheckButtonEnable();
        }
    }

    public void setMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }
}

