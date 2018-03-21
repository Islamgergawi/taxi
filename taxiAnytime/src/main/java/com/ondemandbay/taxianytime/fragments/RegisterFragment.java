package com.ondemandbay.taxianytime.fragments;

import android.Manifest;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
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
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.parse.MultiPartRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.soundcloud.android.crop.Crop;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Profile.Properties;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author Elluminati elluminati.in
 */
public class RegisterFragment extends BaseFragmentRegister
        implements
        com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final int MY_REQUEST_CODE = 9;
    ArrayList<String> list;
    // private MyFontTextView tvPopupMsg;
    ImageButton btnClickPhoto, btnPhotoFromGalary;
    Permission[] facebookPermission = new Permission[]{Permission.EMAIL};
    private Button btnNext;
    private ImageButton btnGPlush, btnFb;
    private EditText etFName, etLName, etEmail, etZipCode, etAddress,
            etPassword, etNumber;
    private ImageView ivProPic, ivPassword;
    private TextView tvPassword;
    // Gplus
    private ConnectionResult mConnectionResult;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private Uri uri = null;
    private String filePath = null, profileImageFilePath;
    private SimpleFacebook mSimpleFacebook;
    private TextView spCCode;
    private ParseContent pContent;
    private String type = Const.MANUAL;
    private String socialId;
    private String socialUrl;
    private String country;
    private PopupWindow registerInfoPopup;
    private int rotationAngle;
    private Bitmap photoBitmap;
    private SimpleFacebookConfiguration facebookConfiguration;

    // private ImageView btnRegisterEmailInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity.actionBar.show();
        Scope scope = new Scope("https://www.googleapis.com/auth/plus.login");
        // Scope scopePro = new
        // Scope("https://www.googleapis.com/auth/plus.me");
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, PlusOptions.builder().build())
                .addScope(scope).build();
        country = Locale.getDefault().getDisplayCountry();

