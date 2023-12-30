package com.wecareapp.android.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.DialogFragment;/
//import android.support.v4.app.FragmentManager;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
//import com.firebase.client.ChildEventListener;
//import com.firebase.client.DataSnapshot;
//import com.firebase.client.Firebase;
//import com.firebase.client.FirebaseError;
//import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;
//import com.ortiz.touchview.TouchImageView;
//import com.versatilemobitech.vortex.R;
//import com.versatilemobitech.vortex.SampleBackgroundThreadPermissionListener;
//import com.versatilemobitech.vortex.SampleErrorListener;
//import com.versatilemobitech.vortex.SamplePermissionListener;
//import com.versatilemobitech.vortex.model.ChatWith;
//import com.versatilemobitech.vortex.model.StaffDetails;
//import com.versatilemobitech.vortex.utilities.Constants;
//import com.versatilemobitech.vortex.utilities.FileUtils;
//import com.versatilemobitech.vortex.utilities.PopUtilities;
//import com.versatilemobitech.vortex.utilities.ScalingUtilities;
//import com.versatilemobitech.vortex.utilities.StaticUtils;
//import com.versatilemobitech.vortex.utilities.Utility;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesChatUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 21;
//    private static final String TAG = com.versatilemobitech.vortex.activities.MessagesChatUserActivity.class.getSimpleName();
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton, uploadImage, hometab;
    EditText messageArea;
    //profiletab
    ScrollView scrollView;
//    Firebase reference1, reference2, reference3;
    String userId, doctor;
    ImageView ivBack;

