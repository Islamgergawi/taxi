package com.ondemandbay.taxianytime.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ondemandbay.taxianytime.CommonUtilities;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

/**
 * @author Elluminati elluminati.in
 */
public class MainFragment extends BaseFragmentRegister {

    private ImageButton btnSignIn, btnRegister;
    private PreferenceHelper pHelper;
    private boolean isReceiverRegister;

    // private Animation topToBottomAnimation, bottomToTopAnimation;
    // private RelativeLayout rlLoginRegisterLayout;
    // private MyFontTextView tvMainBottomView;
    private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            AndyUtils.removeCustomProgressDialog();
            if (intent.getAction().equals(CommonUtilities.DISPLAY_REGISTER_GCM)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {

                    int resultCode = bundle.getInt(CommonUtilities.RESULT);
                    if (resultCode == Activity.RESULT_OK) {

                    } else {
                        Toast.makeText(
                                activity,
                                getResources().getString(
                                        R.string.register_gcm_failed),
                                Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }

                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isReceiverRegister = false;
        pHelper = new PreferenceHelper(activity);
        // oldOptions = activity.getWindow().getDecorView()
        // .getSystemUiVisibility();
        // int newOptions = oldOptions;
        // newOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        // newOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // activity.getWindow().getDecorView().setSystemUiVisibility(newOptions);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);

        btnSignIn = (ImageButton) view.findViewById(R.id.btnSignIn);
        btnRegister = (ImageButton) view.findViewById(R.id.btnRegister);
        // rlLoginRegisterLayout = (RelativeLayout) view
        // .findViewById(R.id.rlLoginRegisterLayout);
        // tvMainBottomView = (MyFontTextView) view
        // .findViewById(R.id.tvMainBottomView);
        btnSignIn.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (TextUtils.isEmpty(pHelper.getDeviceToken())) {
            isReceiverRegister = true;
            activity.registerGcmReceiver(mHandleMessageReceiver);
        }
        // topToBottomAnimation = AnimationUtils.loadAnimation(activity,
        // R.anim.top_bottom);
        // rlLoginRegisterLayout.setAnimation(topToBottomAnimation);
        // rlLoginRegisterLayout.startAnimation(topToBottomAnimation);
        // bottomToTopAnimation = AnimationUtils.loadAnimation(activity,
        // R.anim.bottom_top);
        // tvMainBottomView.setAnimation(bottomToTopAnimation);
        // tvMainBottomView.startAnimation(bottomToTopAnimation);
    }

    @Override
    public void onResume() {

        super.onResume();
        activity.actionBar.hide();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                gotSignInFragment();
                break;
            case R.id.btnRegister:
                goToRegisterFragment();
                break;

            default:
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    private void gotSignInFragment() {
        SignInFragment signInFrag = new SignInFragment();
        activity.clearBackStack();
        activity.addFragment(signInFrag, false, Const.FRAGMENT_SIGNIN);
    }

    private void goToRegisterFragment() {
        RegisterFragment regFrag = new RegisterFragment();
        activity.clearBackStack();
        activity.addFragment(regFrag, false, Const.FRAGMENT_REGISTER);
    }

    @Override
    public void onDestroy() {
        if (isReceiverRegister) {
            activity.unregisterGcmReceiver(mHandleMessageReceiver);
            isReceiverRegister = false;
        }
        super.onDestroy();
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see com.uberorg.fragments.BaseFragmentRegister#OnBackPressed()
	 */
    // @Override
    // public boolean OnBackPressed() {
    // Toast.makeText(activity, "main", Toast.LENGTH_SHORT).show();
    // return false;
    // }

}
