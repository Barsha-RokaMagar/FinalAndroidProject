package com.example.androidfinalproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Calendar;

public class DermatologistPage extends AppCompatActivity {

    private EditText dateInput, timeInput;
    private ImageButton selectDateButton, selectTimeButton;
    private Button makeAppointmentButton, goBackButton;
    private RadioGroup doctorRadioGroup;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, appointmentRef;
    private String selectedDermatologist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dermatologist_page); // Ensure layout file matches

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        appointmentRef = FirebaseDatabase.getInstance().getReference().child("appointments");

        dateInput = findViewById(R.id.et_date);
        timeInput = findViewById(R.id.et_time);
        selectDateButton = findViewById(R.id.btn_date_picker);
        selectTimeButton = findViewById(R.id.btn_time_picker);
        makeAppointmentButton = findViewById(R.id.btn_book_appointment);
        goBackButton = findViewById(R.id.btn_go_back);
        doctorRadioGroup = findViewById(R.id.rg_doctors);

        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        makeAppointmentButton.setOnClickListener(v -> bookAppointment());
        goBackButton.setOnClickListener(v -> goBack());

        loadDermatologists();

        doctorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = findViewById(checkedId);
            selectedDermatologist = selectedButton != null ? selectedButton.getText().toString() : null;
            Log.d("DermatologistPage", "Selected dermatologist: " + selectedDermatologist);
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) ->
                        dateInput.setText(selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear),
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) ->
                        timeInput.setText(String.format("%02d:%02d", selectedHour, selectedMinute)),
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    private void bookAppointment() {
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty() || selectedDermatologist == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String patientId = currentUser.getUid();

        DatabaseReference newAppointment = appointmentRef.push();
        newAppointment.child("date").setValue(date);
        newAppointment.child("time").setValue(time);
        newAppointment.child("dermatologist").setValue(selectedDermatologist);
        newAppointment.child("patientId").setValue(patientId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DermatologistPage.this, "Appointment booked", Toast.LENGTH_SHORT).show();

                Log.d("DermatologistPage", "Appointment booked - Date: " + date + ", Time: " + time + ", Dermatologist: " + selectedDermatologist);

                Intent intent = new Intent(DermatologistPage.this, AppointmentDetailsofPatients.class);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("dermatologistName", selectedDermatologist); // Updated to dermatologist
                intent.putExtra("specialty", "Dermatologist"); // Updated to dermatologist
                startActivity(intent);
            } else {
                Log.e("DermatologistPage", "Failed to book appointment", task.getException());
                Toast.makeText(DermatologistPage.this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goBack() {
        Intent intent = new Intent(DermatologistPage.this, PatientPage.class);
        startActivity(intent);
        finish();
    }

    private void loadDermatologists() {
        usersRef.orderByChild("specialty").equalTo("Dermatologist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DermatologistPage", "Data snapshot received: " + dataSnapshot.toString());
                doctorRadioGroup.removeAllViews();

                if (!dataSnapshot.exists()) {
                    Log.d("DermatologistPage", "No dermatologists found.");
                    Toast.makeText(DermatologistPage.this, "No dermatologists available", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) {
                        RadioButton radioButton = new RadioButton(DermatologistPage.this);
                        radioButton.setText(name);
                        doctorRadioGroup.addView(radioButton);
                        Log.d("DermatologistPage", "Added dermatologist: " + name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DermatologistPage", "Failed to load dermatologists", databaseError.toException());
                Toast.makeText(DermatologistPage.this, "Failed to load dermatologists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
