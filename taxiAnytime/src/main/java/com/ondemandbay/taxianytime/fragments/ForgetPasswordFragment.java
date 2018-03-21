/**
 *
 */
package com.ondemandbay.taxianytime.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.component.MyFontEdittextView;
import com.ondemandbay.taxianytime.parse.AsyncTaskCompleteListener;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;

import java.util.HashMap;

/**
 * @author Elluminati elluminati.in
 */
public class ForgetPasswordFragment extends BaseFragmentRegister implements
        AsyncTaskCompleteListener {
    private MyFontEdittextView etEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View forgetView = inflater.inflate(R.layout.forget_pass_fragment,
                container, false);
        etEmail = (MyFontEdittextView) forgetView
                .findViewById(R.id.etForgetEmail);
        forgetView.findViewById(R.id.tvForgetSubmit).setOnClickListener(this);
        activity.setTitle(getString(R.string.text_forget_password));
        return forgetView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.actionBar.show();
        etEmail.requestFocus();
        activity.showKeyboard(etEmail);
        // (getResources().getString(
        // R.string.text_forget_password));
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvForgetSubmit:
                if (etEmail.getText().length() == 0) {
                    AndyUtils.showToast(
                            getResources().getString(R.string.text_enter_email),
                            activity);
                    return;
                } else if (!AndyUtils.eMailValidation(etEmail.getText().toString())) {
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

    private void forgetPassowrd() {

        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_forget_password_loading_msg), false,
                null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.FORGET_PASSWORD);
        map.put(Const.Params.TYPE, Const.Params.OWNER);
        map.put(Const.Params.EMAIL, etEmail.getText().toString());
        new HttpRequester(activity, map, Const.ServiceCode.FORGET_PASSWORD,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.FORGET_PASSWORD, this, this));
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
        switch (serviceCode) {
            case Const.ServiceCode.FORGET_PASSWORD:
                AppLog.Log("TAG", "forget res:" + response);
                if (new ParseContent(activity).isSuccess(response)) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.toast_forget_password_success),
                            activity);
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

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.fragments.UberBaseFragmentRegister#isValidate()
     */
    @Override
    protected boolean isValidate() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.automated.taxinow.fragments.UberBaseFragmentRegister#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        activity.actionBar.show();
        activity.actionBar.setTitle(getString(R.string.text_forget_pass_small));
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.fragments.UberBaseFragmentRegister#OnBackPressed()
     */
    @Override
    public boolean OnBackPressed() {

        return false;
    }
}
