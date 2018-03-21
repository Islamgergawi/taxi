package com.ondemandbay.taxianytime.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.BookingAdapter;
import com.ondemandbay.taxianytime.interfaces.OnProgressCancelListener;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBookingsFragment extends BaseFragment implements
        OnItemClickListener, OnProgressCancelListener {

    public PreferenceHelper pHelper;
    private ListView bookings;
    private ParseContent parseContent;
    private ArrayList<Booking> bookingList;
    private BookingAdapter bookingAdapter;
    private View view;
    private ImageView ivNoHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.btnNotification.setVisibility(View.INVISIBLE);
        view = inflater
                .inflate(R.layout.fragment_my_bookings, container, false);
        ivNoHistory = (ImageView) view.findViewById(R.id.ivNoBookings);
        bookings = (ListView) view.findViewById(R.id.my_trips);
        bookings.setOnItemClickListener(this);
        activity.btnActionMenu.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity.setTitle(getResources().getString(R.string.text_my_bookings));
        activity.setIconMenu(R.drawable.back);

        bookingList = new ArrayList<Booking>();
        pHelper = new PreferenceHelper(activity);
        parseContent = new ParseContent(activity);
        activity.isBookingFragment = true;
        getTripsList();
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                if (activity.isBookingFragment) {
                    activity.isBookingFragment = false;
                    activity.setIconMenu(R.drawable.menu);
                    activity.drawerLayout.closeDrawer(activity.listDrawer);
                    activity.gotoMapFragment();
                    break;
                }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        Booking booking = bookingList.get(position);
        if (booking.isFutureRequest()) {
            activity.gotoBookingDetail(booking);
        } else {
            activity.pHelper.putRequestId(booking.getRequestId());
            getRequestStatus(String.valueOf(activity.pHelper.getRequestId()));

        }

    }

    private void getRequestStatus(String requestId) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                Const.ServiceType.GET_REQUEST_STATUS + Const.Params.ID + "="
                        + new PreferenceHelper(activity).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(activity).getSessionToken()
                        + "&" + Const.Params.REQUEST_ID + "=" + requestId);

        new HttpRequester(activity, map, Const.ServiceCode.GET_REQUEST_STATUS,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REQUEST_STATUS, this, this));
    }

    private void getTripsList() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.dialog_no_inter_message),
                    activity);
            return;
        }

        AndyUtils.showCustomProgressDialog(activity,
                getResources().getString(R.string.progress_getting_trips),
                false, null);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_FUTURE_REQUEST);
        map.put(Const.Params.TOKEN, pHelper.getSessionToken());
        map.put(Const.Params.ID, pHelper.getUserId());
        AppLog.Log("Get future Request", "Parameters");
        AppLog.Log("id", pHelper.getUserId());
        AppLog.Log("token", pHelper.getSessionToken());

        new HttpRequester(activity, map, Const.ServiceCode.GET_FUTURE_REQUEST,
                this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.GET_FUTURE_REQUEST, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        AndyUtils.removeCustomProgressRequestDialog();

        switch (serviceCode) {
            case Const.ServiceCode.GET_FUTURE_REQUEST:
                AppLog.Log("TAG", "GET FUTURE REQUEST Response :" + response);
                // try {
                // JSONObject obj = new JSONObject(response);
                // JSONArray jArray = obj.getJSONArray("requests");
                // if(jArray.length() > 0){
                // for(int i=0; i<jArray.length(); i++){
                // JSONObject jObj = jArray.getJSONObject(i);
                // String status = jObj.getString("status");
                // AppLog.Log("Driver Status ", status);
                // }
                // }
                // } catch (JSONException e1) {
                // e1.printStackTrace();
                // }
                try {
                    bookingList.clear();
                    parseContent.parseBookingList(response, bookingList);
                    for (int i = 0; i < bookingList.size(); i++) {
                        AppLog.Log("BookingList", bookingList.get(i).getStartTime());
                    }

                    if (bookingList.size() > 0) {
                        bookings.setVisibility(View.VISIBLE);
                        ivNoHistory.setVisibility(View.GONE);
                    } else {
                        ivNoHistory.setVisibility(View.VISIBLE);
                        bookings.setVisibility(View.GONE);
                    }

                    bookingAdapter = new BookingAdapter(activity, bookingList);
                    bookings.setAdapter(bookingAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case Const.ServiceCode.GET_REQUEST_STATUS:
//			AppLog.Log("TAG", "GET_REQUEST_STATUS Response :" + response);

                if (activity.pContent.isSuccess(response)) {
//				AppLog.Log("UberMapFragment", "get request Response : "
//						+ response);
                    switch (activity.pContent.checkRequestStatus(response)) {
                        case Const.IS_WALKER_STARTED:
                        case Const.IS_WALKER_ARRIVED:
                        case Const.IS_WALK_STARTED:
                        case Const.IS_COMPLETED:
                            Driver driver = activity.pContent.getDriverDetail(response);
                            activity.setIconMenu(R.drawable.menu);
                            activity.gotoTripFragment(driver);
                            break;
                        case Const.IS_WALKER_RATED:
                            activity.setIconMenu(R.drawable.menu);
                            activity.gotoRateFragment(activity.pContent
                                    .getDriverDetail(response));
                            break;
                        case Const.IS_REQEUST_CREATED:
                            // if (activity.pHelper.getRequestId() != Const.NO_REQUEST
                            // && isAdded()) {
                            // Driver driverDetails = activity.pContent
                            // .getDriverDetail(response);
                            // AndyUtils.showCustomProgressRequestDialog(activity,
                            // getString(R.string.text_contacting), false,
                            // this, driverDetails);
                            // }
                            break;
                    }
                }
        }
    }

    @Override
    public void onProgressCancel() {

    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK) {
                    MapFragment mf = MapFragment.newInstance();
                    activity.setIconMenu(R.drawable.menu);
                    activity.addFragment(mf, false, Const.FRAGMENT_MAP);
                    return true;
                }
                return false;
            }
        });
    }

}
