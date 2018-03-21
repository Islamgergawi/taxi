package com.ondemandbay.taxianytime.parse;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.db.DBHelper;
import com.ondemandbay.taxianytime.maputils.PolyLineUtils;
import com.ondemandbay.taxianytime.models.ApplicationPages;
import com.ondemandbay.taxianytime.models.Bill;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.models.Card;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.models.DriverLocation;
import com.ondemandbay.taxianytime.models.History;
import com.ondemandbay.taxianytime.models.Reffral;
import com.ondemandbay.taxianytime.models.Route;
import com.ondemandbay.taxianytime.models.Step;
import com.ondemandbay.taxianytime.models.User;
import com.ondemandbay.taxianytime.models.VehicalType;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.ondemandbay.taxianytime.utils.ReadFiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Elluminati elluminati.in
 */
public class ParseContent {
    public static final String DATE = "date";
    private final String KEY_SUCCESS = "success";
    private final String KEY_ERROR = "error";
    // private final String AGE = "age";
    private final String TYPE = "type";
    private final String MIN_FARE = "min_fare";
    private final String MAX_SIZE = "max_size";
    // private final String NOTES = "notes";
    // private final String IMAGE_URL = "image_url";
    // private final String THINGS_ID = "thing_id";
    private final String KEY_ERROR_CODE = "error_code";
    private final String KEY_ERROR_MESSAGES = "error_messages";
    private final String KEY_WALKER = "walker";
    private final String BILL = "bill";
    private final String KEY_BILL = "bill";
    private final String IS_WALKER_STARTED = "is_walker_started";
    private final String IS_WALKER_ARRIVED = "is_walker_arrived";
    private final String IS_WALK_STARTED = "is_walk_started";
    private final String IS_WALKER_RATED = "is_walker_rated";
    private final String IS_COMPLETED = "is_completed";
    private final String STATUS = "status";
    private final String CONFIRMED_WALKER = "confirmed_walker";
    private final String PAYMENT_TYPE = "payment_type";
    private final String TIME = "time";
    private final String BASE_PRICE = "base_price";
    private final String BASE_DISTANCE = "base_distance";
    private final String DISTANCE_COST = "distance_cost";
    private final String DISTANCE = "distance";
    private final String UNIT = "unit";
    private final String TIME_COST = "time_cost";
    private final String TOTAL = "total";
    private final String CURRENCY = "currency";
    private final String IS_PAID = "is_paid";
    private final String START_TIME = "start_time";
    private final String TYPES = "types";
    private final String ID = "id";
    private final String ICON = "icon";
    private final String IS_DEFAULT = "is_default";
    private final String PRICE_PER_UNIT_TIME = "price_per_unit_time";
    private final String PRICE_PER_UNIT_DISTANCE = "price_per_unit_distance";
    // private final String STRIPE_TOKEN = "stripe_token";
    private final String LAST_FOUR = "last_four";
    // private final String CREATED_AT = "created_at";
    // private final String UPDATED_AT = "updated_at";
    private final String OWNER_ID = "owner_id";
    private final String CARD_TYPE = "card_type";
    private final String PAYMENTS = "payments";
    private final String REQUESTS = "requests";
    private final String WALKER = "walker";
    private final String CUSTOMER_ID = "customer_id";
    private final String REFERED_USER_BONUS = "refered_user_bonus";
    // private final String REFERRAL_USER_BONUS="refereel_user_bonus";
    private final String REFERRAL_CODE = "referral_code";
    private final String TOTAL_REFERRALS = "total_referrals";
    private final String AMOUNT_EARNED = "total_referrals";
    private final String AMOUNT_SPENT = "total_referrals";
    private final String BALANCE_AMOUNT = "balance_amount";
    private final String WALKERS = "walker_list";
    private final String PROMO_CODE = "promo_code";
    private final String PROMO_BONUS = "promo_bonus";
    private final String REFERRAL_BONUS = "referral_bonus";
    private final String ROUTES = "routes";
    private final String LEGS = "legs";
    private final String TEXT = "text";
    private final String VALUE = "value";
    private final String DURATION = "duration";
    private final String START_ADDRESS = "start_address";
    private final String END_ADDRESS = "end_address";
    private final String START_LOCATION = "start_location";
    private final String END_LOCATION = "end_location";
    private final String LATITUDE = "lat";
    private final String LONGITUDE = "lng";
    private final String STEPS = "steps";
    private final String POLYLINE = "polyline";
    private final String POINTS = "points";
    private final String HTML_INSTRUCTIONS = "html_instructions";
    private final String PHONE_CODE = "phone-code";
    private final String NAME = "name";
    private final String DEST_LATITUDE = "dest_latitude";
    private final String DEST_LONGITUDE = "dest_longitude";
    private final String KMS = "kms";
    private final String CARD_DETAILS = "card_details";
    private final String CARD_ID = "card_id";
    // private final String CHARGE_DETAILS = "charge_details";
    // private final String DISTANCE_PRICE = "distance_price";
    // private final String OWNER = "owner";
    // private final String PAYMENT_TYPE = "payment_type";
    private final String RESULTS = "results";
    private final String ALL_SCHEDULED_REQUEST = "all_scheduled_requests";
    private final String SRC_ADDRESS = "src_address";
    private final String TAG_FUTURE = "future";
    private final String TAG_ONGOING = "ongoing";
    private Activity activity;
    private PreferenceHelper preferenceHelper;

