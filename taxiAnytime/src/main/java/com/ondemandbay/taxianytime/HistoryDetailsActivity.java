package com.ondemandbay.taxianytime;

//import java.util.ArrayList;
//import java.util.HashMap;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.models.History;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;

import java.text.DecimalFormat;

import io.fabric.sdk.android.Fabric;
//import com.android.volley.Request.Method;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.widget.ListView;

public class HistoryDetailsActivity extends ActionBarBaseActivitiy implements
        OnItemClickListener {

    private TextView tvHistoryPickupAddr;
    private TextView tvHistoryDestAddr;
    private TextView tvHistoryDistance;
    private TextView tvHistoryDuration;
    private TextView tvHistoryTotalCost;
    private History history;
    private ImageOptions imageOptions;
    private AQuery aQuery;
    private ImageView ivHistoryMapBig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.history_details);
        setTitle(getString(R.string.text_trip_details));
        setIconMenu(R.drawable.back);
        btnNotification.setVisibility(View.INVISIBLE);
        history = (History) getIntent().getSerializableExtra(
                Const.HISTORY_DETAILS);
        imageOptions = new ImageOptions();
        imageOptions.fileCache = true;
        imageOptions.memCache = true;
        imageOptions.fallback = R.drawable.no_items;
        aQuery = new AQuery(this);
        // parseContent = new ParseContent(this);
        // preferenceHelper = new PreferenceHelper(this);

        tvHistoryPickupAddr = (TextView) findViewById(R.id.tvHistoryPickupAddr);
        tvHistoryDestAddr = (TextView) findViewById(R.id.tvHistoryDestAddr);
        tvHistoryDistance = (TextView) findViewById(R.id.tvHistoryDistance);
        tvHistoryDuration = (TextView) findViewById(R.id.tvHistoryDuration);
        tvHistoryTotalCost = (TextView) findViewById(R.id.tvHistoryTotalCost);
        ivHistoryMapBig = (ImageView) findViewById(R.id.ivHistoryMapBig);
        // lvIssues = (ListView) findViewById(R.id.lvIssues);
        // lvIssues.setOnItemClickListener(this);

        // findViewById(R.id.tvNeedHelp).setOnClickListener(this);
        // tvHistoryPickupAddr.setText(getAddressFromLocation(new
        // LatLng(Double.parseDouble(history.getSourceLat()),
        // Double.parseDouble(history.getSourceLng()))));
        tvHistoryPickupAddr.setText(history.getSrcAdd());
        tvHistoryPickupAddr.setSingleLine();
        // tvHistoryDestAddr.setText(getAddressFromLocation(new
        // LatLng(Double.parseDouble(history.getDestLat()),
        // Double.parseDouble(history.getDestLng()))));
        tvHistoryDestAddr.setText(history.getDestAdd());
        tvHistoryDestAddr.setSingleLine();

        aQuery.id(ivHistoryMapBig).progress(R.id.pBar)
                .image(history.getMapImage(), imageOptions);
        tvHistoryDuration.setText(new DecimalFormat("0.00").format(Double
                .parseDouble(history.getTime()))
                + " "
                + getResources().getString(R.string.text_mins));
        tvHistoryDistance.setText(history.getDistance() + " "
                + history.getUnit());
        tvHistoryTotalCost.setText(history.getCurrency() + history.getTotal());
        // issueList = new ArrayList<Issue>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_right);
                break;
            // case R.id.tvNeedHelp:
            // getIssues();
            // break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    // private void getIssues() {
    // if (!AndyUtils.isNetworkAvailable(this)) {
    // AndyUtils.showToast(getResources().getString(R.string.no_internet),
    // this);
    // return;
    // }
    // AndyUtils.showCustomProgressDialog(this,
    // getResources().getString(R.string.progress_loading), false,
    // null);
    // HashMap<String, String> map = new HashMap<String, String>();
    // map.put(Const.URL, Const.ServiceType.ISSUE + Const.Params.ID + "="
    // + preferenceHelper.getUserId() + "&" + Const.Params.TOKEN + "="
    // + preferenceHelper.getSessionToken());
    // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
    // Const.ServiceCode.ISSUE, this, this));
    // }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        switch (serviceCode) {
            // case Const.ServiceCode.ISSUE:
            // AppLog.Log("NeedHelpActivity", "Issue Response :" + response);
            // if (!parseContent.isSuccess(response)) {
            // return;
            // }
            // issueList.clear();
            // parseContent.parseIssue(response, issueList);
            // issueAdapter = new IssueAdapter(this, issueList);
            // lvIssues.setAdapter(issueAdapter);
            // break;
            // case Const.ServiceCode.SEND_ISSUE:
            // AppLog.Log("NeedHelpActivity", "Send Issue Response :" + response);
            // if (parseContent.isSuccess(response)) {
            // AndyUtils.showToast(
            // getResources().getString(R.string.msg_issue_success),
            // this);
            // }
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            final int position, long id) {
        // new AlertDialog.Builder(this)
        // .setTitle(getString(R.string.dialog_issue))
        // .setMessage(getString(R.string.dialog_issue_text))
        // .setPositiveButton(android.R.string.yes,
        // new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog,
        // int which) {
        // sendIssue(issueList.get(position).getIssueId());
        // }
        // })
        // .setNegativeButton(android.R.string.no,
        // new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog,
        // int which) {
        // dialog.cancel();
        // }
        // }).show();
    }

    // private void sendIssue(int issueId) {
    // if (!AndyUtils.isNetworkAvailable(this)) {
    // AndyUtils.showToast(getResources().getString(R.string.no_internet),
    // this);
    // return;
    // }
    // AndyUtils.showCustomProgressDialog(this,
    // getResources().getString(R.string.progress_send_issue), false,
    // null);
    // HashMap<String, String> map = new HashMap<String, String>();
    // map.put(Const.URL, Const.ServiceType.SEND_ISSUE);
    // map.put(Const.Params.ID, preferenceHelper.getUserId());
    // map.put(Const.Params.REQUEST_ID, String.valueOf(history.getId()));
    // map.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
    // map.put(Const.Params.ISSUE_ID, String.valueOf(issueId));
    // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
    // Const.ServiceCode.SEND_ISSUE, this, this));
    // }

    // private String getAddressFromLocation(final LatLng latlng) {
    // Geocoder gCoder = new Geocoder(this);
    // try{
    // final List<Address> list = gCoder.getFromLocation(
    // latlng.latitude, latlng.longitude, 1);
    // if (list != null && list.size() > 0) {
    // address = list.get(0);
    // StringBuilder sb = new StringBuilder();
    // if (address.getAddressLine(0) != null) {
    // for (int i = 0; i < address
    // .getMaxAddressLineIndex(); i++) {
    // if (i == (address.getMaxAddressLineIndex() - 1))
    // sb.append(address.getAddressLine(i));
    // else
    // sb.append(address.getAddressLine(i))
    // .append(", ");
    // }
    // }
    // strAddress = sb.toString();
    // strAddress = strAddress.replace(",null", "");
    // strAddress = strAddress.replace("null", "");
    // strAddress = strAddress.replace("Unnamed", "");
    // }
    // }
    // catch(Exception e){
    // e.printStackTrace();
    // }
    //
    // return strAddress;
    // }
}
