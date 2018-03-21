/**
 *
 */
package com.ondemandbay.taxianytime;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.utils.Const;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class MenuDescActivity extends ActionBarBaseActivitiy {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.menu_desc_activity);
        setIconMenu(R.drawable.back);
        btnNotification.setVisibility(View.GONE);
        setTitle(getIntent().getStringExtra(Const.Params.TITLE));
        webView = (WebView) findViewById(R.id.wvDesc);
        webView.loadData(getIntent().getStringExtra(Const.Params.CONTENT),
                "text/html", "utf-8");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                onBackPressed();
                break;

            default:
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        return false;
    }
}
