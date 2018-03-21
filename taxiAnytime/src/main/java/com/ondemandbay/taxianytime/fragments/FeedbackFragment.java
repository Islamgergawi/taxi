package com.ondemandbay.taxianytime.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.util.HashMap;

/**
 * @author Elluminati elluminati.in
 */
public class FeedbackFragment extends BaseFragment {
    private EditText etComment;
    private RatingBar rtBar;
    private Button btnSubmit;
    private ImageView ivDriverImage;
    private Driver driver;
    private TextView tvDistance, tvTime, tvClientName;

    // private TextView tvFeedbackAmount;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driver = (Driver) getArguments().getParcelable(Const.DRIVER);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(getString(R.string.text_feedback));
        activity.tvTitle.setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.feedback, container, false);
        tvClientName = (TextView) view.findViewById(R.id.tvClientName);
        etComment = (EditText) view.findViewById(R.id.etComment);
        rtBar = (RatingBar) view.findViewById(R.id.ratingBar);
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        ivDriverImage = (ImageView) view.findViewById(R.id.ivDriverImage);
        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
        // tvFeedbackAmount = (TextView)
        // view.findViewById(R.id.tvFeedbackAmount);
        // tvDistance.setText(driver.getLastDistance());
        tvDistance.setText(driver.getBill().getDistance() + " "
                + driver.getBill().getUnit());
        tvTime.setText((int) (Double.parseDouble(driver.getBill().getTime()))
                + " " + getString(R.string.text_mins));
        // tvFeedbackAmount.setText(getString(R.string.text_price_unit)
        // + Double.parseDouble(driver.getBill().getTotal()));
        // tvTime.setText(driver.getLastTime());
        activity.btnNotification.setVisibility(View.GONE);
        btnSubmit.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (driver != null) {
            if (!TextUtils.isEmpty(driver.getPicture())) {
                new AQuery(activity).id(ivDriverImage).progress(R.id.pBar)
                        .image(driver.getPicture());
            } else {
                new AQuery(activity).id(ivDriverImage).progress(R.id.pBar)
                        .image(R.drawable.default_user);
            }
            tvClientName.setText(driver.getFirstName() + " "
                    + driver.getLastName());
            activity.showBillDialog(driver.getBill().getTimeCost(), driver
                            .getBill().getTotal(), driver.getBill().getDistanceCost(),
                    driver.getBill().getBasePrice(),
                    driver.getBill().getTime(), driver.getBill().getDistance(),
                    driver.getBill().getPromoBouns(), driver.getBill()
                            .getReferralBouns(), driver.getBill()
                            .getPricePerDistance(), driver.getBill()
                            .getPricePerTime(), driver.getBill().getUnit(), driver.getPayment_type());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                if (isValidate()) {
                    rating();
                } else
                    AndyUtils.showToast(
                            activity.getString(R.string.error_empty_rating),
                            activity);
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        if (rtBar.getRating() <= 0)
            return false;
        return true;
    }

    private void rating() {
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_rating), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.RATING);
        map.put(Const.Params.TOKEN, activity.pHelper.getSessionToken());
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.COMMENT, etComment.getText().toString());
        map.put(Const.Params.RATING, String.valueOf(((int) rtBar.getRating())));
        map.put(Const.Params.REQUEST_ID,
                String.valueOf(activity.pHelper.getRequestId()));
        new HttpRequester(activity, map, Const.ServiceCode.RATING, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.RATING, this, this));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.uberorg.fragments.UberBaseFragment#onTaskCompleted(java.lang.String,
     * int)
     */
    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.RATING:
                AndyUtils.removeCustomProgressDialog();
                if (activity.pContent.isSuccess(response)) {
                    activity.pHelper.clearRequestData();
                    AndyUtils.showToast(
                            getString(R.string.text_feedback_completed), activity);
                    activity.gotoMapFragment();
                }
                break;
        }
    }

	/*
     * (non-Javadoc)
	 * 
	 * @see com.uberorg.fragments.BaseFragmentRegister#OnBackPressed()
	 */
}
