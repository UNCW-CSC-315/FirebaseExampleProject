package edu.uncw.example.firebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FirestoreListActivity extends AppCompatActivity {

    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private static final String TAG = "FirestoreListActivity";
    private static final String PATIENTS = "patients";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_list);

    }

    public void onRefreshClick(View view) {
        mDb.collection(PATIENTS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Patient> patients = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Patient p = document.toObject(Patient.class);
                            patients.add(p);
                            Log.d(TAG, p.getName() + " " + p.getAge());
                        }
                    }
                });
    }

    public void onSubmitClick(View view) {
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText ageEditText = findViewById(R.id.ageEditText);

        String name = nameEditText.getText().toString();
        String ageString = ageEditText.getText().toString();
        int age = Integer.parseInt(ageString);

        Patient p = new Patient(name, age);
        Log.d(TAG, "Submitted name: " + p.getName() + ", age: " + p.getAge());
        mDb.collection(PATIENTS)
                .add(p)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Patient entry added successfully.");
                        Toast.makeText(FirestoreListActivity.this,
                                "Patient entry added!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Could not add patient!");
                        Toast.makeText(FirestoreListActivity.this,
                                "Failed to add patient!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
