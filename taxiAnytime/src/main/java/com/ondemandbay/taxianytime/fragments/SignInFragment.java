package com.ondemandbay.taxianytime.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.ondemandbay.taxianytime.MainDrawerActivity;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.component.MyFontButton;
import com.ondemandbay.taxianytime.component.MyFontEdittextView;
import com.ondemandbay.taxianytime.component.MyTitleFontTextView;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.util.HashMap;
import java.util.List;

/**
 * @author Elluminati elluminati.in
 */
public class SignInFragment extends BaseFragmentRegister
        implements
        com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private MyFontEdittextView etEmail, etPassword, etForgetEmail;
    private MyFontButton btnSignIn, btnBackSignIn;
    private ImageButton btnGPlus;
    private ImageButton btnFb;
    // Gplus
    private ConnectionResult mConnectionResult;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;

    // FB
    private SimpleFacebook mSimpleFacebook;

    private MyTitleFontTextView btnForgetPassowrd/* , tvRegisterAccount */;
    // private ParseContent pContent;
    // private ArrayList<String> list;
    private Dialog forgotPasswordDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity.actionBar.hide();
        // activity.setStatusBarColor(getResources().getColor(
        // R.color.color_action_bar_main));
        // activity.getSupportActionBar().hide();
        Scope scope = new Scope("https://www.googleapis.com/auth/plus.login");
        // Scope scopePro = new
        // Scope("https://www.googleapis.com/auth/plus.me");
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, PlusOptions.builder().build())
                .addScope(scope).build();
        // country = Locale.getDefault().getDisplayCountry();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // activity.setTitle(getResources().getString(R.string.text_signin));
        // activity.setIconMenu(R.drawable.taxi);
        View view = inflater.inflate(R.layout.login, container, false);
        etEmail = (MyFontEdittextView) view.findViewById(R.id.etEmail);
        etPassword = (MyFontEdittextView) view.findViewById(R.id.etPassword);
        btnSignIn = (MyFontButton) view.findViewById(R.id.btnSignIn);
        btnGPlus = (ImageButton) view.findViewById(R.id.btnGplus);
        btnFb = (ImageButton) view.findViewById(R.id.btnFb);
        btnForgetPassowrd = (MyTitleFontTextView) view
                .findViewById(R.id.btnForgetPassword);
        // tvRegisterAccount = (MyFontTextView) view
        // .findViewById(R.id.tvRegisterAccount);
        btnBackSignIn = (MyFontButton) view.findViewById(R.id.btnBackSignIn);
        btnForgetPassowrd.setOnClickListener(this);
        // tvRegisterAccount.setOnClickListener(this);
        btnGPlus.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnFb.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnBackSignIn.setOnClickListener(this);

        // pContent = new ParseContent(activity);

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        activity.actionBar.hide();
        activity.currentFragment = Const.FRAGMENT_SIGNIN;
        activity.actionBar.setTitle(getString(R.string.text_signin_small));
