package edu.uncw.example.firebase;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends AppCompatActivity {

    private static final String TAG = "StorageActivity";
    private static final String CURRENT_PLANT = "currentPlant";

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private List<Plant> plantData = new ArrayList<>();
    private int mCurrentImage = 0;

    private TextView mPlantNameTextView;
    private ImageView mPlantImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        mPlantImageView = findViewById(R.id.plant_image);
        mPlantNameTextView = findViewById(R.id.plant_name);

        // Download our Plant information from the Firestore, which contains the name of the plant
        // and its location in Storage
        mDb.collection("plants")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            plantData = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Converts the Firestore documents into a Plant objects for
                                // easier manipulation in Java
                                Plant plant = document.toObject(Plant.class);
                                plantData.add(plant);
                            }
                            updateUI(mCurrentImage);
                        } else {
                            Log.w(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });


        // Clicking on a plant image will advance to the next picture.
        mPlantImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentImage = (mCurrentImage + 1) % plantData.size();
                updateUI(mCurrentImage);
            }
        });

    }

    /**
     * display a plant's name and picture according to its index in plantData
     *
     * @param imageNum the index in plantData of the plant to show
     */
    private void updateUI(int imageNum) {
        if(plantData.isEmpty() || imageNum < 0 || imageNum > plantData.size()) {
            mPlantNameTextView.setText(R.string.no_plant);
            mPlantImageView.setVisibility(View.GONE);
        }
        else {
            mPlantNameTextView.setText(plantData.get(imageNum).getName());
            mPlantImageView.setContentDescription(String.format(getResources().getString(R.string.plant_image_description), plantData.get(imageNum).getName()));
            mPlantImageView.setVisibility(View.VISIBLE);
            StorageReference image = mStorageRef.child(plantData.get(imageNum).getImageFile());

            // Glide is a 3rd party library that simplifies image downloading, caching,
            // and injection into your UI. This tells Glide to load the image from Storage and
            // put it in the ImageView when complete.
            GlideApp.with(StorageActivity.this)
                    .load(image)
                    .into(mPlantImageView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(mCurrentImage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(CURRENT_PLANT, mCurrentImage);
        super.onSaveInstanceState(outState, outPersistentState);
        GlideApp.with(this).pauseAllRequests();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        GlideApp.with(this).resumeRequests();

        if(savedInstanceState != null) {
            mCurrentImage = savedInstanceState.getInt(CURRENT_PLANT);
        }
    }
}
