package com.ondemandbay.taxianytime;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class BookingDetailsActivity extends ActionBarBaseActivitiy {
    private ImageOptions imageOptions;
    private AQuery aQuery;
    private TextView tvDetailsPickupAddr;
    private TextView tvDetailsDestAddr;
    private TextView tvBookingDate;
    private TextView tvPickUpTime;
    private TextView tvVehicle;
    private ImageView ivBookingMapBig;
    private ParseContent pContent;
    private PreferenceHelper pHelper;
    private Booking booking;
    private Intent i;
    private Date newDate = new Date();
    // private String startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_booking_details);
        setTitle(getString(R.string.title_activity_booking_details));
        setIconMenu(R.drawable.back);
        btnNotification.setVisibility(View.INVISIBLE);
        imageOptions = new ImageOptions();
        imageOptions.fileCache = true;
        imageOptions.memCache = true;
        imageOptions.fallback = R.drawable.no_items;
        aQuery = new AQuery(this);
        booking = (Booking) getIntent().getSerializableExtra("bookingObj");
        pContent = new ParseContent(this);
        pHelper = new PreferenceHelper(this);
        tvDetailsPickupAddr = (TextView) findViewById(R.id.tvPickupAddr);
        tvDetailsDestAddr = (TextView) findViewById(R.id.tvDestAddr);
        tvBookingDate = (TextView) findViewById(R.id.tvBookingDate);
        tvPickUpTime = (TextView) findViewById(R.id.pickUpTime);
        tvVehicle = (TextView) findViewById(R.id.tvVehicle);
        ivBookingMapBig = (ImageView) findViewById(R.id.ivBookingMapBig);

        findViewById(R.id.tvCancelRequest).setOnClickListener(this);
        tvDetailsPickupAddr.setText(booking.getSource());
        tvDetailsDestAddr.setText(booking.getDest());

        aQuery.id(ivBookingMapBig).progress(R.id.pBar)
                .image(booking.getMapImage(), imageOptions);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            newDate = sdf.parse(booking.getStartTime());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        sdf = new SimpleDateFormat("EEE, MMM d hh:mm a");

        // startTime = sdf.format(newDate);

        tvBookingDate.setText((sdf.format(newDate)).substring(0, 11));
        tvPickUpTime.setText((sdf.format(newDate)).substring(11));
        tvVehicle.setText(booking.getVehicleType());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                onBackPressed();
                break;
            case R.id.tvCancelRequest:
                cancelFutureRequest();
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    private void cancelFutureRequest() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.dialog_no_inter_message),
                    this);
            return;
        }
        showCancellationtDialog();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.DELETE_FUTURE_REQUEST:
                if (pContent.isSuccess(response)) {
                    AppLog.Log("DELETE_FUTURE_REQUEST response", response);
                    AndyUtils.removeCustomProgressDialog();
                    AndyUtils.showToast("Your request cancelled successfully.",
                            BookingDetailsActivity.this);
                    // onBackPressed();
                    i = new Intent(BookingDetailsActivity.this,
                            MainDrawerActivity.class);
                    startActivity(i);
                } else {
                    AndyUtils.removeCustomProgressDialog();
                    AndyUtils.showToast("Error", this);
                }
        }
    }

    private void showCancellationtDialog() {
        final Dialog cancellationDialog = new Dialog(this);
        cancellationDialog.setTitle("Tell us why");
        cancellationDialog.setContentView(R.layout.dilog_cancel_future_request);
        Button ok = (Button) cancellationDialog.findViewById(R.id.btnOk);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                cancellationDialog.dismiss();
                AndyUtils.showCustomProgressDialog(
                        BookingDetailsActivity.this,
                        getResources().getString(
                                R.string.progress_canceling_trip), false, null);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Const.URL, Const.ServiceType.DELETE_FUTURE_REQUEST);
                map.put(Const.Params.TOKEN, pHelper.getSessionToken());
                map.put(Const.Params.ID, pHelper.getUserId());
                map.put(Const.Params.REQUEST_ID,
                        String.valueOf(booking.getRequestId()));

                new HttpRequester(BookingDetailsActivity.this, map,
                        Const.ServiceCode.DELETE_FUTURE_REQUEST,
                        BookingDetailsActivity.this);

                // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
                // Const.ServiceCode.DELETE_FUTURE_REQUEST,
                // BookingDetailsActivity.this,
                // BookingDetailsActivity.this));
            }
        });
        cancellationDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
