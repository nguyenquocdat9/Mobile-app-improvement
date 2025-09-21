package com.example.myapplication.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.R;
import com.example.myapplication.data.Enum.PropertyStatus;
import com.example.myapplication.data.Model.Property.Property;
import com.example.myapplication.data.Repository.Auth.AuthRepository;
import com.example.myapplication.data.Repository.Property.PropertyRepository;
import com.example.myapplication.interfaces.IStepValidator;
import com.example.myapplication.ui.misc.PropertyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Date;
import java.util.List;

public class CreatePropertyActivity extends AppCompatActivity {
    private static final int TOTAL_STEP = 5;
    private PropertyViewModel viewModel;
    private NavHostFragment navigator;
    private NavController navController;

    private AlertDialog loadingDialog;
    private boolean isUpdatingProcess = false;

    MaterialButton nextButton;
    MaterialButton prevButton;
    MaterialButton exitButton;

    private int stepIndex = 0;

    private final int[] stepNextTrans = {
            R.id.t01,
            R.id.t12,
            R.id.t23,
            R.id.t34
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_property);

        viewModel = new ViewModelProvider(this).get(PropertyViewModel.class);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("property_json")) {
            String json = intent.getStringExtra("property_json");

            if (json != null && !json.isEmpty()) {
                try {
                    Property property = new Gson().fromJson(json, Property.class);

                    if (property != null) {
                        viewModel.setPropertyData(property);
                        isUpdatingProcess = true;
                        Log.d("IntentCheck", "Nhận property thành công: " + property.name);
                    } else {
                        Log.e("IntentCheck", "Không thể parse JSON thành Property");
                    }

                } catch (JsonSyntaxException e) {
                    Log.e("IntentCheck", "Lỗi parse JSON: " + e.getMessage());
                }
            } else {
                Log.w("IntentCheck", "JSON rỗng hoặc null");
            }
        } else {
            Log.w("IntentCheck", "Không nhận được extra 'property_json'");
        }

        navigator = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.stepNavigator);

        if(navigator != null) navController = navigator.getNavController();


        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        exitButton = findViewById(R.id.exitButton);

        nextButton.setOnClickListener(v -> NextStep());
        prevButton.setOnClickListener(v -> PrevStep());
        exitButton.setOnClickListener(v -> finish());
    }

    private void NextStep() {
        Fragment current = navigator.getChildFragmentManager().getFragments().get(0);

        if (current instanceof IStepValidator) {
            String warning = null;
            if (((IStepValidator) current).validate(warning)) {
                ((IStepValidator) current).save();
                Toast.makeText(this, "Validate Successfully", Toast.LENGTH_SHORT).show();

                if (stepIndex >= TOTAL_STEP - 1 && !isUpdatingProcess) {
                    Property property = viewModel.getPropertyData().getValue();

                    AuthRepository auth = new AuthRepository(this);
                    property.host_id = auth.getUserUid();
                    property.created_at = new Date();
                    property.updated_at = new Date();
                    property.status = PropertyStatus.Active;
                    PropertyRepository propertyRepository = new PropertyRepository(this);
                    showLoadingDialog();
                    propertyRepository.addProperty(property, this,
                            unused -> {
                                hideLoadingDialog();
                                showSuccessDialog();
                            },
                            e -> {
                                hideLoadingDialog();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    Log.d("Property", viewModel.getPropertyData().getValue().toString());
                } else if (stepIndex >= TOTAL_STEP - 1 && isUpdatingProcess) {
                    Property property = viewModel.getPropertyData().getValue();
                    property.updated_at = new Date();

                    showLoadingDialog();
                    PropertyRepository propertyRepository = new PropertyRepository(this);
                    propertyRepository.updateProperty(property.getId(), property, this,
                            unused ->{
                                hideLoadingDialog();
                                showSuccessDialog();
                            },
                            e -> {
                                hideLoadingDialog();
                                Toast.makeText(this,  e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    navController.navigate(stepNextTrans[stepIndex]);
                    stepIndex++;
                }
            } else {

            }
        }

        if (stepIndex == TOTAL_STEP - 1) nextButton.setText(isUpdatingProcess ? "Cập nhật" : "Hoàn thành");
    }

    private void PrevStep() {
        Fragment current = navigator.getChildFragmentManager().getFragments().get(0);
        nextButton.setText("Tiếp theo");

        if (stepIndex <= 0) return;

        if (current instanceof IStepValidator) {
            ((IStepValidator) current).save();

            navController.popBackStack();
            stepIndex--;
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(view);

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(CreatePropertyActivity.this, HostMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }


}
