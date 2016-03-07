package zhenma.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    @JsonIgnoreProperties({ "friendlist" })
    public static class UserProfile {
        private String birthYear;
        private String email;
        private String label;
        private String number;
        private String position;
        private String username;

        public String getBirthYear() {
            return birthYear;
        }
        public String getEmail() {
            return email;
        }
        public String getLabel() {
            return label;
        }
        public String getNumber() {
            return number;
        }
        public String getPosition() {
            return position;
        }
        public String getUsername() {
            return username;
        }
    }

    private Uri mImageCaptureUri;
    private ImageView mImageView;
    // temp buffer for storing the image
    private byte[] mProfilePictureArray;

    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final String TMP_PROFILE_IMG_KEY = "saved_uri";

    private static final String IMAGE_UNSPECIFIED = "image/*";
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    public static final int REQUEST_CODE_CROP_PHOTO = 2;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private boolean isTakenFromCamera;

    private String id;

    private Firebase myFirebaseRef;

    // load image byte array to image view
    private void loadImageToView() {
        /*try {
            ByteArrayInputStream bis = new ByteArrayInputStream(
                    mProfilePictureArray);
            Bitmap bmap = BitmapFactory.decodeStream(bis);
            mImageView.setImageBitmap(bmap);
            bis.close();
        } catch (Exception ex) {
        }*/
        mImageView.setImageResource(R.drawable.default_profile);
    }
    // convert bitmap to byte array
    private void dumpImage(Bitmap bmap) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            mProfilePictureArray = bos.toByteArray();
            bos.close();
        } catch (IOException ioe) {

        }
    }

    //load profile from SharedPreferences
    private void loadProfile(String id) {

        myFirebaseRef.child("vertex").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
                //System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                //for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Contact data = new Contact();
                //    String friend_id = postSnapshot.getKey();

                String key, str_val;
                int int_val;

                // Load and update all profile views
                key = getString(R.string.preference_private);
                SharedPreferences prefs = getSharedPreferences(key, MODE_PRIVATE);

                UserProfile facts = snapshot.getValue(UserProfile.class);
                ((EditText) findViewById(R.id.editName)).setText(facts.getLabel());
                ((EditText) findViewById(R.id.editEmail)).setText(facts.getEmail());
                ((EditText) findViewById(R.id.editPhone)).setText(facts.getNumber());
                ((EditText) findViewById(R.id.editTittle)).setText(facts.getPosition());

                // Load profile photo from internal storage
                try {
                    // open the file using a file input stream
                    FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
                    // the file's data will be read into a bytearray output stream
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    // inputstream -> buffer -> outputstream
                    byte[] buffer = new byte[5 * 1024];
                    int n;
                    // read data in a while loop
                    while ((n = fis.read(buffer)) > -1) {
                        bos.write(buffer, 0, n); // Don't allow any extra bytes to creep
                        // in, final write
                    }
                    fis.close();
                    //get the byte array from the ByteArrayOutputStream
                    mProfilePictureArray = bos.toByteArray();
                    bos.close();
                } catch (IOException e) {
                    mImageView.setImageResource(R.drawable.default_profile);
                }

                // load the byte array to the image view
                loadImageToView();


                //System.out.println(facts.getLabel());
                    //data.setName(facts.getLabel());
                    //data.setPinyin(facts.getLabel());
                    //data.setUrl(id);
                    //datas.add(data);
                    //ids.add(friend_id);
                    //System.out.println(friend_id);
                //}
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });


        // Load user name
        // key = getString(R.string.preference_key_profile_name);
        // str_val = prefs.getString(key, "");
        // ((EditText) findViewById(R.id.editName)).setText(str_val);

        // Load user email
        //key = getString(R.string.preference_key_profile_email);
        //str_val = prefs.getString(key, "");
        //((EditText) findViewById(R.id.editEmail)).setText(str_val);

        // Load user phone number
        //key = getString(R.string.preference_key_profile_psw);
        //str_val = prefs.getString(key, "");
        //((EditText) findViewById(R.id.editPsw)).setText(str_val);

        // Load user phone number
        //key = getString(R.string.preference_key_profile_phone);
        //str_val = prefs.getString(key, "");
        //((EditText) findViewById(R.id.editPhone)).setText(str_val);

        // Load student major info
        //key = getString(R.string.preference_key_profile_tittle);
        //str_val = prefs.getString(key, "");
        //((EditText) findViewById(R.id.editTittle)).setText(str_val);


    }

    //save profile to SharedPreferences
    private void saveProfile(String id) {

        String key, str_val;
        int int_val;

        Firebase newRef = myFirebaseRef.child("vertex").child(id);

        key = getString(R.string.preference_private);
        SharedPreferences prefs = getSharedPreferences(key, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        // Write screen contents into corresponding editor fields.
        key = getString(R.string.preference_key_profile_name);
        str_val = ((EditText) findViewById(R.id.editName)).getText().toString();
        newRef.child("label").setValue(str_val);
        //editor.putString(key, str_val);

        key = getString(R.string.preference_key_profile_email);
        str_val = ((EditText) findViewById(R.id.editEmail)).getText()
                .toString();
        newRef.child("email").setValue(str_val);
        //editor.putString(key, str_val);

        key = getString(R.string.preference_key_profile_psw);
        str_val = ((EditText) findViewById(R.id.editPsw)).getText()
                .toString();
        editor.putString(key, str_val);

        key = getString(R.string.preference_key_profile_phone);
        str_val = ((EditText) findViewById(R.id.editPhone)).getText()
                .toString();
        newRef.child("number").setValue(str_val);
        //editor.putString(key, str_val);

        key = getString(R.string.preference_key_profile_tittle);
        str_val = ((EditText) findViewById(R.id.editTittle)).getText()
                .toString();
        newRef.child("position").setValue(str_val);
        //editor.putString(key, str_val);

        //editor.commit();
        // Save profile image into internal storage.
        try {
            // if the user did not change default profile
            // picture, mProfilePictureArray will be null

            FileOutputStream fos = openFileOutput(
                    getString(R.string.profile_photo_file_name), MODE_PRIVATE);
            fos.write(mProfilePictureArray);
            fos.flush();
            fos.close();

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        // Uri is a Parcelable object, so use putParcelable() to put it into the Bundle
        // see http://developer.android.com/reference/android/os/Bundle.html#putParcelable(java.lang.String, android.os.Parcelable)
        outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);

        // Save profile image into internal storage.
        if (mProfilePictureArray != null) {
            // use putByteArray() to put a byte array into a Bundle
            // see http://developer.android.com/reference/android/os/Bundle.html#putByteArray(java.lang.String, byte[])
            outState.putByteArray(TMP_PROFILE_IMG_KEY, mProfilePictureArray);
        }
    }

    // Crop and resize the image for profile
    private void cropImage() {
        // Use existing crop activity.
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);

        // Specify aspect ratio, 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        // REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
        // identify the activity in onActivityResult() when it returns
        startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA: {
                if (resultCode == RESULT_OK) {
                    cropImage();
                    break;
                }
                break;
            } // ACTION_TAKE_PHOTO_B
            case REQUEST_CODE_CROP_PHOTO: {
                Bundle extras = data.getExtras();
                if (extras!=null){
                    Bitmap photo = extras.getParcelable("data");
                    // load the byte array to the image view
                    dumpImage(photo);
                    loadImageToView();
                }
                if(isTakenFromCamera){
                    File f = new File(mImageCaptureUri.getPath());
                    if(f.exists())
                        f.delete();
                }
                break;
            }
        }
    }

    // generate a temp file uri for capturing profile photo
    private Uri getPhotoUri() {
        Uri photoUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), JPEG_FILE_PREFIX
                + String.valueOf(System.currentTimeMillis()) + JPEG_FILE_SUFFIX));

        return photoUri;
    }

    public void onChangePhotoClicked(View v) {
        // Take photo from camera，
        // Construct an intent with action
        // MediaStore.ACTION_IMAGE_CAPTURE

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct temporary image path and name to save the taken
        // photo
        mImageCaptureUri = getPhotoUri();

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                mImageCaptureUri);
        intent.putExtra("return-data", true);
        try {
            startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
        isTakenFromCamera = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        Intent intent = getIntent();
        id = "";
        id = intent.getStringExtra("UID");
        final String MY_UID = intent.getStringExtra("MY_UID");

        setContentView(R.layout.activity_profile);
        mImageView = (ImageView) findViewById(R.id.imageProfile);
        if(savedInstanceState != null)
            mImageCaptureUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
        myFirebaseRef = new Firebase("https://graphdata.firebaseio.com/");

        loadProfile(id);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Bundle extras = getIntent().getExtras();
        boolean fromList = false;
        boolean notyetaccept = false;
        if (extras != null){
            fromList = extras.getBoolean("FROM_LIST");
            notyetaccept = extras.getBoolean("NOTYETACCEPT");
        }

        if (fromList){
            if(!notyetaccept){
                CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                p.setAnchorId(View.NO_ID);
                fab.setLayoutParams(p);
                fab.setVisibility(View.GONE);
            }else{
                fab.setImageResource(R.drawable.ic_accept);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Firebase newRef = myFirebaseRef.child("vertex").child(MY_UID).child("friendlist").child(id);
                        newRef.setValue("0");
                        // Making a "toast" informing the user the profile is saved.
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.ui_profile_toast_accept_request),
                                Toast.LENGTH_SHORT).show();
                        // Close the activity
//                finish();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("UID", MY_UID);
                        startActivity(intent);
                    }
                });
            }
        }else{
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Save profile
                    saveProfile(id);
                    // Making a "toast" informing the user the profile is saved.
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.ui_profile_toast_save_text),
                            Toast.LENGTH_SHORT).show();
                    // Close the activity
//                finish();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.putExtra("UID", id);
                    startActivity(intent);
                }
            });
        }

    }
}