//        mSimpleFacebook = SimpleFacebook.getInstance(activity);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFb:
                if (!mSimpleFacebook.isLogin()) {
                    activity.setFbTag(Const.FRAGMENT_SIGNIN);

                    mSimpleFacebook.login(new OnLoginListener() {
                        @Override
                        public void onException(Throwable throwable) {
                            Log.d("tag", "onException");
                        }

                        @Override
                        public void onFail(String reason) {
                            Toast.makeText(activity, getString(R.string.error_facebook_login_failed),
                                    Toast.LENGTH_SHORT).show();
                            Log.d("tag", "onFail");
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

                        /*@Override
                        public void onFail(String arg0) {
                            Toast.makeText(activity, "fb login failed",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("tag", "onFail");
                        }

                        @Override
                        public void onException(Throwable arg0) {
                            Log.d("tag", "onException");
                        }

                        @Override
                        public void onThinking() {
                            Log.d("tag", "onThinking");
                        }

                        @Override
                        public void onNotAcceptingPermissions(Type arg0) {
                            Log.d("tag", "onNotAcceptingPermissions");
                        }

                        @Override
                        public void onLogin() {
                            Toast.makeText(activity, "success", Toast.LENGTH_SHORT)
                                    .show();
                            Log.d("tag", "onLogin");
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
            case R.id.btnSignIn:
                if (isValidate()) {
                    login();
                }
                break;
            case R.id.btnForgetPassword:
                showForgotPasswordDialog();
                // activity.addFragment(new ForgetPasswordFragment(), true,
                // Const.FOREGETPASS_FRAGMENT_TAG);
                break;

            // case R.id.tvRegisterAccount:
            // activity.addFragment(new UberRegisterFragment(), true,
            // Const.FRAGMENT_REGISTER);
            // break;
            case R.id.btnBackSignIn:
                OnBackPressed();
                break;

            case R.id.tvForgetSubmit:
                if (etForgetEmail.getText().length() == 0) {
                    AndyUtils.showToast(
                            getResources().getString(R.string.text_enter_email),
                            activity);
                    return;
                } else if (!AndyUtils.eMailValidation(etForgetEmail.getText()
                        .toString())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.text_enter_valid_email), activity);
                    return;
                } else {
                    if (!AndyUtils.isNetworkAvailable(activity)) {
                        AndyUtils
                                .showToast(
                                        getResources().getString(
                                                R.string.dialog_no_inter_message),
                                        activity);
                        return;
                    }
                    forgetPassowrd();
                }
                break;
            default:
                break;
        }
    }

    private void getProfile() {
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_getting_info), true, null);

/*
        mSimpleFacebook.getProfile(new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                AndyUtils.removeCustomProgressDialog();
                Log.i("Uber", "My profile id = " + profile.getId());
                btnGPlus.setEnabled(false);
                btnFb.setEnabled(false);
                loginSocial(profile.getId(), Const.SOCIAL_FACEBOOK);
            }
        });
*/
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
        btnGPlus.setEnabled(false);

        // String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);
        //Toast.makeText(getActivity(), currentPerson.getDisplayName(), Toast.LENGTH_SHORT).show();
        // String personName = currentPerson.getDisplayName();
    etEmail.setText(currentPerson.getId());
        // String personPhoto = currentPerson.getImage().toString();
        // String personGooglePlusProfile = currentPerson.getUrl();
        loginSocial(currentPerson.getId(), Const.SOCIAL_GOOGLE);
        // signIn();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {

        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                activity.startIntentSenderForResult(mConnectionResult
                                .getResolution().getIntentSender(), RC_SIGN_IN, null,
                        0, 0, 0, Const.FRAGMENT_SIGNIN);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {

            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
                AndyUtils.removeCustomProgressDialog();
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            mSimpleFacebook.onActivityResult( requestCode, resultCode,
                    data);
            if (mSimpleFacebook.isLogin()) {
                getProfile();
            } else {
                Toast.makeText(activity, getString(R.string.error_facebook_login_failed),
                        Toast.LENGTH_SHORT).show();
            }

            super.onActivityResult(requestCode, resultCode, data);

        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {

    }

    @Override
    protected boolean isValidate() {
        String msg = null;
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            msg = getResources().getString(R.string.text_enter_email);
        } else if (!TextUtils.isEmpty(etEmail.getText().toString())) {
            if (!AndyUtils.eMailValidation(etEmail.getText().toString())) {
                msg = getResources().getString(R.string.text_enter_valid_email);
            }
        }
        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            msg = getResources().getString(R.string.text_enter_password);
        }
        if (msg == null)
            return true;

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    // private void signIn() {
    // Intent intent = new Intent(activity, MainDrawerActivity.class);
    // startActivity(intent);
    // activity.finish();
    // }

    private void login() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressDialog(activity,
                getResources().getString(R.string.text_signing), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGIN);
        map.put(Const.Params.EMAIL, etEmail.getText().toString());
        map.put(Const.Params.PASSWORD, etPassword.getText().toString());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(activity).getDeviceToken());
        map.put(Const.Params.LOGIN_BY, Const.MANUAL);
        new HttpRequester(activity, map, Const.ServiceCode.LOGIN, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.LOGIN, this, this));
    }

    private void loginSocial(String id, String loginType) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressDialog(activity,
                getResources().getString(R.string.text_signin), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGIN);
        map.put(Const.Params.SOCIAL_UNIQUE_ID, id);
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(activity).getDeviceToken());
        map.put(Const.Params.LOGIN_BY, loginType);
        new HttpRequester(activity, map, Const.ServiceCode.LOGIN, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.LOGIN, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        ParseContent parseContent = new ParseContent(activity);
        AndyUtils.removeCustomProgressDialog();
        super.onTaskCompleted(response, serviceCode);
        switch (serviceCode) {
            case Const.ServiceCode.LOGIN:
                // Log.d("tag",
                // "Login response"
                // + parseContent.isSuccessWithStoreId(response));
                // try {
                // JSONObject jsonObject = new JSONObject(response);
                // if (jsonObject.getBoolean("success")) {
                // Log.d("tag",
                // "Login response=="
                // + jsonObject.getBoolean("success"));
                // }
                // } catch (Exception e) {
                // e.printStackTrace();
                // }

                if (parseContent.isSuccessWithStoreId(response)) {
                    parseContent.parseUserAndStoreToDb(response);
                    new PreferenceHelper(activity).putPassword(etPassword.getText()
                            .toString());
                    if (isAdded()) {
                        startActivity(new Intent(activity, MainDrawerActivity.class));
                        activity.finish();
                    }
                } else {
                    AndyUtils.showToast(
                            getResources().getString(R.string.signin_failed),
                            activity);
                    btnFb.setEnabled(true);
                    btnGPlus.setEnabled(true);
                }
                break;

            case Const.ServiceCode.FORGET_PASSWORD:
                AppLog.Log("TAG", "forget res:" + response);
                if (new ParseContent(activity).isSuccess(response)) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.toast_forget_password_success),
                            activity);
                    forgotPasswordDialog.dismiss();
                } else {
                    AndyUtils.showToast(
                            getResources().getString(R.string.toast_email_ivalid),
                            activity);
                }
                break;
            default:
                break;
        }
    }

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

    private void showForgotPasswordDialog() {
        forgotPasswordDialog = new Dialog(getActivity());
        forgotPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        forgotPasswordDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        forgotPasswordDialog.setContentView(R.layout.forget_pass_fragment);
        forgotPasswordDialog.setCancelable(true);
        etForgetEmail = (MyFontEdittextView) forgotPasswordDialog
                .findViewById(R.id.etForgetEmail);
        forgotPasswordDialog.findViewById(R.id.tvForgetSubmit)
                .setOnClickListener(this);
        etForgetEmail.requestFocus();
        activity.showKeyboard(etForgetEmail);

        forgotPasswordDialog.show();

    }

    private void forgetPassowrd() {

        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_forget_password_loading_msg), false,
                null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.FORGET_PASSWORD);
        map.put(Const.Params.TYPE, Const.Params.OWNER);
        map.put(Const.Params.EMAIL, etForgetEmail.getText().toString());
        new HttpRequester(activity, map, Const.ServiceCode.FORGET_PASSWORD,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.FORGET_PASSWORD, this, this));
    }

}
