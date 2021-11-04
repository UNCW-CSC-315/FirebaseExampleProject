package edu.uncw.example.firebase;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends AppCompatActivity {

    private static final String TAG = "StorageActivity";
    private static final String CURRENT_PLANT = "currentPlant";

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private List<Plant> plantList = new ArrayList<>();
    private int mCurrentImage = 0;

    private TextView mPlantNameTextView;
    private ImageView mPlantImageView;

    private ImageView mImageThumbnail;
    private ConstraintLayout mImagePreviewSection;

    ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        mImagePreviewSection.setVisibility(View.VISIBLE);
                        // Handle the returned URI
                        ContentResolver cr = getBaseContext().getContentResolver();
                        String imageType = cr.getType(result).split("/")[1];
                        mImageThumbnail.setImageURI(result);
                        mImageThumbnail.setOnClickListener(v -> {
                                    EditText newPlantName = findViewById(R.id.newName);
                                    String name = newPlantName.getText().toString().trim();
                                    if (TextUtils.isEmpty(name)) {
                                        newPlantName.setError("You must provide a name");
                                    } else {
                                        Plant newPlant = new Plant(name, "images/" + name + "." + imageType);
                                        mDb.collection("plants")
                                                .add(newPlant).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "Plant entry added successfully.");
                                                mStorageRef.child(newPlant.getImageFile())
                                                        .putFile(result)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                        Toast.makeText(StorageActivity.this,
                                                                                "New plant picture saved!",
                                                                                Toast.LENGTH_LONG).show();
                                                                        loadPlants();
                                                                        mImagePreviewSection.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                        )
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(StorageActivity.this,
                                                                        "Could not save new plant!",
                                                                        Toast.LENGTH_LONG).show();
                                                                // TODO: Need to delete Firestore plant
                                                                documentReference.delete();
                                                            }
                                                        });
                                            }
                                        });

                                    }

                                }

                        );
                    }
                }
            });


    private void loadPlants() {
        // Download our Plant information from the Firestore, which contains the name of the plant
        // and its location in Storage
        mDb.collection("plants")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            plantList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Converts the Firestore documents into a Plant objects for
                                // easier manipulation in Java
                                Plant plant = document.toObject(Plant.class);
                                plantList.add(plant);
                            }
                            updateUI(mCurrentImage);
                        } else {
                            Log.w(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        mPlantImageView = findViewById(R.id.plant_image);
        mPlantNameTextView = findViewById(R.id.plant_name);

        loadPlants();


        // Clicking on a plant image will advance to the next picture.
        mPlantImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentImage = (mCurrentImage + 1) % plantList.size();
                updateUI(mCurrentImage);
            }
        });


        mImageThumbnail = findViewById(R.id.thumbnail);
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> {
            getContent.launch("image/*");
        });
        mImagePreviewSection = findViewById(R.id.imagePreview);


    }

    /**
     * display a plant's name and picture according to its index in plantList
     *
     * @param imageNum the index in plantList of the plant to show
     */
    private void updateUI(int imageNum) {
        if (plantList.isEmpty() || imageNum < 0 || imageNum > plantList.size()) {
            mPlantNameTextView.setText(R.string.no_plant);
            mPlantImageView.setVisibility(View.GONE);
        } else {
            mPlantNameTextView.setText(plantList.get(imageNum).getName());
            mPlantImageView.setContentDescription(String.format(getResources().getString(R.string.plant_image_description), plantList.get(imageNum).getName()));
            mPlantImageView.setVisibility(View.VISIBLE);
            StorageReference image = mStorageRef.child(plantList.get(imageNum).getImageFile());

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

        if (savedInstanceState != null) {
            mCurrentImage = savedInstanceState.getInt(CURRENT_PLANT);
        }
    }
}
