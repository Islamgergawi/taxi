package com.ondemandbay.taxianytime.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View.OnClickListener;

import com.ondemandbay.taxianytime.MainDrawerActivity;
import com.ondemandbay.taxianytime.RegisterActivity;
import com.ondemandbay.taxianytime.parse.AsyncTaskCompleteListener;

/**
 * @author Elluminati elluminati.in
 */
abstract public class BaseFragmentRegister extends Fragment implements
        OnClickListener, AsyncTaskCompleteListener {
    RegisterActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        activity = (RegisterActivity) getActivity();
    }

    protected abstract boolean isValidate();

    public boolean OnBackPressed() {
        // activity.removeAllFragment(new UberMainFragment(), false,
        // Const.FRAGMENT_MAIN);
        activity.startActivity(new Intent(activity, MainDrawerActivity.class));
        activity.finish();
        return true;
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        // TODO Auto-generated method stub

        super.onResume();
        // activity.actionBar.show();

    }

}