    // private final String DRIVER_STATUS = "status";

    public ParseContent(Activity activity) {
        this.activity = activity;
        preferenceHelper = new PreferenceHelper(activity);
    }

    public Route parseRoute(String response, Route routeBean) {

        try {
            Step stepBean;
            JSONObject jObject = new JSONObject(response);
            JSONArray jArray = jObject.getJSONArray(ROUTES);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject innerjObject = jArray.getJSONObject(i);
                if (innerjObject != null) {
                    JSONArray innerJarry = innerjObject.getJSONArray(LEGS);
                    for (int j = 0; j < innerJarry.length(); j++) {

                        JSONObject jObjectLegs = innerJarry.getJSONObject(j);
                        routeBean.setDistanceText(jObjectLegs.getJSONObject(
                                Const.Params.DISTANCE).getString(TEXT));
                        routeBean.setDistanceValue(jObjectLegs.getJSONObject(
                                Const.Params.DISTANCE).getInt(VALUE));

                        routeBean.setDurationText(jObjectLegs.getJSONObject(
                                DURATION).getString(TEXT));
                        routeBean.setDurationValue(jObjectLegs.getJSONObject(
                                DURATION).getInt(VALUE));

                        routeBean.setStartAddress(jObjectLegs
                                .getString(START_ADDRESS));
                        routeBean.setEndAddress(jObjectLegs
                                .getString(END_ADDRESS));

                        routeBean.setStartLat(jObjectLegs.getJSONObject(
                                START_LOCATION).getDouble(LATITUDE));
                        routeBean.setStartLon(jObjectLegs.getJSONObject(
                                START_LOCATION).getDouble(LONGITUDE));

                        routeBean.setEndLat(jObjectLegs.getJSONObject(
                                END_LOCATION).getDouble(LATITUDE));
                        routeBean.setEndLon(jObjectLegs.getJSONObject(
                                END_LOCATION).getDouble(LONGITUDE));

                        JSONArray jstepArray = jObjectLegs.getJSONArray(STEPS);
                        if (jstepArray != null) {
                            for (int k = 0; k < jstepArray.length(); k++) {
                                stepBean = new Step();
                                JSONObject jStepObject = jstepArray
                                        .getJSONObject(k);
                                if (jStepObject != null) {

                                    stepBean.setHtml_instructions(jStepObject
                                            .getString(HTML_INSTRUCTIONS));
                                    stepBean.setStrPoint(jStepObject
                                            .getJSONObject(POLYLINE).getString(
                                                    POINTS));
                                    stepBean.setStartLat(jStepObject
                                            .getJSONObject(START_LOCATION)
                                            .getDouble(LATITUDE));
                                    stepBean.setStartLon(jStepObject
                                            .getJSONObject(START_LOCATION)
                                            .getDouble(LONGITUDE));
                                    stepBean.setEndLat(jStepObject
                                            .getJSONObject(END_LOCATION)
                                            .getDouble(LATITUDE));
                                    stepBean.setEndLong(jStepObject
                                            .getJSONObject(END_LOCATION)
                                            .getDouble(LONGITUDE));

                                    stepBean.setListPoints(new PolyLineUtils()
                                            .decodePoly(stepBean.getStrPoint()));
                                    routeBean.getListStep().add(stepBean);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routeBean;
    }

    public boolean isSuccessWithStoreId(String response) {
        AppLog.Log(Const.TAG, response);
        if (TextUtils.isEmpty(response))
            return false;
        try {
            System.out.println(response);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                preferenceHelper.putUserId(jsonObject
                        .getString(Const.Params.ID));
                preferenceHelper.putSessionToken(jsonObject
                        .getString(Const.Params.TOKEN));
                preferenceHelper.putEmail(jsonObject
                        .optString(Const.Params.EMAIL));
                preferenceHelper.putLoginBy(jsonObject
                        .getString(Const.Params.LOGIN_BY));
                preferenceHelper.putReferee(jsonObject
                        .getInt(Const.Params.IS_REFEREE));
                if (!preferenceHelper.getLoginBy().equalsIgnoreCase(
                        Const.MANUAL)) {
                    preferenceHelper.putSocialId(jsonObject
                            .getString(Const.Params.SOCIAL_UNIQUE_ID));
                }
                return true;
            } else {
                AndyUtils.showErrorToast(
                        jsonObject.getJSONArray(KEY_ERROR_MESSAGES).getInt(0),
                        activity);

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User parseUserAndStoreToDb(String response) {
        User user = null;
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                user = new User();
                DBHelper dbHelper = new DBHelper(activity);
                user.setUserId(jsonObject.getInt(Const.Params.ID));
                user.setEmail(jsonObject.optString(Const.Params.EMAIL));
                user.setFname(jsonObject.getString(Const.Params.FIRSTNAME));
                user.setLname(jsonObject.getString(Const.Params.LAST_NAME));

                user.setAddress(jsonObject.getString(Const.Params.ADDRESS));
                user.setBio(jsonObject.getString(Const.Params.BIO));
                user.setZipcode(jsonObject.getString(Const.Params.ZIPCODE));
                user.setPicture(jsonObject.getString(Const.Params.PICTURE));
                user.setContact(jsonObject.getString(Const.Params.PHONE));
                dbHelper.createUser(user);

            } else {


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean isSuccess(String response) {
        if (TextUtils.isEmpty(response))
            return false;
        try {
            // AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                return true;
            } else {
                // AndyUtils.showToast(jsonObject.getString(KEY_ERROR),
                // activity);
                AndyUtils.showErrorToast(
                        jsonObject.getJSONArray(KEY_ERROR_MESSAGES).getInt(0),
                        activity);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public DriverLocation getDriverLocation(String response) {
        DriverLocation driverLocation = null;
        LatLng latLng = null;
        if (TextUtils.isEmpty(response))
            return null;
        AppLog.Log(Const.TAG, response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            driverLocation = new DriverLocation();
            latLng = new LatLng(jsonObject.getDouble(Const.Params.LATITUDE),
                    jsonObject.getDouble(Const.Params.LONGITUDE));
            driverLocation.setLatLng(latLng);
            driverLocation.setDistance(new DecimalFormat("0.00").format(Double
                    .parseDouble(jsonObject.getString(DISTANCE))));
            driverLocation.setBearing(jsonObject
                    .getDouble(Const.Params.BEARING));
            driverLocation.setUnit(jsonObject.getString(UNIT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return driverLocation;
    }

    public int getErrorCode(String response) {
        if (TextUtils.isEmpty(response))
            return 0;
        try {
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getInt(KEY_ERROR_CODE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getMessage(String response) {
        if (TextUtils.isEmpty(response))
            return "";
        try {
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString(KEY_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    public int checkRequestStatus(String response) {
        int status = Const.NO_REQUEST;
        try {
            // AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt(CONFIRMED_WALKER) == 0
                    && jsonObject.getInt(STATUS) == 0) {
                return Const.IS_REQEUST_CREATED;
            } else if (jsonObject.getInt(CONFIRMED_WALKER) == 0
                    && jsonObject.getInt(STATUS) == 1) {
                return Const.NO_REQUEST;
            } else if (jsonObject.getInt(CONFIRMED_WALKER) != 0
                    && jsonObject.getInt(STATUS) == 1) {

                if (jsonObject.getInt(IS_WALKER_STARTED) == 0) {
                    status = Const.IS_WALKER_STARTED;
                } else if (jsonObject.getInt(IS_WALKER_ARRIVED) == 0) {
                    status = Const.IS_WALKER_ARRIVED;
                } else if (jsonObject.getInt(IS_WALK_STARTED) == 0) {
                    status = Const.IS_WALK_STARTED;
                } else if (jsonObject.getInt(IS_COMPLETED) == 0) {
                    status = Const.IS_COMPLETED;
                } else if (jsonObject.getInt(IS_WALKER_RATED) == 0) {
                    status = Const.IS_WALKER_RATED;
                }
            }
            preferenceHelper.putPromoCode(jsonObject.optString(PROMO_CODE));
            String time = jsonObject.optString(START_TIME);
            if (!TextUtils.isEmpty(time)) {
                try {
                    TimeZone.setDefault(TimeZone.getDefault());
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.ENGLISH).parse(time);
                    AppLog.Log("TAG", "START DATE---->" + date.toString()
                            + " month:" + date.getMonth());
                    preferenceHelper.putRequestTime(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (jsonObject.getString(DEST_LATITUDE).length() != 0) {
                // AndyUtils.showToast("get dest", activity);
                preferenceHelper.putClientDestination(new LatLng(jsonObject
                        .getDouble(DEST_LATITUDE), jsonObject
                        .getDouble(DEST_LONGITUDE)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public Bill parseBllingInfo(String response) {
        Bill bill = null;
        try {
            JSONObject jsonObject = new JSONObject(response)
                    .getJSONObject(KEY_BILL);
            bill = new Bill();
            bill.setBasePrice(jsonObject.getString(BASE_PRICE));
            double distance = Double
                    .parseDouble(jsonObject.getString(DISTANCE));
            // bill.setDistance(jsonObject.getString(DISTANCE));
            bill.setUnit(jsonObject.getString(UNIT));
            if (bill.getUnit().equalsIgnoreCase(KMS)) {
                distance = distance * 0.62137;
            }
            bill.setDistance(new DecimalFormat("0.00").format(distance));
            bill.setDistanceCost(jsonObject.getString(DISTANCE_COST));
            bill.setTime(jsonObject.getString(TIME));
            bill.setTimeCost(jsonObject.getString(TIME_COST));
            bill.setIsPaid(jsonObject.getString(IS_PAID));
            bill.setTotal(jsonObject.getString(TOTAL));
        } catch (JSONException e) {
            bill = null;
            e.printStackTrace();
        }
        return bill;
    }

    public Reffral parseReffrelCode(String response) {
        Reffral reffral = null;
        try {
            JSONObject jsonObject = new JSONObject(response);

            reffral = new Reffral();

            reffral.setReferralBonus(jsonObject.getString(REFERED_USER_BONUS));
            reffral.setReferralCode(jsonObject.getString(REFERRAL_CODE));
            reffral.setAmountSpent(jsonObject.getString(AMOUNT_SPENT));
            reffral.setBalanceAmount(jsonObject.getString(BALANCE_AMOUNT));
            reffral.setTotalReferrals(jsonObject.getString(TOTAL_REFERRALS));
            reffral.setAmountEarned(jsonObject.getString(AMOUNT_EARNED));
        } catch (JSONException e) {
            reffral = null;
            e.printStackTrace();
        }
        return reffral;
    }

    public void getNextDefaultCard(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jArray = jsonObject.getJSONArray(PAYMENTS);
            if (jArray.length() > 0) {
                JSONObject obj = jArray.getJSONObject(0);
                preferenceHelper.putDefaultCard(obj.getInt(CARD_ID));
                preferenceHelper.putDefaultCardNo(obj.getString(LAST_FOUR));
                preferenceHelper.putDefaultCardType(obj.getString(CARD_TYPE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Booking> parseBookingList(String response,
                                               ArrayList<Booking> list) {
        list.clear();

        if (TextUtils.isEmpty(response)) {
            return list;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {

                JSONArray jsonArray = jsonObject
                        .getJSONArray(ALL_SCHEDULED_REQUEST);
                JSONArray jArray = jsonObject.getJSONArray(REQUESTS);

                if (jArray.length() > 0) {
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject object = jArray.getJSONObject(i);
                        int requestId = object.optInt(Const.Params.REQUEST_ID);
                        // new
                        // PreferenceHelper(activity).putRequestId(requestId);
                        Booking booking = new Booking();
                        booking.setRequestId(requestId);
                        booking.setSource(object.optString(SRC_ADDRESS));
                        booking.setVehicleType(object
                                .optString(Const.Params.TYPE_NAME));
                        booking.setRequestCreatedTime(object
                                .getString(Const.Params.CURRENT_DATE));
                        booking.setMapImage(object
                                .optString(Const.Params.BOOKING_MAP));
                        booking.setIsFutureRequest(false);
                        list.add(booking);
                    }
                }

                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Booking booking = new Booking();
                        booking.setId(object.optInt(OWNER_ID));
                        booking.setRequestId((object
                                .getInt(Const.Params.REQ_ID)));
                        booking.setSource(object.optString(SRC_ADDRESS));
                        booking.setDest(object
                                .getString(Const.Params.DEST_ADDRESS));
                        booking.setStartTime(object.optString(START_TIME));
                        booking.setMapImage(object
                                .optString(Const.Params.BOOKING_MAP));
                        booking.setVehicleType(object
                                .optString(Const.Params.TYPE_NAME));
                        booking.setIsFutureRequest(true);
                        list.add(booking);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Driver getDriverInfo(String response) {
        Driver driver = null;
        try {
            driver = new Driver();
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response)
                    .getJSONObject(KEY_WALKER);
            driver.setBio(jsonObject.getString(Const.Params.BIO));
            driver.setFirstName(jsonObject.getString(Const.Params.FIRSTNAME));
            driver.setLastName(jsonObject.getString(Const.Params.LAST_NAME));
            driver.setPhone(jsonObject.getString(Const.Params.PHONE));
            driver.setPicture(jsonObject.getString(Const.Params.PICTURE));
            driver.setLatitude(jsonObject.getDouble(Const.Params.LATITUDE));
            driver.setLongitude(jsonObject.getDouble(Const.Params.LONGITUDE));
            driver.setRating(jsonObject.getDouble(Const.Params.RATING));
            driver.setCarModel(jsonObject.getString(Const.Params.TAXI_MODEL));
            driver.setCarNumber(jsonObject.getString(Const.Params.TAXI_NUMBER));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return driver;
    }

    public Driver getDriverDetail(String response) {
        Driver driver = null;
        try {
            driver = new Driver();
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response)
                    .getJSONObject(KEY_WALKER);
            driver.setBio(jsonObject.getString(Const.Params.BIO));
            driver.setFirstName(jsonObject.getString(Const.Params.FIRSTNAME));
            driver.setLastName(jsonObject.getString(Const.Params.LAST_NAME));
            driver.setPhone(jsonObject.getString(Const.Params.PHONE));
            driver.setPicture(jsonObject.getString(Const.Params.PICTURE));
            driver.setLatitude(jsonObject.getDouble(Const.Params.LATITUDE));
            driver.setLongitude(jsonObject.getDouble(Const.Params.LONGITUDE));
            driver.setRating(jsonObject.getDouble(Const.Params.RATING));
            driver.setCarModel(jsonObject.getString(Const.Params.TAXI_MODEL));
            driver.setCarNumber(jsonObject.getString(Const.Params.TAXI_NUMBER));
            JSONObject jsonObjectBill = new JSONObject(response)
                    .optJSONObject(BILL);
            if (jsonObjectBill != null) {
                Bill bill = new Bill();
                bill.setUnit(jsonObjectBill.getString(UNIT));
                double distance = Double.parseDouble(jsonObjectBill
                        .getString(DISTANCE));
                if (bill.getUnit().equalsIgnoreCase(KMS)) {
                    distance = distance * 0.62137;

                }
                bill.setDistance(new DecimalFormat("0.00").format(distance));
                bill.setTime(jsonObjectBill.getString(TIME));
                bill.setBasePrice(jsonObjectBill.getString(BASE_PRICE));
                bill.setTimeCost(jsonObjectBill.getString(TIME_COST));
                bill.setDistanceCost(jsonObjectBill.getString(DISTANCE_COST));
                bill.setTotal(jsonObjectBill.getString(TOTAL));
                bill.setIsPaid(jsonObjectBill.getString(IS_PAID));
                bill.setPromoBouns(jsonObjectBill.getString(PROMO_BONUS));
                bill.setReferralBouns(jsonObjectBill.getString(REFERRAL_BONUS));
                bill.setPricePerDistance(jsonObjectBill
                        .getString(PRICE_PER_UNIT_DISTANCE));
                driver.setPayment_type(jsonObjectBill.getInt(PAYMENT_TYPE));
                bill.setPricePerTime(jsonObjectBill
                        .getString(PRICE_PER_UNIT_TIME));
                driver.setBill(bill);
            }
            driver.setBearing(jsonObject.optDouble(Const.Params.BEARING));
            // driver.getBill().setUnit(object.getString(UNIT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return driver;
    }

    public ArrayList<LatLng> parsePathRequest(String response,
                                              ArrayList<LatLng> points) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                JSONArray jsonArray = jsonObject
                        .getJSONArray(Const.Params.LOCATION_DATA);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    points.add(new LatLng(Double.parseDouble(json
                            .getString(Const.Params.LATITUDE)),
                            Double.parseDouble(json
                                    .getString(Const.Params.LONGITUDE))));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return points;
    }

    public int getRequestInProgress(String response) {
        if (TextUtils.isEmpty(response))
            return Const.NO_REQUEST;
        try {
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                int requestId = jsonObject.getInt(Const.Params.REQUEST_ID);
                new PreferenceHelper(activity).putRequestId(requestId);
                return requestId;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Const.NO_REQUEST;
    }

    public int getRequestId(String response) {
        if (TextUtils.isEmpty(response))
            return Const.NO_REQUEST;
        try {
            AppLog.Log(Const.TAG, response);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                int requestId = jsonObject.getInt(Const.Params.REQUEST_ID);
                new PreferenceHelper(activity).putRequestId(requestId);
                return requestId;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Const.NO_REQUEST;
    }

    public ArrayList<String> parseCountryCodes() {
        String response = "";
        ArrayList<String> list = new ArrayList<String>();
        try {
            response = ReadFiles.readRawFileAsString(activity,
                    R.raw.countrycodes);

            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                list.add(object.getString(PHONE_CODE) + " "
                        + object.getString(NAME));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<VehicalType> parseTypes(String response,
                                             ArrayList<VehicalType> list) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(TYPES);
                for (int i = 0; i < jsonArray.length(); i++) {
                    VehicalType type = new VehicalType();
                    JSONObject typeJson = jsonArray.getJSONObject(i);
                    type.setBasePrice(typeJson.getDouble(BASE_PRICE));
                    type.setBaseDistance(typeJson.getInt(BASE_DISTANCE));
                    type.setUnit(typeJson.getString(UNIT));
                    type.setIcon(typeJson.getString(ICON));
                    type.setId(typeJson.getInt(ID));
                    type.setName(typeJson.getString(NAME));
                    type.setPricePerUnitDistance(typeJson
                            .getDouble(PRICE_PER_UNIT_DISTANCE));
                    type.setPricePerUnitTime(typeJson
                            .getDouble(PRICE_PER_UNIT_TIME));
                    type.setMinFare(typeJson.optString(MIN_FARE));
                    type.setMaxSize(typeJson.optString(MAX_SIZE));
                    list.add(type);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;

    }

    public ArrayList<ApplicationPages> parsePages(
            ArrayList<ApplicationPages> listMenu, String response) {
        listMenu.clear();

        ApplicationPages applicationPages = new ApplicationPages();
        applicationPages.setId(-1);
        applicationPages.setTitle(activity
                .getString(R.string.text_menu_profile));
        applicationPages.setData("");
        listMenu.add(applicationPages);

        applicationPages = new ApplicationPages();
        applicationPages.setId(-5);
        applicationPages
                .setTitle(activity.getString(R.string.text_my_bookings));
        applicationPages.setData("");
        listMenu.add(applicationPages);

        // applicationPages = new ApplicationPages();
        // applicationPages.setId(-6);
        // applicationPages.setTitle(activity.getString(R.string.text_event));
        // applicationPages.setData("");
        // listMenu.add(applicationPages);

        applicationPages = new ApplicationPages();
        applicationPages.setId(-2);
        applicationPages.setTitle(activity
                .getString(R.string.text_menu_payment));
        applicationPages.setData("");
        listMenu.add(applicationPages);

        applicationPages = new ApplicationPages();
        applicationPages.setId(-3);
        applicationPages.setTitle(activity
                .getString(R.string.text_menu_history));
        applicationPages.setData("");
        listMenu.add(applicationPages);

        applicationPages = new ApplicationPages();
        applicationPages.setId(-4);
        applicationPages.setTitle(activity
                .getString(R.string.text_menu_referral));
        applicationPages.setData("");
        listMenu.add(applicationPages);

        if (TextUtils.isEmpty(response)) {
            return listMenu;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                JSONArray jsonArray = jsonObject
                        .getJSONArray(Const.Params.INFORMATIONS);
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        applicationPages = new ApplicationPages();
                        JSONObject object = jsonArray.getJSONObject(i);
                        applicationPages.setId(object.getInt(Const.Params.ID));
                        applicationPages.setTitle(object
                                .getString(Const.Params.TITLE));
                        applicationPages.setData(object
                                .getString(Const.Params.CONTENT));
                        applicationPages.setIcon(object
                                .getString(Const.Params.ICON));
                        listMenu.add(applicationPages);
                    }
                }
            }
            // else {
            // AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
            // }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listMenu;
    }

    public ArrayList<Card> parseCards(String response, ArrayList<Card> listCards) {

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(PAYMENTS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    Card card = new Card();
                    JSONObject cardJson = jsonArray.getJSONObject(i);
                    // card.setStripeToken(cardJson.getString(STRIPE_TOKEN));
                    card.setLastFour(cardJson.getString(LAST_FOUR));
                    card.setStripeToken(cardJson.getString(CUSTOMER_ID));
                    card.setId(cardJson.getInt(ID));
                    // card.setCreatedAt(cardJson.getString(CREATED_AT));
                    // card.setUpdatedAt(cardJson.getString(UPDATED_AT));
                    card.setOwnerId(cardJson.getString(OWNER_ID));
                    card.setCardType(cardJson.getString(CARD_TYPE));
                    if (cardJson.getInt(IS_DEFAULT) == 1)
                        card.setDefault(true);
                    else
                        card.setDefault(false);
                    listCards.add(card);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listCards;
    }

    public ArrayList<History> parseHistory(String response,
                                           ArrayList<History> list) {
        list.clear();

        if (TextUtils.isEmpty(response)) {
            return list;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getBoolean(KEY_SUCCESS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(REQUESTS);
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);
                        History history = new History();
                        history.setId(object.getInt(ID));
                        history.setDate(object.getString(DATE));

                        double distance = Double.parseDouble(object
                                .getString(DISTANCE));
                        // bill.setDistance(jsonObject.getString(DISTANCE));
                        history.setUnit(object.getString(UNIT));
                        if (history.getUnit().equalsIgnoreCase(KMS)) {
                            distance = distance * 0.62137;
                        }
                        history.setDistance(new DecimalFormat("0.00")
                                .format(distance));
                        history.setUnit(object.getString(UNIT));
                        history.setTime(object.getString(TIME));
                        history.setDistanceCost(object.getString(DISTANCE_COST));
                        history.setTimecost(object.getString(TIME_COST));
                        history.setBasePrice(object.getString(BASE_PRICE));
                        history.setTotal(new DecimalFormat("0.00")
                                .format(Double.parseDouble(object
                                        .getString(TOTAL))));
                        history.setCurrency(object.getString(CURRENCY));
                        history.setType(object.getString(TYPE));
                        history.setPricePerDistance(object
                                .getString(PRICE_PER_UNIT_DISTANCE));
                        history.setPricePerTime(object
                                .getString(PRICE_PER_UNIT_TIME));

                        // history.setPromoBonus(object.getString(PROMO_BONUS));
                        // history.setReferralBonus(object
                        // .getString(REFERRAL_BONUS));
                        JSONObject userObject = object.getJSONObject(WALKER);
                        history.setFirstName(userObject
                                .getString(Const.Params.FIRSTNAME));
                        history.setLastName(userObject
                                .getString(Const.Params.LAST_NAME));
                        history.setPhone(userObject
                                .getString(Const.Params.PHONE));
                        history.setPicture(userObject
                                .getString(Const.Params.PICTURE));
                        history.setEmail(userObject
                                .getString(Const.Params.EMAIL));
                        history.setBio(userObject.getString(Const.Params.BIO));
                        history.setMapImage(object
                                .getString(Const.Params.HISTORY_MAP));
                        // history.setVehicle(object
                        // .getString(Const.Params.HISTORY_VEHICLE));
                        history.setSourceLat(object
                                .getString(Const.Params.SOURCE_LAT));
                        history.setSourceLng(object
                                .getString(Const.Params.SOURCE_LNG));
                        history.setDestLat(object
                                .getString(Const.Params.DESTI_LAT));
                        history.setDestLng(object
                                .getString(Const.Params.DESTI_LNG));
                        history.setSrcAdd(object
                                .getString(Const.Params.SRC_ADD));
                        history.setDestAdd(object
                                .getString(Const.Params.DEST_ADD));
                        list.add(history);
                    }
                }

            }
            // else {
            // AndyUtils.showToast(jsonObject.getString(KEY_ERROR), activity);
            // }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Driver> parseNearestDrivers(String response) {
        ArrayList<Driver> listDriver = new ArrayList<Driver>();
        if (TextUtils.isEmpty(response))
            return listDriver;
        try {

            JSONArray jsonArray = new JSONObject(response)
                    .getJSONArray(WALKERS);
            for (int i = 0; i < jsonArray.length(); i++) {
                Driver driver = new Driver();
                driver.setDriverId(jsonArray.getJSONObject(i).getInt(
                        Const.Params.ID));
                driver.setLatitude(jsonArray.getJSONObject(i).getDouble(
                        Const.Params.LATITUDE));
                driver.setLongitude(jsonArray.getJSONObject(i).getDouble(
                        Const.Params.LONGITUDE));
                driver.setBearing(jsonArray.getJSONObject(i).getDouble(
                        Const.Params.BEARING));
                driver.setVehicleTypeId(jsonArray.getJSONObject(i).getInt(
                        "type"));
                listDriver.add(driver);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listDriver;
    }

    public int parseNearestDriverDuration(String response) {
        if (TextUtils.isEmpty(response))
            return 0;
        try {
            JSONArray jsonArray = new JSONObject(response).getJSONArray(ROUTES);
            JSONArray jArrSub = jsonArray.getJSONObject(0).getJSONArray(LEGS);
            long totalSeconds = jArrSub.getJSONObject(0)
                    .getJSONObject(DURATION).getLong(VALUE);
            int totalMinutes = Math.round(totalSeconds / 60);
            return totalMinutes;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // public String parseNearestDriverDurationString(String response) {
    // if (TextUtils.isEmpty(response))
    // return "N/A";
    // try {
    // JSONArray jsonArray = new JSONObject(response)
    // .getJSONArray(ROUTES);
    // JSONArray jArrSub = jsonArray.getJSONObject(0).getJSONArray(LEGS);
    //
    // return jArrSub.getJSONObject(0).getJSONObject(DURATION)
    // .getString(TEXT);
    // } catch (JSONException e) {
    // System.out.println("***********************************");
    // e.printStackTrace();
    // AppLog.Log("JSONException===============", e+" ");
    // // AndyUtils.showToast("Exception occured.", activity);
    // return " ";
    // }
    // }

    public String parseNearestDriverDurationString(String response) {
        if (TextUtils.isEmpty(response))
            return "";
        try {
            JSONObject jObj = new JSONObject(response);
            JSONArray jsonArray = jObj.getJSONArray(ROUTES);
            JSONObject jSubobj = jsonArray.getJSONObject(0);
            JSONArray jArray = jSubobj.getJSONArray(LEGS);

            String duration = jArray.getJSONObject(0).getJSONObject(DURATION)
                    .getString(TEXT);
            return duration;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void parseCardAndPriceDetails(String response) {
        try {
            AppLog.Log("ParseContent", "parseCardAndPriceDetails");
            JSONObject job = new JSONObject(response);
            JSONObject cardObject = job.getJSONObject(CARD_DETAILS);
            preferenceHelper.putDefaultCard(cardObject.getInt(CARD_ID));
            preferenceHelper.putDefaultCardNo(cardObject.getString(LAST_FOUR));
            preferenceHelper
                    .putDefaultCardType(cardObject.getString(CARD_TYPE));

            // JSONObject chargeObject = job.getJSONObject(CHARGE_DETAILS);
            // preferenceHelper.putBasePrice(Float.parseFloat(chargeObject
            // .getString("base_price")));
            // preferenceHelper.putDistancePrice(Float.parseFloat(chargeObject
            // .getString(DISTANCE_PRICE)));
            // preferenceHelper.putTimePrice(Float.parseFloat(chargeObject
            // .getString(PRICE_PER_UNIT_TIME)));
            // if (job.has(OWNER)) {
            //
            // JSONObject walkerObject = job.getJSONObject(OWNER);
            // AppLog.Log("ParseContent",
            // "Payment type : " + walkerObject.getInt(PAYMENT_TYPE));
            // preferenceHelper.putPaymentMode(walkerObject
            // .getInt(PAYMENT_TYPE));
            // }
        } catch (JSONException e) {
            AppLog.Log("MainDrawerActivity", "" + e);
        }
    }

    public ArrayList<String> parseNearByPlaces(String response,
                                               ArrayList<String> resultList) {
        try {
            JSONObject resultObject;
            JSONObject job = new JSONObject(response);
            JSONArray resultArr = job.getJSONArray(RESULTS);
            for (int i = 0; i < resultArr.length(); i++) {
                resultObject = resultArr.getJSONObject(i);
                // String fullVicinity = resultObject.getString("vicinity");
                // String[] vicinityArr = resultObject.getString("vicinity")
                // .split(", ");
                // String vicinity = null;
                // if (vicinityArr.length > 2) {
                // vicinity = vicinityArr[vicinityArr.length - 2];
                // vicinity += ", " + vicinityArr[vicinityArr.length - 1];
                // } else {
                // vicinity = fullVicinity;
                // }
                resultList.add(resultObject.getString(NAME));
            }
        } catch (JSONException e) {
            AppLog.Log("MainDrawerActivity", "" + e);
        }
        return resultList;
    }
}