//        facebookConfiguration = new SimpleFacebookConfiguration.Builder()
//                .setAppId(getResources().getString(R.string.applicationId))
//                .setNamespace(getResources().getString(R.string.app_name))
//                .setPermissions(facebookPermission).build();
//        SimpleFacebook.setConfiguration(facebookConfiguration);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(getResources().getString(R.string.text_register));
        activity.setIconMenu(R.drawable.close_payment);
        activity.btnActionMenu.setOnClickListener(this);
        activity.btnNotification.setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.register, container, false);
        btnNext = (Button) view.findViewById(R.id.btnNext);
        btnGPlush = (ImageButton) view.findViewById(R.id.btnGplus);
        btnFb = (ImageButton) view.findViewById(R.id.btnFb);
        tvPassword = (TextView) view.findViewById(R.id.tvPassword);
        ivPassword = (ImageView) view.findViewById(R.id.ivPassword);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etFName = (EditText) view.findViewById(R.id.etFName);
        etLName = (EditText) view.findViewById(R.id.etLName);
        // etBio = (EditText) view.findViewById(R.id.etBio);
        etNumber = (EditText) view.findViewById(R.id.etNumber);
        etZipCode = (EditText) view.findViewById(R.id.etZipCode);
        etAddress = (EditText) view.findViewById(R.id.etAddress);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        ivProPic = (ImageView) view.findViewById(R.id.ivChooseProPic);
        spCCode = (TextView) view.findViewById(R.id.spCCode);
        btnClickPhoto = (ImageButton) view.findViewById(R.id.btnClickPhoto);
        btnPhotoFromGalary = (ImageButton) view
                .findViewById(R.id.btnPhotoFromGalary);
        // btnRegisterEmailInfo = (ImageView) view
        // .findViewById(R.id.btnRegisterEmailInfo);
        // btnRegisterEmailInfo.setOnClickListener(this);

        spCCode.setOnClickListener(this);
        btnClickPhoto.setOnClickListener(this);
        btnPhotoFromGalary.setOnClickListener(this);
        btnGPlush.setOnClickListener(this);
        btnFb.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pContent = new ParseContent(activity);
        AppLog.Log(Const.TAG, country);
        list = pContent.parseCountryCodes();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(country)) {
                spCCode.setText((list.get(i).substring(0,
                        list.get(i).indexOf(" "))));
            }
        }
        if (TextUtils.isEmpty(spCCode.getText())) {
            spCCode.setText((list.get(0).substring(0, list.get(0).indexOf(" "))));
        }
        // popup
        LayoutInflater inflate = LayoutInflater.from(activity);
        RelativeLayout layout = (RelativeLayout) inflate.inflate(
                R.layout.popup_info_window, null);
        // tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
        registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        layout.setOnClickListener(this);
        registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
        registerInfoPopup.setOutsideTouchable(true);

    }

    @Override
    public void onResume() {
        activity.currentFragment = Const.FRAGMENT_REGISTER;
        super.onResume();
        activity.actionBar.show();
        activity.actionBar.setTitle(getString(R.string.text_register_small));
//        mSimpleFacebook = SimpleFacebook.getInstance(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFb:
                socialUrl = null;
                if (!mSimpleFacebook.isLogin()) {
                    activity.setFbTag(Const.FRAGMENT_REGISTER);
                    mSimpleFacebook.login(new OnLoginListener() {
                        @Override
                        public void onException(Throwable throwable) {

                        }

                        @Override
                        public void onFail(String reason) {
                            Toast.makeText(
                                    activity,
                                    getString(R.string.toast_facebook_login_failed) + "\n" + reason,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                            Toast.makeText(activity, getString(R.string.text_success), Toast.LENGTH_SHORT)
                                    .show();
                            Log.d("tag", "onLogin");
                        }

                        @Override
                        public void onCancel() {

                        }

                      /*  @Override
                        public void onFail(String arg0) {
                            Toast.makeText(
                                    activity,
                                    getString(R.string.toast_facebook_login_failed),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable arg0) {

                        }

                        @Override
                        public void onThinking() {

                        }

                        @Override
                        public void onNotAcceptingPermissions(Type arg0) {
                            Log.w("UBER",
                                    String.format(
                                            "You didn't accept %s permissions",
                                            arg0.name()));
                        }

                        @Override
                        public void onLogin() {

                        }*/
                    });
                } else {
                    getProfile();
                }
                break;
            case R.id.btnGplus:
                mSignInClicked = true;
                if (!mGoogleApiClient.isConnecting()) {
                    AndyUtils.showCustomProgressDialog(activity,
                            getString(R.string.text_getting_info), true, null);
                    mGoogleApiClient.connect();
                }
                break;
            case R.id.btnNext:

                if (isValidate()) {

                    register(type, socialId);

                }
                break;

            case R.id.btnClickPhoto:
                takePhotoFromCamera();
                break;

            case R.id.btnPhotoFromGalary:
                choosePhotoFromGallary();
                break;

            case R.id.spCCode:
                AlertDialog.Builder countryBuilder = new Builder(activity);
                countryBuilder.setTitle(getString(R.string.text_country_code));

                final String[] array = new String[list.size()];
                list.toArray(array);
                countryBuilder.setItems(array,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                spCCode.setText(array[which].substring(0,
                                        array[which].indexOf(" ")));
                            }
                        }).show();
                break;
            // case R.id.btnRegisterEmailInfo:
            // if (registerInfoPopup.isShowing())
            // registerInfoPopup.dismiss();
            // else {
            // registerInfoPopup.showAsDropDown(btnRegisterEmailInfo);
            // // tvPopupMsg.setText(msg);
            // }
            //
            // break;

            case R.id.btnActionMenu:
                OnBackPressed();
                break;

            default:
                break;
        }
    }

    // private void showPictureDialog() {
    // AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    // dialog.setTitle(getString(R.string.text_choosepicture));
    // String[] items = { getString(R.string.text_gallary),
    // getString(R.string.text_camera) };
    //
    // dialog.setItems(items, new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // switch (which) {
    // case 0:
    // choosePhotoFromGallary();
    // break;
    // case 1:
    // takePhotoFromCamera();
    // break;
    //
    // }
    // }
    // });
    // dialog.show();
    // }

    private void resolveSignInError() {

        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                activity.startIntentSenderForResult(mConnectionResult
                                .getResolution().getIntentSender(), RC_SIGN_IN, null,
                        0, 0, 0, Const.FRAGMENT_REGISTER);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the
            // user clicks
            // 'sign-in'.

            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all

                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        AndyUtils.removeCustomProgressDialog();
        mSignInClicked = false;
        btnGPlush.setEnabled(false);
        btnFb.setEnabled(false);
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);

        String personName = currentPerson.getDisplayName();

        String personPhoto = currentPerson.getImage().toString();
        socialId = currentPerson.getId();
        // etPassword.setEnabled(false);
        etPassword.setVisibility(View.GONE);
        tvPassword.setVisibility(View.GONE);
        ivPassword.setVisibility(View.GONE);
        etEmail.setText(email);
        type = Const.SOCIAL_GOOGLE;
        // etFName.setText(personName);
        if (personName.contains(" ")) {
            String[] split = personName.split(" ");
            etFName.setText(split[0]);
            etLName.setText(split[1]);
        } else {
            etFName.setText(personName);
        }
        if (!TextUtils.isEmpty(personPhoto)
                || !personPhoto.equalsIgnoreCase("null")) {
            try {
                AppLog.Log("Person photo-----", personPhoto + "-----");
                socialUrl = new JSONObject(personPhoto).getString("url");
                AppLog.Log("Person photo-----", socialUrl + "-----");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AQuery aQuery = new AQuery(activity);
            aQuery.id(ivProPic).image(socialUrl, getAqueryOption());
        } else {
            socialUrl = null;
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode != Activity.RESULT_OK) {
                    mSignInClicked = false;
                    AndyUtils.removeCustomProgressDialog();
                }

                mIntentInProgress = false;

                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;

            default:
                mSimpleFacebook.onActivityResult(requestCode, resultCode,
                        data);
                if (mSimpleFacebook.isLogin()) {
                    getProfile();
                } else {
                    Toast.makeText(activity, getString(R.string.error_facebook_login_failed),
                            Toast.LENGTH_SHORT).show();
                }

                super.onActivityResult(requestCode, resultCode, data);
                break;
            case Const.CHOOSE_PHOTO:
                if (data != null) {
                    Uri uri = data.getData();

                    AppLog.Log("RegisterFragment",
                            "Choose photo on activity result");

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

                            AppLog.Log("RegisterFragment",
                                    "Take photo on activity result");
                            String path = Images.Media.insertImage(
                                    activity.getContentResolver(), photoBitmap,
                                    Calendar.getInstance().getTimeInMillis()
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
                            activity,
                            getResources().getString(
                                    R.string.toast_unable_to_selct_image),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case Const.TAKE_PHOTO:
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
                            // int outHeight = options.outHeight;
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
                                        activity.getContentResolver(), bmp,
                                        Calendar.getInstance().getTimeInMillis()
                                                + ".jpg", null);
                                beginCrop(Uri.parse(path));
                            }
                            // AQuery aQuery = new AQuery(this);
                            // aQuery.id(ivProfile).image(bmp);
                            //
                            // String filePath = imageFilePath;
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
                            activity,
                            getResources().getString(
                                    R.string.toast_unable_to_selct_image),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case Crop.REQUEST_CROP:

                AppLog.Log(Const.TAG, "Crop photo on activity result");
                if (data != null)
                    handleCrop(resultCode, data);

                break;
        }

    }

    // private void gotoMyThingFragment(String token, String id) {
    // UberMyThingFragmentRegister fragMything = new
    // UberMyThingFragmentRegister();
    // Bundle bundle = new Bundle();
    // bundle.putString(Const.Params.TOKEN, token);
    // bundle.putString(Const.Params.ID, id);
    // fragMything.setArguments(bundle);
    // activity.addFragment(fragMything, false,
    // Const.FRAGMENT_MYTHING_REGISTER);
    // }

    // private void gotoPaymentFragment(String id, String token) {
    // UberAddPaymentFragmentRegister paymentFragment = new
    // UberAddPaymentFragmentRegister();
    // Bundle bundle = new Bundle();
    // bundle.putString(Const.Params.TOKEN, token);
    // bundle.putString(Const.Params.ID, id);
    // paymentFragment.setArguments(bundle);
    // activity.addFragment(paymentFragment, false,
    // Const.FRAGMENT_PAYMENT_REGISTER);
    // }
    private void goToReffralCodeFragment(String id, String token) {
        ReffralCodeFragment reffralCodeFragment = new ReffralCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Const.Params.TOKEN, token);
        bundle.putString(Const.Params.ID, id);
        reffralCodeFragment.setArguments(bundle);
        activity.addFragment(reffralCodeFragment, false,
                Const.FRAGMENT_REFFREAL);
    }

    @Override
    protected boolean isValidate() {
        String msg = null;
        if (TextUtils.isEmpty(etFName.getText().toString())) {
            msg = getString(R.string.text_enter_name);
            etFName.requestFocus();
        } else if (TextUtils.isEmpty(etLName.getText().toString())) {
            msg = getString(R.string.text_enter_lname);
            etLName.requestFocus();
        } else if (TextUtils.isEmpty(etEmail.getText().toString())) {
            msg = getString(R.string.text_enter_email);
            etEmail.requestFocus();
        } else if (!AndyUtils.eMailValidation((etEmail.getText().toString()))) {
            msg = getString(R.string.text_enter_valid_email);
            etEmail.requestFocus();
        } else if (etPassword.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                msg = getString(R.string.text_enter_password);
                etPassword.requestFocus();
            } else if (etPassword.getText().toString().length() < 6) {
                msg = getString(R.string.text_enter_password_valid);
                etPassword.requestFocus();
            }
        }
        if (msg != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            return false;

        }
        if (etPassword.getVisibility() == View.GONE) {
            if (!TextUtils.isEmpty(socialUrl)) {
                filePath = null;
                filePath = new AQuery(activity).getCachedFile(socialUrl)
                        .getAbsolutePath();
            }
        }
        if (TextUtils.isEmpty(etNumber.getText().toString())) {
            msg = getString(R.string.text_enter_number);
            etNumber.requestFocus();
        }
        // else if (TextUtils.isEmpty(filePath)) {
        // msg = getString(R.string.text_pro_pic);
        //
        // }

        if (msg == null) {
            return true;
        }
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void getProfile() {
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_getting_info), true, null);
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Properties.ID).add(Properties.FIRST_NAME)
                .add(Properties.GENDER).add(Properties.EMAIL)
                .add(Properties.LAST_NAME).add(Properties.BIRTHDAY)
                .add(Properties.EDUCATION).add(Properties.PICTURE).build();
        mSimpleFacebook.getProfile(properties, new OnProfileListener() {

            @Override
            public void onComplete(Profile profile) {
                AndyUtils.removeCustomProgressDialog();
                Log.i("Uber", "My profile id = " + profile.getId());
                btnGPlush.setEnabled(false);
                btnFb.setEnabled(false);
                etEmail.setText(profile.getEmail());
                etFName.setText(profile.getFirstName());
                etLName.setText(profile.getLastName());
                socialId = profile.getId();
                type = Const.SOCIAL_FACEBOOK;
                // etPassword.setEnabled(false);
                tvPassword.setVisibility(View.GONE);
                ivPassword.setVisibility(View.GONE);
                etPassword.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(profile.getPicture())
                        || !profile.getPicture().equalsIgnoreCase("null")) {
                    socialUrl = profile.getPicture();
                    AQuery aQuery = new AQuery(activity);
                    aQuery.id(ivProPic).image(profile.getPicture(),
                            getAqueryOption());
                } else {
                    socialUrl = null;
                }

            }
        });
    }

    private void register(String type, String id) {

        if (type.equalsIgnoreCase(Const.MANUAL)) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.URL, Const.ServiceType.REGISTER);
            map.put(Const.Params.FIRSTNAME, etFName.getText().toString());
            map.put(Const.Params.LAST_NAME, etLName.getText().toString());
            map.put(Const.Params.EMAIL, etEmail.getText().toString());
            map.put(Const.Params.PASSWORD, etPassword.getText().toString());
            map.put(Const.Params.PICTURE, filePath);
            map.put(Const.Params.PHONE, spCCode.getText().toString().trim()
                    + etNumber.getText().toString());
            map.put(Const.Params.DEVICE_TOKEN,
                    activity.phelper.getDeviceToken());
            map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
            map.put(Const.Params.ADDRESS, etAddress.getText().toString());
            // map.put(Const.Params.BIO, etBio.getText().toString());
            map.put(Const.Params.ZIPCODE, etZipCode.getText().toString());
            map.put(Const.Params.STATE, "");
            map.put(Const.Params.COUNTRY, "");
            map.put(Const.Params.LOGIN_BY, Const.MANUAL);
            AndyUtils.showCustomProgressDialog(activity,
                    getString(R.string.text_registering), true, null);
            new MultiPartRequester(activity, map, Const.ServiceCode.REGISTER,
                    this);
        } else {
            registerSocial(id, type);
        }

    }

    private void registerSocial(final String id, final String type) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.REGISTER);
        map.put(Const.Params.FIRSTNAME, etFName.getText().toString());
        map.put(Const.Params.LAST_NAME, etLName.getText().toString());
        map.put(Const.Params.EMAIL, etEmail.getText().toString());
        map.put(Const.Params.SOCIAL_UNIQUE_ID, id);
        map.put(Const.Params.PICTURE, filePath);
        map.put(Const.Params.PHONE, spCCode.getText().toString().trim()
                + etNumber.getText().toString());
        map.put(Const.Params.DEVICE_TOKEN, activity.phelper.getDeviceToken());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
        // map.put(Const.Params.ADDRESS, etAddress.getText().toString());
        // map.put(Const.Params.BIO, etBio.getText().toString());
        // map.put(Const.Params.ZIPCODE, etZipCode.getText().toString());
        map.put(Const.Params.STATE, "");
        map.put(Const.Params.COUNTRY, "");
        map.put(Const.Params.LOGIN_BY, type);
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_registering), true, null);
        new MultiPartRequester(activity, map, Const.ServiceCode.REGISTER, this);

    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        // System.out.println(response + "<-------");
        switch (serviceCode) {
            case Const.ServiceCode.REGISTER:
                AndyUtils.removeCustomProgressDialog();
                if (pContent.isSuccessWithStoreId(response)) {
                    // Toast.makeText(activity,
                    // getString(R.string.toast_register_success),
                    // Toast.LENGTH_SHORT).show();

                    // activity.phelper.putPassword(etPassword.getText().toString());
                    pContent.parseUserAndStoreToDb(response);
                    activity.phelper.putPassword(etPassword.getText().toString());
                    // JSONObject jsonObject;
                    // try {
                    // jsonObject = new JSONObject(response);
                    // activity.phelper.putSessionToken(jsonObject
                    // .getString(Const.Params.TOKEN));
                    // activity.phelper.putUserId(jsonObject
                    // .getString(Const.Params.ID));
                    // gotoPaymentFragment(
                    //
                    // jsonObject.getString(Const.Params.ID),
                    // jsonObject.getString(Const.Params.TOKEN));
                    showRegistrationConfirmationDialog();
                    goToReffralCodeFragment(activity.phelper.getUserId(),
                            activity.phelper.getSessionToken());
                    // } catch (JSONException e) {
                    // e.printStackTrace();
                    // }

                } else {
                    Toast.makeText(activity,
                            getString(R.string.toast_register_failed),
                            Toast.LENGTH_SHORT).show();

                }
                break;

        }
    }

    private void showRegistrationConfirmationDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);
        Button okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void choosePhotoFromGallary() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        activity.startActivityForResult(i, Const.CHOOSE_PHOTO,
                Const.FRAGMENT_REGISTER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                takePhotoAfterPermission();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void askForCameraPermission(){
        if ((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {

            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_REQUEST_CODE);
        }
        else{
            takePhotoAfterPermission();
        }
    }
    private void takePhotoFromCamera() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // only for gingerbread and newer versions
            askForCameraPermission();
            return;
        }
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
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(i, Const.TAKE_PHOTO,
                Const.FRAGMENT_REGISTER);
    }
