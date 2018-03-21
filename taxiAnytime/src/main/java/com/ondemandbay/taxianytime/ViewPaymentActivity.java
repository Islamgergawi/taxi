/**
 *
 */
package com.ondemandbay.taxianytime;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ondemandbay.taxianytime.adapter.PaymentListAdapter;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.util.StripeTextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class ViewPaymentActivity extends ActionBarBaseActivitiy {

    // private LinearLayout llPaymentList;
    public static final String[] PREFIXES_AMERICAN_EXPRESS = {"34", "37"};
    public static final String[] PREFIXES_DISCOVER = {"60", "62", "64", "65"};
    public static final String[] PREFIXES_JCB = {"35"};
    public static final String[] PREFIXES_DINERS_CLUB = {"300", "301", "302",
            "303", "304", "305", "309", "36", "38", "37", "39"};
    public static final String[] PREFIXES_VISA = {"4"};
    public static final String[] PREFIXES_MASTERCARD = {"50", "51", "52",
            "53", "54", "55"};
    public static final String AMERICAN_EXPRESS = "American Express";
    public static final String DISCOVER = "Discover";
    public static final String JCB = "JCB";
    public static final String DINERS_CLUB = "Diners Club";
    public static final String VISA = "Visa";
    public static final String MASTERCARD = "MasterCard";
    public static final String UNKNOWN = "Unknown";
    public static final int MAX_LENGTH_STANDARD = 16;
    public static final int MAX_LENGTH_AMERICAN_EXPRESS = 15;
    public static final int MAX_LENGTH_DINERS_CLUB = 14;
    static final Pattern CODE_PATTERN = Pattern
            .compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");
    // private final int MY_SCAN_REQUEST_CODE = 111;
    int paymentMode;
    Dialog addCardDialog;
    EditText etCreditCardNum, etCvc, etYear, etMonth;
    private ListView listViewPayment;
    private PaymentListAdapter adapter;
    private ArrayList<com.ondemandbay.taxianytime.models.Card> listCards;
    // private int REQUEST_ADD_CARD = 1;
    private ImageView tvNoHistory;
    // private ImageView ivCredit, ivCash;
    private TextView tvHeaderText, tvAddPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_view_payment);

        setTitle(getString(R.string.text_cards));
        btnNotification.setVisibility(View.GONE);
        setIconMenu(R.drawable.back);
        listViewPayment = (ListView) findViewById(R.id.listViewPayment);
        // llPaymentList = (LinearLayout) findViewById(R.id.llPaymentList);
        tvNoHistory = (ImageView) findViewById(R.id.ivEmptyPayment);
        tvHeaderText = (TextView) findViewById(R.id.tvHeaderText);
        tvAddPayment = (TextView) findViewById(R.id.tvAddPayment);

        paymentMode = (int) new PreferenceHelper(this).getPaymentMode();
        listCards = new ArrayList<com.ondemandbay.taxianytime.models.Card>();
        adapter = new PaymentListAdapter(this, listCards, new PreferenceHelper(
                this).getDefaultCard());
        listViewPayment.setAdapter(adapter);
        tvAddPayment.setOnClickListener(this);
        getCards();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionMenu:
                onBackPressed();
                break;
            case R.id.tvAddPayment:
                showAddCardDialog();
                break;
            case R.id.btnAddPayment:
                if (isValidate()) {
                    saveCreditCard();
                }
                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.ActionBarBaseActivitiy#isValidate()
     */
    @Override
    protected boolean isValidate() {
        if (etCreditCardNum.getText().length() == 0
                || etCvc.getText().length() == 0
                || etMonth.getText().length() == 0
                || etYear.getText().length() == 0) {
            AndyUtils.showToast("Enter Proper data", this);
            return false;
        }
        return true;
    }

    private void getCards() {
        AndyUtils.showCustomProgressDialog(this,
                getString(R.string.progress_loading), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                Const.ServiceType.GET_CARDS + Const.Params.ID + "="
                        + new PreferenceHelper(this).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(this).getSessionToken());
        new HttpRequester(this, map, Const.ServiceCode.GET_CARDS, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_CARDS, this, this));
    }

    // private void scan() {
    // Intent scanIntent = new Intent(this, CardIOActivity.class);
    //
    // // required for authentication with card.io
    // // scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN,
    // // Const.MY_CARDIO_APP_TOKEN);
    //
    // // customize these values to suit your needs.
    // scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); //
    // default:
    // // true
    // scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default:
    // // false
    // scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); //
    // default:
    // // false
    //
    // // hides the manual entry button
    // // if set, developers should provide their own manual entry
    // // mechanism in
    // // the app
    // scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
    // // default:
    // // false
    //
    // // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this
    // // activity.
    // startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
    // }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.ActionBarBaseActivitiy#onTaskCompleted(java.lang.String,
     * int)
     */
    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        switch (serviceCode) {
            case Const.ServiceCode.GET_CARDS:
                if (new ParseContent(this).isSuccess(response)) {
                    listCards.clear();
                    new ParseContent(this).parseCards(response, listCards);
                    AppLog.Log("UberViewPayment", "listCards : " + listCards.size());
                    if (listCards.size() > 0) {
                        listViewPayment.setVisibility(View.VISIBLE);
                        tvNoHistory.setVisibility(View.GONE);
                        paymentMode = 1;
                        tvHeaderText.setVisibility(View.VISIBLE);
                    } else {
                        listViewPayment.setVisibility(View.GONE);
                        tvNoHistory.setVisibility(View.VISIBLE);
                        tvHeaderText.setVisibility(View.GONE);
                        paymentMode = 0;
                    }
                    adapter.notifyDataSetChanged();
                }
                break;

            case Const.ServiceCode.ADD_CARD:

                if (new ParseContent(this).isSuccess(response)) {
                    AndyUtils.showToast(getString(R.string.text_add_card_scucess),
                            this);
                    setResult(Activity.RESULT_OK);
                } else {
                    AndyUtils.showToast(
                            getString(R.string.text_not_add_card_unscucess), this);
                    setResult(Activity.RESULT_CANCELED);
                }
                addCardDialog.dismiss();
                getCards();
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.uberorg.ActionBarBaseActivitiy#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                getCards();
                break;

            // case MY_SCAN_REQUEST_CODE:
            // if (resultCode == Activity.RESULT_OK) {
            // if (data != null
            // && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            // final CreditCard scanResult = data
            // .getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            //
            // Card card = new Card(scanResult.getRedactedCardNumber(),
            // scanResult.expiryMonth, scanResult.expiryYear,
            // scanResult.cvv);
            //
            // boolean validation = card.validateCard();
            // if (validation) {
            // AndyUtils
            // .showCustomProgressDialog(this,
            // getString(R.string.adding_payment),
            // false, null);
            // new Stripe().createToken(card, Const.PUBLISHABLE_KEY,
            // new TokenCallback() {
            // public void onSuccess(Token token) {
            // // getTokenList().addToList(token);
            // // AndyUtils.showToast(token.getId(),
            // // activity);
            // String lastFour = scanResult
            // .getRedactedCardNumber();
            // lastFour = lastFour.substring(lastFour
            // .length() - 4);
            // addCard(token.getId(), lastFour);
            // // finishProgress();
            // }
            //
            // public void onError(Exception error) {
            // AndyUtils.showToast("Error",
            // UberViewPaymentActivity.this);
            // // finishProgress();
            // AndyUtils.removeCustomProgressDialog();
            // }
            // });
            // } else if (!card.validateNumber()) {
            // // handleError("The card number that you entered is invalid");
            // AndyUtils.showToast(
            // "The card number that you entered is invalid",
            // this);
            // } else if (!card.validateExpiryDate()) {
            // // handleError("");
            // AndyUtils
            // .showToast(
            // "The expiration date that you entered is invalid",
            // this);
            // } else if (!card.validateCVC()) {
            // // handleError("");
            // AndyUtils.showToast(
            // "The CVC code that you entered is invalid",
            // this);
            //
            // } else {
            // // handleError("");
            // AndyUtils
            // .showToast(
            // "The card details that you entered are invalid",
            // this);
            // }
            //
            // } else {
            // // resultStr = "Scan was canceled.";
            // AndyUtils.showToast("Scan was canceled.", this);
            // }
            // } else {
            // AndyUtils.showToast("Scan was uncessfull.", this);
            // }
            // break;
        }
    }

    private void addCard(String stripeToken, String lastFour) {
        // AppLog.Log(TAG, "Final token : " + peachToken.substring(3));
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.ADD_CARD);
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN,
                new PreferenceHelper(this).getSessionToken());
        map.put(Const.Params.STRIPE_TOKEN, stripeToken);
        map.put(Const.Params.LAST_FOUR, lastFour);
        // map.put(Const.Params.CARD_TYPE, type);
        new HttpRequester(this, map, Const.ServiceCode.ADD_CARD, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.ADD_CARD, this, this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void showAddCardDialog() {
        ImageView btnAddPayment;

        addCardDialog = new Dialog(this);
        addCardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addCardDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        addCardDialog.setContentView(R.layout.fragment_payment);

        btnAddPayment = (ImageView) addCardDialog
                .findViewById(R.id.btnAddPayment);
        etCreditCardNum = (EditText) addCardDialog
                .findViewById(R.id.edtRegisterCreditCardNumber);
        etCvc = (EditText) addCardDialog.findViewById(R.id.edtRegistercvc);
        etYear = (EditText) addCardDialog.findViewById(R.id.edtRegisterexpYear);
        etMonth = (EditText) addCardDialog
                .findViewById(R.id.edtRegisterexpMonth);

        etCreditCardNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (StripeTextUtils.isBlank(s.toString())) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null);
                }
                String type = getType(s.toString());

                if (type.equals(VISA)) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(
                                    R.drawable.ub__creditcard_visa), null,
                            null, null);

                } else if (type.equals(MASTERCARD)) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(
                                    R.drawable.ub__creditcard_mastercard),
                            null, null, null);

                } else if (type.equals(AMERICAN_EXPRESS)) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(
                                    R.drawable.ub__creditcard_amex), null,
                            null, null);

                } else if (type.equals(DISCOVER)) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(
                                    R.drawable.ub__creditcard_discover), null,
                            null, null);

                } else if (type.equals(DINERS_CLUB)) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(
                                    R.drawable.ub__creditcard_discover), null,
                            null, null);

                } else {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null);
                }
                if (etCreditCardNum.getText().toString().length() == 19) {
                    etCvc.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !CODE_PATTERN.matcher(s).matches()) {
                    String input = s.toString();
                    String numbersOnly = keepNumbersOnly(input);
                    String code = formatNumbersAsCode(numbersOnly);
                    etCreditCardNum.removeTextChangedListener(this);
                    etCreditCardNum.setText(code);
                    etCreditCardNum.setSelection(code.length());
                    etCreditCardNum.addTextChangedListener(this);
                }
            }

            private String keepNumbersOnly(CharSequence s) {
                return s.toString().replaceAll("[^0-9]", ""); // Should of
                // course be
                // more robust
            }

            private String formatNumbersAsCode(CharSequence s) {
                int groupDigits = 0;
                String tmp = "";
                for (int i = 0; i < s.length(); ++i) {
                    tmp += s.charAt(i);
                    ++groupDigits;
                    if (groupDigits == 4) {
                        tmp += "-";
                        groupDigits = 0;
                    }
                }
                return tmp;
            }
        });

        etCvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (etCvc.getText().toString().length() == 3) {
                    etMonth.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etMonth.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (etMonth.getText().toString().length() == 2) {
                    etYear.requestFocus();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btnAddPayment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isValidate()) {
                    saveCreditCard();
                }
            }
        });

        addCardDialog.show();

    }

    public String getType(String number) {
        if (!StripeTextUtils.isBlank(number)) {
            if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_AMERICAN_EXPRESS)) {
                return AMERICAN_EXPRESS;
            } else if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_DISCOVER)) {
                return DISCOVER;
            } else if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_JCB)) {
                return JCB;
            } else if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_DINERS_CLUB)) {
                return DINERS_CLUB;
            } else if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_VISA)) {
                return VISA;
            } else if (StripeTextUtils.hasAnyPrefix(number, PREFIXES_MASTERCARD)) {
                return MASTERCARD;
            } else {
                return UNKNOWN;
            }
        }
        return UNKNOWN;

    }

    public void saveCreditCard() {
        Card card = new Card(etCreditCardNum.getText().toString(),
                Integer.parseInt(etMonth.getText().toString()),
                Integer.parseInt(etYear.getText().toString()), etCvc.getText()
                .toString());

        boolean validation = card.validateCard();
        if (validation) {
            AndyUtils.showCustomProgressDialog(this,
                    getString(R.string.adding_payment), false, null);
            new Stripe().createToken(card, Const.PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // getTokenList().addToList(token);
                            // AndyUtils.showToast(token.getId(), activity);
                            String lastFour = etCreditCardNum.getText()
                                    .toString().toString();
                            lastFour = lastFour.substring(lastFour.length() - 4);
                            addCard(token.getId(), lastFour);
                            // finishProgress();
                        }

                        public void onError(Exception error) {
                            AndyUtils.showToast("Error",
                                    ViewPaymentActivity.this);
                            // finishProgress();
                            AndyUtils.removeCustomProgressDialog();
                        }
                    });
        } else if (!card.validateNumber()) {
            // handleError("The card number that you entered is invalid");
            AndyUtils.showToast(getString(R.string.error_invalid_card_number),
                    this);
        } else if (!card.validateExpiryDate()) {
            // handleError("");
            AndyUtils.showToast(
                    getString(R.string.error_expiration_date_invalid), this);
        } else if (!card.validateCVC()) {
            // handleError("");
            AndyUtils.showToast(getString(R.string.error_cvc_invalid),
                    this);

        } else {
            // handleError("");
            AndyUtils.showToast(
                    getString(R.string.error_card_detail_invalid), this);
        }
    }

}
