package com.ondemandbay.taxianytime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.adapter.DrawerAdapter;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.db.DBHelper;
import com.ondemandbay.taxianytime.fragments.FeedbackFragment;
import com.ondemandbay.taxianytime.fragments.MapFragment;
import com.ondemandbay.taxianytime.fragments.MyBookingsFragment;
import com.ondemandbay.taxianytime.fragments.TripFragment;
import com.ondemandbay.taxianytime.models.ApplicationPages;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.models.Reffral;
import com.ondemandbay.taxianytime.models.User;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class MainDrawerActivity extends ActionBarBaseActivitiy implements
        DrawerListener {
    public DrawerAdapter adapter;
    // MenuDrawer mMenuDrawer;
    public DrawerLayout drawerLayout;
    public ListView listDrawer;
    // private ActionBarDrawerToggle drawerToggel;
    public PreferenceHelper pHelper;
    public ParseContent pContent;
    public boolean isBookingFragment = false;
    public BroadcastReceiver GpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final LocationManager manager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // do something
                removeGpsDialog();
            } else {
                // do something else
                if (isGpsDialogShowing) {
                    return;
                }
                ShowGpsDialog();
            }
        }
    };
    private ArrayList<ApplicationPages> listMenu;
    private boolean isDataRecieved = false, isRecieverRegistered = false,
            isNetDialogShowing = false, isGpsDialogShowing = false;
    private AlertDialog internetDialog, gpsAlertDialog, locationAlertDialog;
    public BroadcastReceiver internetConnectionReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isConnectedToInternet())
            {
                removeInternetDialog();

            }
            else
            {
                removeInternetDialog();
            }
            /*
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            @SuppressWarnings("static-access")
            NetworkInfo activeWIFIInfo = connectivityManager
                    .getNetworkInfo(connectivityManager.TYPE_WIFI);

            if (activeWIFIInfo.isConnected() || activeNetInfo.isConnected()) {
                removeInternetDialog();
            } else {
                if (isNetDialogShowing) {
                    return;
                }
                removeInternetDialog();
            }*/
        }
    };
    private DBHelper dbHelper;
    private AQuery aQuery;
    private LocationManager manager;
    private ImageView ivMenuProfile;
    private MyFontTextView tvMenuName;
    private ImageOptions imageOptions;
    private User user;

    // private MapFragment uberMapFragment;
    private boolean isLogoutCheck = true;
    private View headerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this, "6f0d48b8");
       Fabric.with(this, new Crashlytics());


        dbHelper = new DBHelper(getApplicationContext());
        aQuery = new AQuery(this);

        // mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        // mMenuDrawer.setContentView(R.layout.activity_map);
        // mMenuDrawer.setMenuView(R.layout.menu_drawer);
        // mMenuDrawer.setDropShadowEnabled(false);
        moveDrawerToTop();
        initActionBar();

        btnActionMenu.setVisibility(View.VISIBLE);
        setIcon(R.drawable.notification_box);

        imageOptions = new ImageOptions();
        imageOptions.memCache = true;
        imageOptions.fileCache = true;
        imageOptions.targetWidth = 200;
        imageOptions.fallback = R.drawable.default_user;

        setContentView(R.layout.activity_map);
        // Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        // setSupportActionBar(toolbar);
        pHelper = new PreferenceHelper(this);
        pContent = new ParseContent(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(this);
        listDrawer = (ListView) findViewById(R.id.left_drawer);
        listMenu = new ArrayList<ApplicationPages>();
        adapter = new DrawerAdapter(this, listMenu);
        headerView = getLayoutInflater().inflate(R.layout.menu_drawer, null);
        listDrawer.addHeaderView(headerView);
        listDrawer.setAdapter(adapter);

        ivMenuProfile = (ImageView) headerView.findViewById(R.id.ivMenuProfile);

        tvMenuName = (MyFontTextView) headerView.findViewById(R.id.tvMenuName);
        // tvMenuName.setText(user.getFname() + " " + user.getLname());
        getMenuItems();
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // drawerToggel = new ActionBarDrawerToggle(this, drawerLayout,
        // R.drawable.slide_btn, 0, 0);
        // drawerLayout.setDrawerListener(drawerToggel);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        // actionBar.setHomeButtonEnabled(true);
        // actionBar.setHomeAsUpIndicator(R.drawable.slide_btn);

        listDrawer.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v,
                                    final int position, long id) {
                if (position == 0)
                    return;
                drawerLayout.closeDrawer(listDrawer);
                // mMenuDrawer.closeMenu();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (position == 1) {
                            startActivity(new Intent(MainDrawerActivity.this,
                                    ProfileActivity.class));
                        } else if (position == 2) {
                            addFragment(new MyBookingsFragment(), false,
                                    Const.FRAGMENT_MYBOOKINGS);
                        }
                        else if (position == 3) {
                            startActivity(new Intent(MainDrawerActivity.this,
                                    ViewPaymentActivity.class));
                        } else if (position == 4) {
                            startActivity(new Intent(MainDrawerActivity.this,
                                    HistoryActivity.class));
                        } else if (position == 5) {
                            getReffrelaCode();
                        } else if (position == (listMenu.size())) {
                            if (isLogoutCheck) {
                                openLogoutDialog();
                                isLogoutCheck = false;
                                return;
                            }
                        } else {
                            Intent intent = new Intent(MainDrawerActivity.this,
                                    MenuDescActivity.class);
                            intent.putExtra(Const.Params.TITLE,
                                    listMenu.get(position - 1).getTitle());
                            intent.putExtra(Const.Params.CONTENT,
                                    listMenu.get(position - 1).getData());
                            startActivity(intent);
                        }
                    }
                }, 350);

            }
        });
        // mMenuDrawer.peekDrawer();
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.main, menu);
    // return true;
    // }

    @Override
    protected void onStart() {
        super.onStart();

    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ShowGpsDialog();
        } else {
            removeGpsDialog();
        }
        registerReceiver(internetConnectionReciever, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
        registerReceiver(GpsChangeReceiver, new IntentFilter(
                LocationManager.PROVIDERS_CHANGED_ACTION));
        isRecieverRegistered = true;
        if (AndyUtils.isNetworkAvailable(this)
                && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!isDataRecieved) {
                isDataRecieved = true;
                checkStatus();
                gotoMapFragment();
            }
        }
        user = dbHelper.getUser();
        if (user != null) {
            aQuery.id(ivMenuProfile).progress(R.id.pBar)
                    .image(user.getPicture(), imageOptions);

            tvMenuName.setText(user.getFname() + " " + user.getLname());
        }

        // getMenuItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard();
        // if (drawerToggel.onOptionsItemSelected(item)) {
        // return true;
        // }
        switch (item.getItemId()) {
            case android.R.id.home:
                // mMenuDrawer.toggleMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        // drawerToggel.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // drawerToggel.onConfigurationChanged(newConfig);
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
            case R.id.tvTitle:
                // mMenuDrawer.toggleMenu();
                drawerLayout.openDrawer(listDrawer);
                InputMethodManager imm = (InputMethodManager) this
                        .getSystemService(MainDrawerActivity.INPUT_METHOD_SERVICE);
                if (imm.isAcceptingText()) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        openExitDialog();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        super.onTaskCompleted(response, serviceCode);

        switch (serviceCode) {
            case Const.ServiceCode.GET_REQUEST_IN_PROGRESS:
                if (pContent.isSuccess(response)) {

                    pContent.parseCardAndPriceDetails(response);
                    if (pContent.getRequestInProgress(response) == Const.NO_REQUEST) {
                        AndyUtils.removeCustomProgressDialog();
                        gotoMapFragment();
                    } else {
                        pHelper.putRequestId(pContent.getRequestId(response));
                        getRequestStatus(String.valueOf(pHelper.getRequestId()));
                    }
                } else if (pContent.getErrorCode(response) == Const.INVALID_TOKEN) {
                    if (pHelper.getLoginBy().equalsIgnoreCase(Const.MANUAL))
                        login();
                    else
                        loginSocial(pHelper.getUserId(), pHelper.getLoginBy());
                } else if (pContent.getErrorCode(response) == Const.REQUEST_ID_NOT_FOUND) {
                    AndyUtils.removeCustomProgressDialog();
                    pHelper.clearRequestData();
                    gotoMapFragment();
                }
                break;
            case Const.ServiceCode.GET_REQUEST_STATUS:
                AndyUtils.removeCustomProgressDialog();
                if (pContent.isSuccess(response)) {
                    pContent.parseCardAndPriceDetails(response);
                    switch (pContent.checkRequestStatus(response)) {
                        case Const.IS_WALK_STARTED:
                        case Const.IS_WALKER_ARRIVED:
                        case Const.IS_COMPLETED:
                        case Const.IS_WALKER_STARTED:
                            Driver driver = pContent.getDriverDetail(response);
                            gotoTripFragment(driver);
                            break;

                        case Const.IS_WALKER_RATED:
                            gotoRateFragment(pContent.getDriverDetail(response));
                            break;
                        default:
                            gotoMapFragment();
                            break;
                    }

                } else if (pContent.getErrorCode(response) == Const.INVALID_TOKEN) {
                    login();
                } else if (pContent.getErrorCode(response) == Const.REQUEST_ID_NOT_FOUND) {
                    pHelper.clearRequestData();
                    gotoMapFragment();
                }
                break;
            case Const.ServiceCode.GET_PAGES:
                AppLog.Log(Const.TAG, "Pages::::" + response);
                listMenu.clear();
                listMenu = pContent.parsePages(listMenu, response);
                ApplicationPages applicationPages = new ApplicationPages();
                applicationPages.setData("");
                applicationPages.setId(listMenu.size());
                applicationPages.setTitle(getString(R.string.text_menu_logout));
                listMenu.add(applicationPages);
                adapter.notifyDataSetChanged();
                break;
            case Const.ServiceCode.GET_REFERREL:
                AppLog.Log("Refferal response::::::::::", response);
                if (pContent.isSuccess(response)) {
                    Reffral ref = pContent.parseReffrelCode(response);
                    if (ref != null) {
                        showRefferelDialog(ref);
                    }
                }
                AndyUtils.removeCustomProgressDialog();

                break;
            case Const.ServiceCode.LOGOUT:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(Const.TAG, "Pages::::" + response);
                if (pContent.isSuccess(response)) {
                    pHelper.Logout();
                    goToMainActivity();
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MapFragment frag = MapFragment.newInstance();
                    addFragment(frag, false, Const.FRAGMENT_MAP);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("test", "tes");
                }
                return;
            }
            // other 'case' lines to check for other
            //  permissions this app might request
        }
    }
    public boolean checkLocationPermission()
    {

String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission( android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            MapFragment frag = MapFragment.newInstance();
            addFragment(frag, false, Const.FRAGMENT_MAP);
             return true;
        } else {
            requestPermissions(permissions,100);
            return false;
          }

    }
    public void gotoMapFragment() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // only for gingerbread and newer versions
            checkLocationPermission();
        }
        else{
            MapFragment frag = MapFragment.newInstance();
            addFragment(frag, false, Const.FRAGMENT_MAP);
        }


    }

    public void gotoTripFragment(Driver driver) {
        TripFragment tripFrag = new TripFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.DRIVER, driver);
        tripFrag.setArguments(bundle);
        addFragment(tripFrag, false, Const.FRAGMENT_TRIP);
    }

    public void gotoRateFragment(Driver driver) {
        try {
            if (TextUtils.isEmpty(driver.getLastTime()))
                driver.setLastTime(0 + " " + getString(R.string.text_mins));
            if (TextUtils.isEmpty(driver.getLastDistance()))
                driver.setLastDistance(0.0 + " "
                        + getString(R.string.text_miles));
            FeedbackFragment feedBack = new FeedbackFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Const.DRIVER, driver);
            feedBack.setArguments(bundle);
            addFragmentWithStateLoss(feedBack, false, Const.FRAGMENT_FEEDBACK);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkStatus() {
        if (pHelper.getRequestId() == Const.NO_REQUEST) {
            getRequestInProgress();
        } else {
            getRequestStatus(String.valueOf(pHelper.getRequestId()));
        }

    }

    private void getRequestInProgress() {
        HashMap<String, String> map = new HashMap<String, String>();
        AppLog.Log(
                "MainDrawerActivity",
                Const.ServiceType.REQUEST_IN_PROGRESS + Const.Params.ID + "="
                        + new PreferenceHelper(this).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(this).getSessionToken());
        map.put(Const.URL,
                Const.ServiceType.REQUEST_IN_PROGRESS + Const.Params.ID + "="
                        + new PreferenceHelper(this).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(this).getSessionToken());
        new HttpRequester(this, map, Const.ServiceCode.GET_REQUEST_IN_PROGRESS,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REQUEST_IN_PROGRESS, this, this));
    }

    private void getRequestStatus(String requestId) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_REQUEST_STATUS
                + Const.Params.ID + "=" + pHelper.getUserId() + "&"
                + Const.Params.TOKEN + "=" + pHelper.getSessionToken() + "&"
                + Const.Params.REQUEST_ID + "=" + requestId);

        new HttpRequester(this, map, Const.ServiceCode.GET_REQUEST_STATUS,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REQUEST_STATUS, this, this));
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    private void getMenuItems() {

        listMenu.clear();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_PAGES + "?user_type=0");
        AppLog.Log(Const.TAG, Const.URL);

        new HttpRequester(this, map, Const.ServiceCode.GET_PAGES, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_PAGES, this, this));
        adapter.notifyDataSetChanged();
    }

    private void login() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    this);
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGIN);
        map.put(Const.Params.EMAIL, pHelper.getEmail());
        map.put(Const.Params.PASSWORD, pHelper.getPassword());
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
        map.put(Const.Params.DEVICE_TOKEN, pHelper.getDeviceToken());
        map.put(Const.Params.LOGIN_BY, Const.MANUAL);
        new HttpRequester(this, map, Const.ServiceCode.LOGIN, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.LOGIN, this, this));
    }

    private void loginSocial(String id, String loginType) {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    this);
            return;
        }
        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.text_signin), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGIN);
        map.put(Const.Params.SOCIAL_UNIQUE_ID, id);
        map.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);
        map.put(Const.Params.DEVICE_TOKEN,
                new PreferenceHelper(this).getDeviceToken());
        map.put(Const.Params.LOGIN_BY, loginType);
        new HttpRequester(this, map, Const.ServiceCode.LOGIN, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.LOGIN, this, this));
    }

    private void ShowGpsDialog() {
        AndyUtils.removeCustomProgressDialog();
        isGpsDialogShowing = true;
        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(
                MainDrawerActivity.this);
        gpsBuilder.setCancelable(false);
        gpsBuilder
                .setTitle(getString(R.string.dialog_no_gps))
                .setMessage(getString(R.string.dialog_no_gps_messgae))
                .setPositiveButton(getString(R.string.dialog_enable_gps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                removeGpsDialog();
                            }
                        })

                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeGpsDialog();
                                finish();
                            }
                        });
        gpsAlertDialog = gpsBuilder.create();
        gpsAlertDialog.show();
    }

    public void showLocationOffDialog() {

        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(
                MainDrawerActivity.this);
        gpsBuilder.setCancelable(false);
        gpsBuilder
                .setTitle(getString(R.string.dialog_no_location_service_title))
                .setMessage(getString(R.string.dialog_no_location_service))
                .setPositiveButton(
                        getString(R.string.dialog_enable_location_service),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                dialog.dismiss();
                                Intent viewIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(viewIntent);

                            }
                        })

                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                dialog.dismiss();
                                finish();
                            }
                        });
        locationAlertDialog = gpsBuilder.create();
        locationAlertDialog.show();
    }

    private void removeGpsDialog() {
        if (gpsAlertDialog != null && gpsAlertDialog.isShowing()) {
            gpsAlertDialog.dismiss();
            isGpsDialogShowing = false;
            gpsAlertDialog = null;

        }
    }

    private void removeInternetDialog() {
        if (internetDialog != null && internetDialog.isShowing()) {
            internetDialog.dismiss();
            isNetDialogShowing = false;
            internetDialog = null;

        }
    }

    private void showInternetDialog() {
        AndyUtils.removeCustomProgressDialog();
        isNetDialogShowing = true;
        AlertDialog.Builder internetBuilder = new AlertDialog.Builder(
                MainDrawerActivity.this);
        internetBuilder.setCancelable(false);
        internetBuilder
                .setTitle(getString(R.string.dialog_no_internet))
                .setMessage(getString(R.string.dialog_no_inter_message))
                .setPositiveButton(getString(R.string.dialog_enable_3g),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivity(intent);
                                removeInternetDialog();
                            }
                        })
                .setNeutralButton(getString(R.string.dialog_enable_wifi),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // User pressed Cancel button. Write
                                // Logic Here
                                startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS));
                                removeInternetDialog();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeInternetDialog();
                                finish();
                            }
                        });
        internetDialog = internetBuilder.create();
        internetDialog.show();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Mint.closeSession(this);
        if (isRecieverRegistered) {
            unregisterReceiver(internetConnectionReciever);
            unregisterReceiver(GpsChangeReceiver);
        }

    }

    private void showRefferelDialog(final Reffral ref) {

        final Dialog mDialog = new Dialog(this, R.style.MyDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.ref_code_layout);
        TextView tvReferralCode = (TextView) mDialog
                .findViewById(R.id.tvReferralCode);
        tvReferralCode.setText(ref.getReferralCode());

        TextView tvReferralEarn = (TextView) mDialog
                .findViewById(R.id.tvReferralEarn);
        tvReferralEarn.setText(getString(R.string.payment_unit)
                + ref.getBalanceAmount());

        TextView tvRefferalBonus = (TextView) mDialog
                .findViewById(R.id.tv_refferal_bonus);
        tvRefferalBonus.setText(ref.getReferralBonus());

        // Button btnCancel = (Button) mDialog.findViewById(R.id.btnCancel);
        Button btnShare = (Button) mDialog.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent
                        .putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                getResources().getString(
                                        R.string.text_take_look_at)
                                        + " "
                                        + getResources().getString(
                                        R.string.app_name)
                                        + " - "
                                        + "https://play.google.com/store/apps/details?id="
                                        + getPackageName()
                                        + getString(R.string.text_note_referral)
                                        + ref.getReferralCode());
                startActivity(Intent.createChooser(sharingIntent,
                        getResources().getString(R.string.text_share_ref_code)));
            }
        });
        mDialog.show();
    }

    private void getReffrelaCode() {
        AndyUtils.showCustomProgressDialog(this,
                getString(R.string.text_getting_ref_code), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_REFERRAL + Const.Params.ID
                + "=" + pHelper.getUserId() + "&" + Const.Params.TOKEN + "="
                + pHelper.getSessionToken());

        new HttpRequester(this, map, Const.ServiceCode.GET_REFERREL, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REFERREL, this, this));
    }

    @SuppressWarnings("deprecation")
    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                        context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);

            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void openLogoutDialog() {
        final Dialog mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.logout);
        mDialog.setCancelable(false);
        mDialog.findViewById(R.id.tvLogoutOk).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        logout();
                        isLogoutCheck = false;
                    }
                });
        mDialog.findViewById(R.id.tvLogoutCancel).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        isLogoutCheck = true;
                    }
                });
        mDialog.show();
    }

    private void logout() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    this);
            return;
        }
        AndyUtils.showCustomProgressDialog(this,
                getString(R.string.progress_loading), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.LOGOUT);
        map.put(Const.Params.ID, pHelper.getUserId());
        map.put(Const.Params.TOKEN, pHelper.getSessionToken());
        new HttpRequester(this, map, Const.ServiceCode.LOGOUT, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.LOGOUT, this, this));
    }

    private void moveDrawerToTop() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(
                R.layout.drawer_layout, null); // "null" is important.

        // HACK: "steal" the first child of decor view
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        LinearLayout container = (LinearLayout) drawer
                .findViewById(R.id.llContent); // This is the container we
        // defined just now.
        container.addView(child, 0);
        drawer.findViewById(R.id.left_drawer).setPadding(0,
                (actionBar.getHeight() + getStatusBarHeight()), 0, 0);

        // Make the drawer replace the first child
        decor.addView(drawer);
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);
        // actionBar.setHomeButtonEnabled(true);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void gotoBookingDetail(Booking booking) {
        Intent in = new Intent(MainDrawerActivity.this,
                BookingDetailsActivity.class);
        in.putExtra("bookingObj", booking);
        startActivity(in);
    }

    @Override
    public void onDrawerClosed(View arg0) {
    }

    @Override
    public void onDrawerOpened(View view) {
        InputMethodManager imm = (InputMethodManager) this
                .getSystemService(MainDrawerActivity.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDrawerSlide(View view, float arg1) {
    }

    @Override
    public void onDrawerStateChanged(int arg0) {
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) (MainDrawerActivity.this).getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