void takePhotoAfterPermission(){
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
    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
    activity.startActivityForResult(i, Const.TAKE_PHOTO,
            Const.FRAGMENT_REGISTER);
}
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = activity.getContentResolver().query(contentURI, null,
                null, null, null);

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
                                        R.string.text_error_get_image),
                                activity);
                result = "";
            }
            cursor.close();
        }
        return result;
    }

    private ImageOptions getAqueryOption() {
        ImageOptions options = new ImageOptions();
        options.targetWidth = 200;
        options.memCache = true;
        options.fallback = R.drawable.default_user;
        options.fileCache = true;
        return options;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.fragments.UberBaseFragmentRegister#OnBackPressed()
     */
    @Override
    public boolean OnBackPressed() {
        // activity.removeAllFragment(new UberMainFragment(), false,
        // Const.FRAGMENT_MAIN);
        activity.goToMainActivity();
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.fragments.BaseFragmentRegister#OnBackPressed()
     */
    private void beginCrop(Uri source) {
        // Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
        // "cropped"));
        Uri outputUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), (Calendar.getInstance()
                .getTimeInMillis() + ".jpg")));
        Crop.of(source, outputUri).asSquare().start(activity);
    }

    @SuppressWarnings("static-access")
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == activity.RESULT_OK) {
            AppLog.Log(Const.TAG, "Handle crop");
            filePath = getRealPathFromURI(Crop.getOutput(result));
            ivProPic.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(activity, Crop.getError(result).getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
