package com.example.androidfinalproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CardiologistPage extends AppCompatActivity {

    private EditText etDate, etTime;
    private ImageButton btnDatePicker, btnTimePicker;
    private Button btnBookAppointment, btnGoBack;
    private RadioGroup rgDoctors;

    private DatabaseReference doctorRef;
    private List<Model> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardiologist_page);

        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        btnDatePicker = findViewById(R.id.btn_date_picker);
        btnTimePicker = findViewById(R.id.btn_time_picker);
        btnBookAppointment = findViewById(R.id.btn_book_appointment);
        btnGoBack = findViewById(R.id.btn_go_back);
        rgDoctors = findViewById(R.id.rg_doctors); // RadioGroup to hold doctors

        doctorList = new ArrayList<>();
        doctorRef = FirebaseDatabase.getInstance().getReference().child("doctors");


        loadDoctors();

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        btnBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();
                int selectedId = rgDoctors.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(CardiologistPage.this, "Please select a doctor", Toast.LENGTH_SHORT).show();
                } else {
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    Model selectedDoctor = (Model) selectedRadioButton.getTag(); // Retrieve doctor object from tag
                    String doctorName = selectedRadioButton.getText().toString();
                    Toast.makeText(CardiologistPage.this, "Appointment Booked with Dr. " + doctorName + " on " + date + " at " + time, Toast.LENGTH_SHORT).show();

                }
            }
        });

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadDoctors() {

        Query query = doctorRef.orderByChild("specialty").equalTo("Cardiologist");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                doctorList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Model doctor = snapshot.getValue(Model.class);
                    doctorList.add(doctor);
                }


                updateRadioGroup();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CardiologistPage.this, "Failed to load doctors: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRadioGroup() {
        rgDoctors.removeAllViews();

        for (Model doctor : doctorList) {
            RadioButton radioButton = new RadioButton(CardiologistPage.this);
            radioButton.setText(doctor.getName());
            radioButton.setTag(doctor);


            rgDoctors.addView(radioButton);
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> etDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1)),
                year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> etTime.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                hour, minute, false);
        timePickerDialog.show();
    }
}
