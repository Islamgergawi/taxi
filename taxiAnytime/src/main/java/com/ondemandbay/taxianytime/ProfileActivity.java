package com.ondemandbay.taxianytime;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.component.MyFontEdittextView;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.db.DBHelper;
import com.ondemandbay.taxianytime.models.User;
import com.ondemandbay.taxianytime.parse.AsyncTaskCompleteListener;
import com.ondemandbay.taxianytime.parse.MultiPartRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class ProfileActivity extends ActionBarBaseActivitiy implements
        OnClickListener, AsyncTaskCompleteListener {
    private final String TAG = "profileActivity";
    private MyFontTextView tvPopupMsg;
    private MyFontEdittextView etProfileFname, etProfileLName, etProfileEmail,
            etProfileNumber, etProfileAddress, etProfileBio, etProfileZipcode,
            etCurrentPassword, etNewPassword, etRetypePassword;
    private ImageView ivProfile, btnProfileEmailInfo;
    private DBHelper dbHelper;
    private Uri uri = null;
    private AQuery aQuery;
    private String profileImageData, loginType, filePath, profileImageFilePath;
    private PreferenceHelper preferenceHelper;
    private ImageOptions imageOptions;
    private PopupWindow registerInfoPopup;
    private MyFontTextView tvProfileFName, tvProfileLName;
    private LinearLayout llCurrentPassword, llNewPassword, llConfirmPassword;
    private int rotationAngle;
    private Bitmap photoBitmap;

    // private boolean isNewPwd = false, isConfirmPwd = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_profile);
        setTitle(getString(R.string.text_profile));
        setIconMenu(R.drawable.back);
        setIcon(R.drawable.edit_profile);
        btnEditProfile.setVisibility(View.GONE);
        btnNotification.setVisibility(View.VISIBLE);
        // actionBar.setTitle(getString(R.string.text_profile));
        // findViewById(R.id.llProfileSocial).setVisibility(View.GONE);
        // findViewById(R.id.etProfilePassword).setVisibility(View.GONE);
        // findViewById(R.id.tvProfileSubmit).setVisibility(View.GONE);
        findViewById(R.id.tvProfileCountryCode).setVisibility(View.GONE);
        // findViewById(R.id.llseprateView).setVisibility(View.GONE);
        llCurrentPassword = (LinearLayout) findViewById(R.id.llCurrentPassword);
        llNewPassword = (LinearLayout) findViewById(R.id.llNewPassword);
        llConfirmPassword = (LinearLayout) findViewById(R.id.llConfirmPassword);
        tvPopupMsg = (MyFontTextView) findViewById(R.id.tvPopupMsg);
        tvProfileFName = (MyFontTextView) findViewById(R.id.tvProfileFName);
        tvProfileLName = (MyFontTextView) findViewById(R.id.tvProfileLName);
        etProfileFname = (MyFontEdittextView) findViewById(R.id.etProfileFName);
        etProfileLName = (MyFontEdittextView) findViewById(R.id.etProfileLName);
        etProfileEmail = (MyFontEdittextView) findViewById(R.id.etProfileEmail);
        etCurrentPassword = (MyFontEdittextView) findViewById(R.id.etCurrentPassword);
        etNewPassword = (MyFontEdittextView) findViewById(R.id.etNewPassword);
        etRetypePassword = (MyFontEdittextView) findViewById(R.id.etRetypePassword);
        etProfileNumber = (MyFontEdittextView) findViewById(R.id.etProfileNumber);
        etProfileBio = (MyFontEdittextView) findViewById(R.id.etProfileBio);
        etProfileAddress = (MyFontEdittextView) findViewById(R.id.etProfileAddress);
        etProfileZipcode = (MyFontEdittextView) findViewById(R.id.etProfileZipCode);
        // RelativeLayout
        // relProfileImage=(RelativeLayout)findViewById(R.id.relProfileImage);
        // tvProfileSubmit = (MyFontButton) findViewById(R.id.tvProfileSubmit);
        ivProfile = (ImageView) findViewById(R.id.ivProfileProfile);
        btnProfileEmailInfo = (ImageView) findViewById(R.id.btnProfileEmailInfo);
        btnProfileEmailInfo.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        // tvProfileSubmit.setOnClickListener(this);
        // tvProfileSubmit.setText(getResources().getString(
        // R.string.text_edit_profile));

        btnEditProfile.setOnClickListener(this);
        preferenceHelper = new PreferenceHelper(this);
        // socialId = preferenceHelper.getSocialId();
        loginType = preferenceHelper.getLoginBy();

        AppLog.Log(Const.TAG, "Login type==+> " + loginType);
        if (loginType.equals(Const.MANUAL)) {

            llCurrentPassword.setVisibility(View.VISIBLE);
            llNewPassword.setVisibility(View.VISIBLE);
            llConfirmPassword.setVisibility(View.VISIBLE);
            // etCurrentPassword.setVisibility(View.VISIBLE);
            // etNewPassword.setVisibility(View.VISIBLE);
            // etRetypePassword.setVisibility(View.VISIBLE);

            // etCurrentPassword.setText(preferenceHelper.getPassword());

        }

        aQuery = new AQuery(this);
        disableViews();
        imageOptions = new ImageOptions();
        imageOptions.memCache = true;
        imageOptions.fileCache = true;
        imageOptions.targetWidth = 200;
        imageOptions.fallback = R.drawable.default_user;
        setData();

        // popup
        LayoutInflater inflate = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflate.inflate(
                R.layout.popup_info_window, null);
        tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
        registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        layout.setOnClickListener(this);
        registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
        registerInfoPopup.setOutsideTouchable(true);

        etProfileFname.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                tvProfileFName.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etProfileLName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                tvProfileLName.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    // @Override
    // protected void onResume() {
    // super.onResume();
    // setData();
    // }

    private void disableViews() {
        etProfileFname.setEnabled(false);
        etProfileLName.setEnabled(false);
        etProfileEmail.setEnabled(false);
        etProfileNumber.setEnabled(false);
        // etProfileBio.setEnabled(false);
        // etProfileAddress.setEnabled(false);
        // etProfileZipcode.setEnabled(false);
        etCurrentPassword.setEnabled(false);
        etNewPassword.setEnabled(false);
        etRetypePassword.setEnabled(false);
        ivProfile.setEnabled(false);
    }

    private void enableViews() {
        etProfileFname.setEnabled(true);
        etProfileLName.setEnabled(true);
        // etProfileEmail.setEnabled(true);
        etProfileNumber.setEnabled(true);
        // etProfileBio.setEnabled(true);
        // etProfileAddress.setEnabled(true);
        // etProfileZipcode.setEnabled(true);
        etCurrentPassword.setEnabled(true);
        etNewPassword.setEnabled(true);
        etRetypePassword.setEnabled(true);
        ivProfile.setEnabled(true);
    }

    private void setData() {
        dbHelper = new DBHelper(getApplicationContext());
        final User user = dbHelper.getUser();
        if (user != null) {
            aQuery.id(ivProfile).progress(R.id.pBar)
                    .image(user.getPicture(), imageOptions);
            tvProfileFName.setText(user.getFname());
            tvProfileLName.setText(user.getLname());
            etProfileFname.setText(user.getFname());
            etProfileLName.setText(user.getLname());
            etProfileEmail.setText(user.getEmail());
            etProfileNumber.setText(user.getContact());
        }
        // etProfileBio.setText(user.getBio());
        // etProfileAddress.setText(user.getAddress());
        // etProfileZipcode.setText(user.getZipcode());
        aQuery.id(R.id.ivProfileProfile).image(user.getPicture(), true, true,
                200, 0, new BitmapAjaxCallback() {

                    @Override
                    public void callback(String url, ImageView iv, Bitmap bm,
                                         AjaxStatus status) {
                        if (url != null && aQuery.getCachedFile(url) != null) {
                            AppLog.Log(TAG, "URL FROM AQUERY::" + url);
                            profileImageData = aQuery.getCachedFile(url)
                                    .getPath();
                            AppLog.Log(TAG, "URL path FROM AQUERY::" + url);
                            iv.setImageBitmap(bm);
                        }
                    }
                });

    }

    private void onUpdateButtonClick() {

        if (etProfileFname.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.text_enter_name), this);
            return;
        } else if (etProfileLName.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.text_enter_lname), this);
            return;
        } else if (etProfileEmail.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.text_enter_email), this);
            return;
        } else if (!AndyUtils
                .eMailValidation(etProfileEmail.getText().toString())) {
            AndyUtils.showToast(
                    getResources().getString(R.string.text_enter_valid_email),
                    this);
            return;
        } else if (etCurrentPassword.getVisibility() == View.VISIBLE) {

            if (!(TextUtils.isEmpty(etRetypePassword.getText()))) {

                if (TextUtils.isEmpty(etCurrentPassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_password), this);
                    return;
                } else if (etCurrentPassword.getText().length() < 6) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_valid_password), this);
                    return;
                } else if (TextUtils.isEmpty(etNewPassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_newpassword), this);
                    return;
                } else if (etNewPassword.getText().length() < 6) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_valid_password), this);
                    return;
                } else if (!(etRetypePassword.getText().toString()
                        .equals(etNewPassword.getText().toString()))) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_mismatch_password), this);
                    return;
                }
            } else if (!TextUtils.isEmpty(etCurrentPassword.getText())) {
                if (TextUtils.isEmpty(etRetypePassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_retypepassword), this);
                    return;
                } else if (etRetypePassword.getText().length() < 6) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_valid_password), this);
                    return;
                }
            }
        } else if (etCurrentPassword.getVisibility() == View.INVISIBLE) {
            etNewPassword.setVisibility(View.INVISIBLE);
            etRetypePassword.setVisibility(View.INVISIBLE);
        }

        if (etProfileNumber.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.text_enter_number), this);
            return;
        }
        // else if (profileImageData == null || profileImageData.equals("")) {
        // AndyUtils.showToast(
        // getResources().getString(R.string.text_pro_pic), this);
        // return;
        // }
        else {
            updateSimpleProfile(loginType);
        }
    }

    private void updateSimpleProfile(String type) {

        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.dialog_no_inter_message),
                    this);
            return;
        }

        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.progress_update_profile),
                false, null);

        if (type.equals(Const.MANUAL)) {
            AppLog.Log(TAG, "Simple Profile update method");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.URL, Const.ServiceType.UPDATE_PROFILE);
            map.put(Const.Params.ID, preferenceHelper.getUserId());
            map.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            map.put(Const.Params.FIRSTNAME, etProfileFname.getText().toString()
                    .trim());
            map.put(Const.Params.LAST_NAME, etProfileLName.getText().toString()
                    .trim());
            map.put(Const.Params.EMAIL, etProfileEmail.getText().toString());
            map.put(Const.Params.OLD_PASSWORD, etCurrentPassword.getText()
                    .toString().trim());
            map.put(Const.Params.NEW_PASSWORD, etNewPassword.getText()
                    .toString().trim());
            map.put(Const.Params.PICTURE, profileImageData);
            map.put(Const.Params.PHONE, etProfileNumber.getText().toString()
                    .trim());
            // map.put(Const.Params.BIO, etProfileBio.getText().toString());
            // map.put(Const.Params.ADDRESS,
            // etProfileAddress.getText().toString());
            // map.put(Const.Params.STATE, "");
            // map.put(Const.Params.COUNTRY, "");
            // map.put(Const.Params.ZIPCODE,
            // etProfileZipcode.getText().toString()
            // .trim());
            new MultiPartRequester(this, map, Const.ServiceCode.UPDATE_PROFILE,
                    this);
        } else {
            updateSocialProfile(type);
        }
    }

    private void updateSocialProfile(String loginType) {
        AppLog.Log(TAG, "profile social update  method");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.UPDATE_PROFILE);
        map.put(Const.Params.ID, preferenceHelper.getUserId());
        map.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(Const.Params.FIRSTNAME, etProfileFname.getText().toString()
                .trim());
        map.put(Const.Params.LAST_NAME, etProfileLName.getText().toString()
                .trim());
        map.put(Const.Params.ADDRESS, etProfileAddress.getText().toString()
                .trim());
        map.put(Const.Params.EMAIL, etProfileEmail.getText().toString().trim());
        map.put(Const.Params.PHONE, etProfileNumber.getText().toString().trim());
        map.put(Const.Params.PICTURE, profileImageData);
        map.put(Const.Params.STATE, "");
        map.put(Const.Params.COUNTRY, "");
        map.put(Const.Params.BIO, etProfileBio.getText().toString().trim());
        map.put(Const.Params.ZIPCODE, etProfileZipcode.getText().toString()
                .trim());
        new MultiPartRequester(this, map, Const.ServiceCode.UPDATE_PROFILE,
                this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ivProfileProfile:
                showPictureDialog();
                break;
            case R.id.btnActionNotification:
                btnEditProfile.setVisibility(View.VISIBLE);
                btnNotification.setVisibility(View.GONE);
                enableViews();
                etProfileFname.requestFocus();
                break;

            case R.id.btnEditProfile:
                onUpdateButtonClick();
                break;

            case R.id.btnActionMenu:
                InputMethodManager imm = (InputMethodManager) this
                        .getSystemService(ProfileActivity.INPUT_METHOD_SERVICE);
                if (imm.isAcceptingText()) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                onBackPressed();
                break;
            case R.id.btnProfileEmailInfo:
                if (registerInfoPopup.isShowing())
                    registerInfoPopup.dismiss();
                else {
                    registerInfoPopup.showAsDropDown(btnProfileEmailInfo);
                    tvPopupMsg.setText(getString(R.string.text_profile_popup));
                }
                break;

            default:
                break;
        }
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getResources().getString(
                R.string.text_choosepicture));
        String[] pictureDialogItems = {
                getResources().getString(R.string.text_gallary),
                getResources().getString(R.string.text_camera)};

        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0:
                                choosePhotoFromGallary();
                                break;

                            case 1:
                                takePhotoFromCamera();
                                break;

                        }
                    }
                });
        pictureDialog.show();
    }

    private void choosePhotoFromGallary() {

        // Intent intent = new Intent();
        // intent.setType("image/*");
        // intent.setAction(Intent.ACTION_GET_CONTENT);
        // intent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, Const.CHOOSE_PHOTO);

    }

    private void takePhotoFromCamera() {
        Calendar cal = Calendar.getInstance();
        File file = new File(Environment.getExternalStorageDirectory(),
                (cal.getTimeInMillis() + ".jpg"));

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uri = Uri.fromFile(file);
        if (uri == null) {
            // AndyUtils.showToast("Null", getApplicationContext());
        }
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, Const.TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Const.CHOOSE_PHOTO) {
            if (data != null) {
                Uri uri = data.getData();

                AppLog.Log(TAG, "Choose photo on activity result");

                profileImageFilePath = getRealPathFromURI(uri);
                filePath = profileImageFilePath;
                try {
                    int mobile_width = 480;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    int outWidth = options.outWidth;
                    int ratio = (int) ((((float) outWidth) / mobile_width) + 0.5f);

                    if (ratio == 0) {
                        ratio = 1;
                    }
                    ExifInterface exif = new ExifInterface(filePath);

                    String orientString = exif
                            .getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer
                            .parseInt(orientString)
                            : ExifInterface.ORIENTATION_NORMAL;

                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                        rotationAngle = 90;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                        rotationAngle = 180;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                        rotationAngle = 270;

                    System.out.println("Rotation : " + rotationAngle);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = ratio;

                    photoBitmap = BitmapFactory.decodeFile(filePath, options);
                    if (photoBitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(rotationAngle,
                                (float) photoBitmap.getWidth() / 2,
                                (float) photoBitmap.getHeight() / 2);
                        photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0,
                                photoBitmap.getWidth(),
                                photoBitmap.getHeight(), matrix, true);

                        AppLog.Log(TAG, "Take photo on activity result");
                        String path = Images.Media.insertImage(
                                getContentResolver(), photoBitmap, Calendar
                                        .getInstance().getTimeInMillis()
                                        + ".jpg", null);

                        beginCrop(Uri.parse(path));

                    }
                } catch (OutOfMemoryError e) {
                    System.out.println("out of bound");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.toast_unable_to_selct_image),
                        Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == Const.TAKE_PHOTO) {

            if (uri != null) {
                String imageFilePath = uri.getPath();
                if (imageFilePath != null && imageFilePath.length() > 0) {
                    // File myFile = new File(imageFilePath);
                    try {
                        // if (bmp != null)
                        // bmp.recycle();
                        int mobile_width = 480;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        // options.inJustDecodeBounds = true;
                        // BitmapFactory.decodeFile(imageFilePath, options);
                        int outWidth = options.outWidth;
                        int outHeight = options.outHeight;
                        int ratio = (int) ((((float) outWidth) / mobile_width) + 0.5f);

                        if (ratio == 0) {
                            ratio = 1;
                        }
                        ExifInterface exif = new ExifInterface(imageFilePath);

                        String orientString = exif
                                .getAttribute(ExifInterface.TAG_ORIENTATION);
                        int orientation = orientString != null ? Integer
                                .parseInt(orientString)
                                : ExifInterface.ORIENTATION_NORMAL;
                        System.out.println("Orientation : " + orientation);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                            rotationAngle = 90;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                            rotationAngle = 180;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                            rotationAngle = 270;

                        System.out.println("Rotation : " + rotationAngle);

                        options.inJustDecodeBounds = false;
                        options.inSampleSize = ratio;

                        Bitmap bmp = BitmapFactory.decodeFile(imageFilePath,
                                options);
                        File myFile = new File(imageFilePath);
                        // bmp = new ImageHelper().decodeFile(myFile);
                        FileOutputStream outStream = new FileOutputStream(
                                myFile);
                        if (bmp != null) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100,
                                    outStream);
                            outStream.flush();
                            outStream.close();

                            Matrix matrix = new Matrix();
                            matrix.setRotate(rotationAngle,
                                    (float) bmp.getWidth() / 2,
                                    (float) bmp.getHeight() / 2);

                            bmp = Bitmap.createBitmap(bmp, 0, 0,
                                    bmp.getWidth(), bmp.getHeight(), matrix,
                                    true);

                            // ivStuffPicture.setImageBitmap(bmp);

                            String path = Images.Media.insertImage(
                                    getContentResolver(), bmp, Calendar
                                            .getInstance().getTimeInMillis()
                                            + ".jpg", null);
                            beginCrop(Uri.parse(path));
                        }
                        // AQuery aQuery = new AQuery(this);
                        // aQuery.id(ivProfile).image(bmp);
                        //
                        // filePath = imageFilePath;
                        // rlDescription.setVisibility(View.VISIBLE);
                        // llPicture.setVisibility(View.GONE);
                    } catch (OutOfMemoryError e) {
                        System.out.println("out of bound");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.toast_unable_to_selct_image),
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            AppLog.Log(TAG, "Crop photo on activity result");
            handleCrop(resultCode, data);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null,
                null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            try {
                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            } catch (Exception e) {
                AndyUtils
                        .showToast(
                                getResources().getString(
                                        R.string.text_error_get_image), this);
                result = "";
            }
            cursor.close();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.uberdriverforx.parse.AsyncTaskCompleteListener#onTaskCompleted(java
     * .lang.String, int)
     */
    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        AppLog.Log(TAG, response);
        switch (serviceCode) {
            case Const.ServiceCode.UPDATE_PROFILE:
                AndyUtils.removeCustomProgressDialog();
                if (new ParseContent(this).isSuccessWithStoreId(response)) {
                    Toast.makeText(this, getString(R.string.toast_update_success),
                            Toast.LENGTH_SHORT).show();
                    new DBHelper(this).deleteUser();
                    new ParseContent(this).parseUserAndStoreToDb(response);
                    new PreferenceHelper(this).putPassword(etCurrentPassword
                            .getText().toString());
                    btnEditProfile.setVisibility(View.GONE);
                    btnNotification.setVisibility(View.VISIBLE);
                    onBackPressed();
                } else {
                    Toast.makeText(this, getString(R.string.toast_update_failed),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.ActionBarBaseActivitiy#isValidate()
     */
    @Override
    protected boolean isValidate() {
        return false;
    }

    private void beginCrop(Uri source) {
        // Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
        // "cropped"));
        Uri outputUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), (Calendar.getInstance()
                .getTimeInMillis() + ".jpg")));
        Crop.of(source, outputUri).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            AppLog.Log(Const.TAG, "Handle crop");
            profileImageData = getRealPathFromURI(Crop.getOutput(result));
            ivProfile.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}