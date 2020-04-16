package edu.uncw.example.firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FirestoreListActivity extends AppCompatActivity {

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final String TAG = "FirestoreListActivity";
    private final String PATIENTS = "patients";

    ArrayAdapter<Patient> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_list);

        ListView patientListView = findViewById(R.id.patientListView);
//        adapter = new ArrayAdapter<Patient>(
//                this,
//                android.R.layout.simple_list_item_1,
//                new ArrayList<Patient>());

        adapter = new PatientAdapter(this, new ArrayList<Patient>());
        patientListView.setAdapter(adapter);

    }

    class PatientAdapter extends ArrayAdapter<Patient> {
        ArrayList<Patient> patients;

        PatientAdapter(Context context, ArrayList<Patient> patients) {
            super(context, 0, patients);
            this.patients = patients;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.patient_list_item, parent, false);
            }

            TextView patientName = convertView.findViewById(R.id.itemName);
            TextView patientAge = convertView.findViewById(R.id.itemAge);

            Patient patient = patients.get(position);
            patientName.setText(patient.getName());
            patientAge.setText(Integer.toString(patient.getAge()));

            return convertView;
        }
    }

    public void refreshData(View view) {
        mDb.collection(PATIENTS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Patient> patients = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Patient p = document.toObject(Patient.class);
                            Log.d(TAG, "Patient object: " + p.getName() + " " + p.getAge());
                            patients.add(p);
                        }
                        adapter.clear();
                        adapter.addAll(patients);
                    }
                });
    }

    public void submitData(View view) {
        EditText nameEditText = findViewById(R.id.nameEditText);
        String name = nameEditText.getText().toString();

        EditText ageEditText = findViewById(R.id.ageEditText);
        String ageString = ageEditText.getText().toString();
        int age = Integer.parseInt(ageString);

        Log.d(TAG, "Submitted name: " + name + ", age: " + ageString);

        Patient p = new Patient(name, age);
        mDb.collection(PATIENTS)
                .add(p)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Patient entry added.");
                        Toast.makeText(FirestoreListActivity.this, "Patient entry added!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding patient!", e);
                        Toast.makeText(FirestoreListActivity.this, "Error adding patient!", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
