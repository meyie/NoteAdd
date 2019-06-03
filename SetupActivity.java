package com.mcksfg.noteadd;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText editDisplayName;
    private CircleImageView displayImage;
    private static final int GALLERY_REQ = 1;
    private Uri mImageUri = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseusers;
    private StorageReference mStorageref;
    private MaterialSpinner spinner;

    private String[] departments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        spinner = findViewById(R.id.spinner);
        editDisplayName = findViewById(R.id.displayName);
        displayImage = findViewById(R.id.setupImageButton);
        mDatabaseusers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mStorageref = FirebaseStorage.getInstance().getReference().child("profile_image");

        mDatabaseusers.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.child("image").getValue().toString();
                Picasso.with(SetupActivity.this).load(imageUrl).placeholder(R.mipmap.defpho).into(displayImage);

                if(dataSnapshot.child("name").getValue() != null) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    editDisplayName.setText(name);
                }

                String departmentId = dataSnapshot.child("department").getValue().toString();
                // TODO: department güncellenecek.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        departments = new String[]{"Computer Engineering", "Civil Engineering"};
        spinner.setItems(departments);
    }

    public void profileImageButtonClicked(View view){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        startActivityForResult(chooserIntent,GALLERY_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQ && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                mImageUri = result.getUri();
                displayImage.setImageURI(mImageUri);
            } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }
    }
    public void doneButtonClicked(View view){
        final ProgressDialog progressDialog = new ProgressDialog(SetupActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        final String name = editDisplayName.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();
        final String departmentValue = departments[spinner.getSelectedIndex()];

        String departmentId = "";
        if(departmentValue.equals("Computer Engineering")) {
            departmentId = "bilgisayar_muhendisligi";
        } else if(departmentValue.equals("Civil Engineering")) {
            departmentId = "insaat_muhendisligi";
        }

        if(!TextUtils.isEmpty(name)){
            final String finalDepartmentId = departmentId;

            if(mImageUri != null) {
                StorageReference filepath = mStorageref.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String downloadurl = taskSnapshot.getDownloadUrl().toString();
                        mDatabaseusers.child(user_id).child("name").setValue(name);
                        mDatabaseusers.child(user_id).child("image").setValue(downloadurl);
                        mDatabaseusers.child(user_id).child("department").setValue(finalDepartmentId);

                        progressDialog.dismiss();

                        finish();
                        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
                        startActivity(mainIntent);
                    }
                });
            } else {
                mDatabaseusers.child(user_id).child("name").setValue(name);
                mDatabaseusers.child(user_id).child("department").setValue(finalDepartmentId);

                progressDialog.dismiss();

                finish();
                Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
                startActivity(mainIntent);
            }


        }
    }
}
