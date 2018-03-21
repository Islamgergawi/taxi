package com.ondemandbay.taxianytime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.fragments.RegisterFragment;
import com.ondemandbay.taxianytime.fragments.SignInFragment;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class RegisterActivity extends ActionBarBaseActivitiy {

    public PreferenceHelper phelper;
    Permission[] permissions = new Permission[]{Permission.USER_PHOTOS,
            Permission.EMAIL,
            Permission.PUBLISH_ACTION};
    SimpleFacebookConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.register_activity);
//       configuration = new SimpleFacebookConfiguration.Builder().setAppId(getString(R.string.applicationId))
//                .setPermissions(permissions).build();
      //  SimpleFacebook.setConfiguration(configuration);
        phelper = new PreferenceHelper(this);
        setIcon(R.drawable.back);

        if (getIntent().getBooleanExtra("isSignin", false)) {
            gotSignInFragment();
        } else {
            goToRegisterFragment();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionNotification:
                onBackPressed();
                break;

            default:
                break;
        }
    }

    public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
        if (mHandleMessageReceiver != null) {
            AndyUtils.showCustomProgressDialog(this,
                    getString(R.string.progress_loading), false, null);
            new GCMRegisterHendler(RegisterActivity.this,
                    mHandleMessageReceiver);

        }
    }

    public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
        if (mHandleMessageReceiver != null) {

            if (mHandleMessageReceiver != null) {
                unregisterReceiver(mHandleMessageReceiver);
            }
        }
    }

    private void gotSignInFragment() {
        SignInFragment signInFrag = new SignInFragment();
        clearBackStack();
        addFragment(signInFrag, false, Const.FRAGMENT_SIGNIN);
    }

    private void goToRegisterFragment() {
        RegisterFragment regFrag = new RegisterFragment();
        clearBackStack();
        addFragment(regFrag, false, Const.FRAGMENT_REGISTER);
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

    public void showKeyboard(View v) {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        // View view = activity.getCurrentFocus();
        // if (view != null) {
        inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        // }
    }

}