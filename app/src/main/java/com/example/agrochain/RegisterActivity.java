package com.example.agrochain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Spinner spinnerUserType, spinnerGender, spinnerDivision, spinnerDistrict, spinnerUpazila;
    private EditText editTextName, editTextAge, editTextPhone;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupSpinners();
    }

    private void initializeViews() {
        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerDivision = findViewById(R.id.spinnerDivision);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerUpazila = findViewById(R.id.spinnerUpazila);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonNext = findViewById(R.id.buttonNext);

        buttonNext.setOnClickListener(v -> moveToConfirmRegistrationActivity());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> userTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles_array, android.R.layout.simple_spinner_item);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(userTypeAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> divisionAdapter = ArrayAdapter.createFromResource(this,
                R.array.divisions_array, android.R.layout.simple_spinner_item);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(divisionAdapter);

        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(this,
                R.array.districts_array, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        ArrayAdapter<CharSequence> upazilaAdapter = ArrayAdapter.createFromResource(this,
                R.array.upazilas_array, android.R.layout.simple_spinner_item);
        upazilaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpazila.setAdapter(upazilaAdapter);
    }

    private void moveToConfirmRegistrationActivity() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", editTextName.getText().toString());
        userData.put("age", editTextAge.getText().toString());
        userData.put("gender", spinnerGender.getSelectedItem().toString());
        userData.put("division", spinnerDivision.getSelectedItem().toString());
        userData.put("district", spinnerDistrict.getSelectedItem().toString());
        userData.put("upazila", spinnerUpazila.getSelectedItem().toString());
        userData.put("phone", editTextPhone.getText().toString());
        userData.put("userType", spinnerUserType.getSelectedItem().toString());

        Intent intent = new Intent(RegisterActivity.this, ConfirmRegistrationActivity.class);
        intent.putExtra("userData", (Serializable) userData);
        startActivity(intent);
    }
}