//    @BindView(R.id.tvTitle)
//    TextView tvTitle;
//    @BindView(android.R.id.content)
//    View contentView;
//    @BindView(R.id.ivProfile)
//    CircleImageView ivProfile;

    //a Uri object to store file path
    /*private Uri filePath;
    private String opponentUserId;
    private String opponentUserType;
    private String userType;

    private PermissionListener cameraPermissionListener;
    private PermissionListener contactsPermissionListener;
    private PermissionRequestErrorListener errorListener;

    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private String selectedImagePath;
    private Bitmap bitmap;

//    FirebaseStorage storage = FirebaseStorage.getInstance();
//    StorageReference storageRef = storage.getReferenceFromUrl("gs://vortex-35279.appspot.com");
    private ProgressDialog progressDialog;
//    private StaffDetails staffDetails;
    private String opponentUserName;
    private String opponentUserProfilePhotoUrl;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_message_chat);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            opponentUserId = bundle.getString("userId", "");
            opponentUserType = bundle.getString("userType", "");
            staffDetails = (StaffDetails) bundle.getSerializable("staffDetails");
        }
        progressDialog = new ProgressDialog(this);

        Firebase.setAndroidContext(this);

        initUI();
        createPermissionListeners();
        initData();*/

    }/*

    private void initData() {
        if (staffDetails != null) {
            opponentUserName = staffDetails.getFullname();
            opponentUserProfilePhotoUrl = staffDetails.getProfileImage();
            Glide.with(this).load(opponentUserProfilePhotoUrl).into(ivProfile);
            tvTitle.setText(opponentUserName);
        }
    }

    private void initUI() {
        ivBack = findViewById(R.id.ivBack);
        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        hometab = findViewById(R.id.home);

        try {
            // Utility.showLoadingDialog(this, "loading", false);
            userId = Utility.getSharedPrefStringData(this, Constants.USER_ID); //userSession.getUserDetails().get("userid");//send userId not username
            userType = Utility.getSharedPrefStringData(this, Constants.USER_TYPE); //userSession.getUserDetails().get("userid");//send userId not username
            Log.e("USER", "" + userId);
            doctor = "doctor";//userSession.getUserDetails().get("userpassword")
            reference1 = new Firebase("https://vortex-35279.firebaseio.com/messages/" + userId + "_" + opponentUserId);//+ UserDetails.username + "_" + UserDetails.chatWith);//"https://vortex-35279.firebaseio.com/main"
            if (TextUtils.isEmpty(opponentUserType))
                reference2 = new Firebase("https://vortex-35279.firebaseio.com/messages/" + doctor);//+ UserDetails.chatWith + "_" + UserDetails.username);//"https://vortex-35279.firebaseio.com/versatile"
            else
                reference2 = new Firebase("https://vortex-35279.firebaseio.com/messages/" + opponentUserId + "_" + userId);//+ UserDetails.chatWith + "_" + UserDetails.username);//"https://vortex-35279.firebaseio.com/versatile"

            reference3 = new Firebase("https://vortex-35279.firebaseio.com/chat_with/");//+ UserDetails.chatWith + "_" + UserDetails.username);//"https://vortex-35279.firebaseio.com/versatile"
        } catch (Exception e) {
            e.printStackTrace();
        }

        ivBack.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> {
            String messageText = messageArea.getText().toString();

            if (!messageText.equals("")) {
//                Utility.hideKeyboard(MessageActivity.this);
                Map<String, String> map = new HashMap<>();
                map.put("message", messageText);
                map.put("message_type", "text");
                map.put("usertype", userType);//userSession.getUserDetails().get("userid"));//UserDetails.username);"hello"
                map.put("datetime", String.valueOf(System.currentTimeMillis()));
                map.put("chat_with_user_id", opponentUserId);
                map.put("chat_with_user_type", opponentUserType);
//                reference1.child("sss").child("rrr");
                reference1.push().setValue(map);
                reference2.push().setValue(map);

                updateChatWithUser(messageText, "text");

            }
            messageArea.setText("");
            messageArea.requestFocus();
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);

                String message = map.get("message") == null ? "" : map.get("message").toString();
                String userName = map.get("usertype") == null ? "" : map.get("usertype").toString();
                String datetime = map.get("datetime") == null ? "" : map.get("datetime").toString();
                Log.e("USERNAME1", "USERNAME1" + userName);
                Log.e("USERNAME2", "USERNAME2" + userId);
                if (userName.equals("user")) {
                    addMessageBox(message, 1, datetime, map);
                } else {
                    addMessageBox(message, 2, datetime, map);
                }
                Utility.hideLoadingDialog();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", firebaseError.toException());
            }
        });

    }

    private void updateChatWithUser(String messageText, String messageType) {
        ChatWith chatWith = new ChatWith();
        chatWith.setChatWithUserId(opponentUserId);
        chatWith.setChatWithUserType(opponentUserType);
        chatWith.setChatWithUserName(opponentUserName);
        chatWith.setChatWithUserProfilePhotoUrl(opponentUserProfilePhotoUrl);
        chatWith.setDateTime(String.valueOf(Calendar.getInstance().getTimeInMillis()));
        chatWith.setMessage(messageText);
        chatWith.setMessageType(messageType);

        reference3.child(userId).orderByChild("user_id").equalTo(opponentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    reference3.child(userId).child(opponentUserId).child("user_name").setValue(opponentUserName);
                    reference3.child(userId).child(opponentUserId).child("message").setValue(messageText);
                    reference3.child(userId).child(opponentUserId).child("date_time").setValue(String.valueOf(System.currentTimeMillis()));
//                            Toast.makeText(MessageActivity.this, "Exits", Toast.LENGTH_SHORT).show();
                } else {
//                            Toast.makeText(MessageActivity.this, "Not Exits", Toast.LENGTH_SHORT).show();
                    reference3.child(userId).child(opponentUserId).setValue(chatWith);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", firebaseError.toException());
            }
        });
    }

    public void addMessageBox(String message, int type, String datetime, Map map) {
        TextView textView = new TextView(com.versatilemobitech.vortex.activities.MessagesChatUserActivity.this);
        TextView textView2 = new TextView(com.versatilemobitech.vortex.activities.MessagesChatUserActivity.this);
        Calendar cal = Calendar.getInstance();
        if (!TextUtils.isEmpty(datetime))
            cal.setTimeInMillis(Long.parseLong(datetime));

        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        textView2.setText(formattedDate);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1.0f;
        lp.topMargin = 40;

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        lp2.topMargin = 20;

        textView2.setTextSize(12);
        textView2.setLayoutParams(lp2);

        String messageType = map.get("message_type") == null ? "" : map.get("message_type").toString();
        String url = map.get("file_url") == null ? "" : map.get("file_url").toString();
        String oppoUserType = map.get("usertype") == null ? "" : map.get("usertype").toString();
        if (userType.equalsIgnoreCase(oppoUserType))
            textView.setText(MessageFormat.format("Me: {0}", message));
        else
            textView.setText(MessageFormat.format("{0}: {1}", opponentUserName, message));

        switch (messageType) {
            case "image":
                ImageView imageView = new ImageView(com.versatilemobitech.vortex.activities.MessagesChatUserActivity.this);

                Glide.with(this).load(url).into(imageView);
                if (type == 1) {
                    Log.e("TYPE1", "" + type);
                    lp.gravity = Gravity.END;
                    lp2.gravity = Gravity.END;
                    imageView.setPadding(50, 30, 50, 30);
                    imageView.setBackgroundResource(R.drawable.message_right);
                } else {
                    Log.e("TYPE2", "" + type);
                    lp.gravity = Gravity.START;
                    lp2.gravity = Gravity.START;
                    imageView.setPadding(50, 30, 50, 30);
                    imageView.setBackgroundResource(R.drawable.message_left);
                }
//                imageView.setTag(url);
                imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(lp);
                imageView.setOnClickListener(v -> {

                    FragmentManager fm = getSupportFragmentManager();
                    EditNameDialogFragment editNameDialogFragment = EditNameDialogFragment.newInstance(url);
                    editNameDialogFragment.show(fm, "fragment_edit_name");

                    *//*Dialog settingsDialog = new Dialog(this);
                    settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    View view = getLayoutInflater().inflate(R.layout.layout_image, null);
                    settingsDialog.setContentView(view);
                    TouchImageView touchImageView = view.findViewById(R.id.imageView);
                    Glide.with(this).load(url).apply(new RequestOptions()
                            .placeholder(R.drawable.splash)
                            .fitCenter()).into(touchImageView);
                    settingsDialog.show();
                    Toast.makeText(this, url, Toast.LENGTH_SHORT).show();*//*
*//*
                    Dialog builder = new Dialog(this);
                    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    builder.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    builder.setOnDismissListener(dialogInterface -> {
                        //nothing;
                    });

                    TouchImageView touchImageView = new TouchImageView(this);
//                    imageView2.setImageURI(new Uri(url));
                    Glide.with(this).load(url).apply(new RequestOptions()
                            .placeholder(R.drawable.splash)
                            .fitCenter()).into(touchImageView);
                    builder.addContentView(touchImageView, new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();*//*
                });
                layout.addView(imageView);
                break;
            default:
                if (type == 1) {
                    Log.e("TYPE1", "" + type);
                    lp.gravity = Gravity.END;
                    lp2.gravity = Gravity.END;
                    textView.setTextColor(Color.parseColor("#666666"));
                    textView.setPadding(50, 20, 50, 20);
                    textView.setBackgroundResource(R.drawable.message_right);
                } else {
                    Log.e("TYPE2", "" + type);
                    lp.gravity = Gravity.START;
                    lp2.gravity = Gravity.START;
                    textView.setTextColor(Color.parseColor("#666666"));
                    textView.setPadding(50, 20, 50, 20);
                    textView.setBackgroundResource(R.drawable.message_left);
                }
                textView.setLayoutParams(lp);
                layout.addView(textView);
        }

        layout.addView(textView2);
        // scrollView.fullScroll(View.FOCUS_DOWN);
        //scrollView.scrollTo(0, scrollView.getBottom());
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void createPermissionListeners() {

        PermissionListener feedbackViewPermissionListener = new SamplePermissionListener(this);
        contactsPermissionListener = new CompositePermissionListener(feedbackViewPermissionListener,
                SnackbarOnDeniedPermissionListener.Builder.with(contentView,
                        R.string.contacts_permission_denied_feedback)
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());
        cameraPermissionListener = new SampleBackgroundThreadPermissionListener(this);

        errorListener = new SampleErrorListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.ivPin)
    public void onCameraPermissionButtonClicked() {
        *//*new Thread(new Runnable() {
            @Override
            public void run() {
                Dexter.withActivity(MessageActivity.this)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(cameraPermissionListener)
                        .withErrorListener(errorListener)
                        .onSameThread()
                        .check();
            }
        }).start();*//*

        if (!Utility.hasPermissions(this, PERMISSIONS)) {
            Log.e("First", "You Clicked on");
            requestPermissions(PERMISSIONS, 1);
        } else {
            Log.e("Second", "You Clicked on Second");
            PopUtilities.cameraDialog(this, CAMERACLICK, GALLERYCLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    token.cancelPermissionRequest();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    token.continuePermissionRequest();
                })
                .setOnDismissListener(dialog -> token.cancelPermissionRequest())
                .show();
    }

    public void showPermissionGranted(String permission) {

        switch (permission) {
            case Manifest.permission.CAMERA:
                Toast.makeText(this, "Granted C", Toast.LENGTH_SHORT).show();
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                Toast.makeText(this, "Granted S", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void showPermissionDenied(String permission, boolean isPermanentlyDenied) {
        Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PopUtilities.cameraDialog(this, CAMERACLICK, GALLERYCLICK);
                } else {
                    Toast.makeText(this, "Permission was denied. You can't access to camera or gallery", Toast.LENGTH_SHORT).show();
                }

                break;
            case 2:

                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
                }*//*else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }*//*
                break;

            default:
                break;
        }
    }

    View.OnClickListener CAMERACLICK = view -> {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.PHONE_CAMERA_CLICK);
    };

    View.OnClickListener GALLERYCLICK = view -> {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PHONE_GALLERY_CLICK);
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case Constants.PHONE_GALLERY_CLICK:
                Uri selectedImageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {
                    selectedImagePath = StaticUtils.getPath(this, selectedImageUri);
                    bitmap = BitmapFactory.decodeFile(selectedImagePath);
                    convertBitmapToFile(bitmap);
                } else {
                    ParcelFileDescriptor parcelFileDescriptor;
                    try {
                        parcelFileDescriptor = this.getContentResolver().openFileDescriptor(selectedImageUri, "r");
                        String imagePath = FileUtils.getPath(this, selectedImageUri);
                        bitmap = StaticUtils.getResizeImage(this,
                                StaticUtils.PROFILE_IMAGE_SIZE,
                                StaticUtils.PROFILE_IMAGE_SIZE,
                                ScalingUtilities.ScalingLogic.CROP,
                                true,
                                imagePath,
                                selectedImageUri);
//                        File sd = Environment.getExternalStorageDirectory();
//                        File image = new File(imagePath);
//                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

                        bitmap = BitmapFactory.decodeFile(imagePath);
//                        bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                        convertBitmapToFile(bitmap);
                        parcelFileDescriptor.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constants.PHONE_CAMERA_CLICK:
                if (data != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    convertBitmapToFile(bitmap);
                }
                break;

            default:
                break;
        }
    }

    private void convertBitmapToFile(Bitmap bitmap) {
        if (bitmap != null) {
            progressDialog.setMessage("Sending...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
            progressDialog.show();
            String file = UUID.randomUUID().toString() + ".jpg";
//            s = "kkk.jpg";
            Log.e("T", file + "");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            storageRef.child(file);
            final StorageReference ref = storageRef.child(file);
            UploadTask uploadTask = ref.putBytes(byteArrayOutputStream.toByteArray());
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                progressDialog.incrementProgressBy((int) progress);
                progressDialog.setProgress((int) progress);
            });
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    Objects.requireNonNull(task.getException()).printStackTrace();
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Map<String, String> map = new HashMap<>();
                    map.put("message", "");
                    map.put("message_type", "image");
                    map.put("usertype", userType);//userSession.getUserDetails().get("userid"));//UserDetails.username);"hello"
                    map.put("datetime", String.valueOf(System.currentTimeMillis()));
                    map.put("chat_with_user_id", opponentUserId);
                    map.put("chat_with_user_type", opponentUserId);
                    map.put("file_url", downloadUri.toString());
//                        databaseReference.push().setValue(message);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);

                    updateChatWithUser("", "image");
                } else {
                    // Handle failures
                    // ...
                }
            });
        }
    }

    public static class EditNameDialogFragment extends DialogFragment {

        public EditNameDialogFragment() {
            // Empty constructor is required for DialogFragment
            // Make sure not to add arguments to the constructor
            // Use `newInstance` instead as shown below
        }

        public static EditNameDialogFragment newInstance(String title) {
            EditNameDialogFragment frag = new EditNameDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_image, container);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // Get field from view
            TouchImageView touchImageView = view.findViewById(R.id.imageView);
            // Fetch arguments from bundle and set title
            String title = getArguments().getString("title", "Enter Name");
            getDialog().setTitle(title);
            // Show soft keyboard automatically and request focus to field
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setLayout(width, height);
            Glide.with(this).load(title).apply(new RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .fitCenter()).into(touchImageView);
//            Glide.with(this).load(title).into(touchImageView);
        }
    }*/
}