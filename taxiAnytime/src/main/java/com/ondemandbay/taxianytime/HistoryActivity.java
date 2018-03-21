/**
 *
 */
package com.ondemandbay.taxianytime;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.hb.views.PinnedSectionListView;
import com.ondemandbay.taxianytime.adapter.HistoryAdapter;
import com.ondemandbay.taxianytime.models.History;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class HistoryActivity extends ActionBarBaseActivitiy implements
        OnItemClickListener {
    Calendar cal = Calendar.getInstance();
    int day;
    int month;
    int year;
    DatePickerDialog fromPiker;
    DatePickerDialog toPiker;
    Date fromDate, toDate;
    private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
    private PinnedSectionListView lvHistory;
    private HistoryAdapter historyAdapter;
    private ArrayList<History> historyList;
    private ArrayList<History> historyListOrg;
    private PreferenceHelper preferenceHelper;
    private ParseContent parseContent;
    private ImageView tvNoHistory;
    private ArrayList<Date> dateList = new ArrayList<Date>();
    private TextView fromDateBtn;
    private TextView toDateBtn;
    private OnDateSetListener dateset;
    private String userDate;

    /*
     * (non-Javadoc)
     *
     * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_history);
        setIconMenu(R.drawable.back);
        btnNotification.setVisibility(View.GONE);
        setTitle(getString(R.string.text_history));
        // setIcon(R.drawable.back);
        lvHistory = (PinnedSectionListView) findViewById(R.id.lvHistory);
        lvHistory.setOnItemClickListener(this);
        fromDateBtn = (TextView) findViewById(R.id.fromDateBtn);
        toDateBtn = (TextView) findViewById(R.id.toDateBtn);
        fromDateBtn.setOnClickListener(this);
        toDateBtn.setOnClickListener(this);
        historyList = new ArrayList<History>();

        tvNoHistory = (ImageView) findViewById(R.id.ivEmptyView);
        findViewById(R.id.btnSearch).setOnClickListener(this);
        //
        // actionBar.setDisplayHomeAsUpEnabled(true);
        // actionBar.setHomeButtonEnabled(true);
        // actionBar.setTitle(getString(R.string.text_history));
        preferenceHelper = new PreferenceHelper(this);
        parseContent = new ParseContent(this);
        dateList = new ArrayList<Date>();
        historyListOrg = new ArrayList<History>();

        // historyAdapter = new HistoryAdapter(this, historyListOrg,
        // mSeparatorsSet);
        // lvHistory.setAdapter(historyAdapter);

        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        fromDate = new Date();
        toDate = new Date();

        getHistory();

        dateset = new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                userDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

                try {
                    if (view == fromPiker.getDatePicker()) {
                        fromDateBtn.setText(userDate);
                        fromDate = sdf.parse(userDate);
                    } else {
                        toDateBtn.setText(userDate);
                        toDate = sdf.parse(userDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        fromPiker = new DatePickerDialog(this, dateset, year, month, day);
        fromPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
        toPiker = new DatePickerDialog(this, dateset, year, month, day);
        toPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    /**
     *
     */
    private void getHistory() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.dialog_no_inter_message),
                    this);
            return;
        }
        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.progress_getting_history),
                false, null);
        HashMap<String, String> map = new HashMap<String, String>();

        if (fromDateBtn.getText().toString()
                .equals(getString(R.string.text_select_date))
                && toDateBtn.getText().toString()
                .equals(getString(R.string.text_select_date))) {
            map.put(Const.URL,
                    Const.ServiceType.HISTORY + Const.Params.ID + "="
                            + preferenceHelper.getUserId() + "&"
                            + Const.Params.TOKEN + "="
                            + preferenceHelper.getSessionToken());
        } else {
            map.put(Const.URL,
                    Const.ServiceType.HISTORY + Const.Params.ID + "="
                            + preferenceHelper.getUserId() + "&"
                            + Const.Params.TOKEN + "="
                            + preferenceHelper.getSessionToken() + "&"
                            + Const.Params.FROM_DATE + "="
                            + fromDateBtn.getText().toString().trim() + "&"
                            + Const.Params.TO_DATE + "="
                            + toDateBtn.getText().toString().trim());
        }
        new HttpRequester(this, map, Const.ServiceCode.HISTORY, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.HISTORY, this, this));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
     * .AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {

        if (mSeparatorsSet.contains(position))
            return;

        History history = historyListOrg.get(position);
        Intent detailIntent = new Intent(this, HistoryDetailsActivity.class);
        detailIntent.putExtra(Const.HISTORY_DETAILS, history);
        startActivity(detailIntent);
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
            case Const.ServiceCode.HISTORY:
                AppLog.Log("TAG", "History Response :" + response);
                if (!parseContent.isSuccess(response)) {
                    return;
                }
                historyListOrg.clear();
                historyList.clear();
                dateList.clear();
                parseContent.parseHistory(response, historyList);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    final Calendar cal = Calendar.getInstance();

                    parseContent.parseHistory(response, historyList);

                    Collections.sort(historyList, new Comparator<History>() {
                        @Override
                        public int compare(History o1, History o2) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(
                                    "yyyy-MM-dd hh:mm:ss");
                            try {
                                String firstStrDate = o1.getDate();
                                String secondStrDate = o2.getDate();
                                Date date2 = dateFormat.parse(secondStrDate);
                                Date date1 = dateFormat.parse(firstStrDate);
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });

                    HashSet<Date> listToSet = new HashSet<Date>();
                    for (int i = 0; i < historyList.size(); i++) {
                        AppLog.Log("date", historyList.get(i).getDate() + "");
                        if (listToSet.add(sdf.parse(historyList.get(i).getDate()))) {
                            dateList.add(sdf.parse(historyList.get(i).getDate()));
                        }

                    }

                    for (int i = 0; i < dateList.size(); i++) {

                        cal.setTime(dateList.get(i));
                        History item = new History();
                        item.setDate(sdf.format(dateList.get(i)));
                        historyListOrg.add(item);

                        mSeparatorsSet.add(historyListOrg.size() - 1);
                        for (int j = 0; j < historyList.size(); j++) {
                            Calendar messageTime = Calendar.getInstance();
                            messageTime.setTime(sdf.parse(historyList.get(j)
                                    .getDate()));
                            if (cal.getTime().compareTo(messageTime.getTime()) == 0) {
                                historyListOrg.add(historyList.get(j));
                            }
                        }
                    }

                    if (historyList.size() > 0) {
                        lvHistory.setVisibility(View.VISIBLE);
                        tvNoHistory.setVisibility(View.GONE);
                    } else {
                        lvHistory.setVisibility(View.GONE);
                        tvNoHistory.setVisibility(View.VISIBLE);
                    }
                    Log.i("historyListOrg size  ", "" + historyListOrg.size());

                    historyAdapter = new HistoryAdapter(this, historyListOrg,
                            mSeparatorsSet);
                    lvHistory.setAdapter(historyAdapter);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                onBackPressed();
                break;
            case R.id.fromDateBtn:
                fromPiker.show();
                break;
            case R.id.toDateBtn:
                toPiker.show();
                break;
            case R.id.btnSearch:
                if (toDate.before(fromDate)) {
                    AndyUtils.showToast(
                            getResources()
                                    .getString(R.string.validate_history_date),
                            getApplicationContext());
                    return;
                } else {
                    getHistory();
                }
                break;
            default:
                break;
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
