package com.ondemandbay.taxianytime;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
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
import java.util.HashMap;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

/**
 * @author Elluminati elluminati.in
 */
public class AddPaymentActivity extends ActionBarBaseActivitiy
// implements PWTokenObtainedListener, PWTransactionListener
{
    // private String patternVisa = "^4[0-9]{12}(?:[0-9]{3})?$";
    // private String patternMasterCard = "^5[1-5][0-9]{14}$";
    // private String patternAmericanExpress = "^3[47][0-9]{13}$";
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
    // private ImageView btnScan;
    private final int MY_SCAN_REQUEST_CODE = 111;
    private ImageView btnAddPayment;
    private EditText etCreditCardNum, etCvc, etYear, etMonth;
    private String type;

    private PreferenceHelper preference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.fragment_payment);
        // startService(new Intent(this,

        setTitle(getString(R.string.text_add_payment));

        preference = new PreferenceHelper(this);
        btnAddPayment = (ImageView) findViewById(R.id.btnAddPayment);

        etCreditCardNum = (EditText) findViewById(R.id.edtRegisterCreditCardNumber);
        etCreditCardNum.addTextChangedListener(new TextWatcher() {
            //
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (StripeTextUtils.isBlank(s.toString())) {
                    etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null);
                }
                type = getType(s.toString());

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
                    etMonth.requestFocus();
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
        etCvc = (EditText) findViewById(R.id.edtRegistercvc);
        etYear = (EditText) findViewById(R.id.edtRegisterexpYear);
        etMonth = (EditText) findViewById(R.id.edtRegisterexpMonth);
        etYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (etYear.getText().toString().length() == 4) {
                    etCvc.requestFocus();
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
        // etHolder = (EditText) findViewById(R.id.edtRegisterCreditCardHolder);
        // btnScan.setOnClickListener(this);
        btnAddPayment.setOnClickListener(this);
        // findViewById(R.id.btnPaymentSkip).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnAddPayment:
                if (isValidate()) {
                    saveCreditCard();
                }
                break;
            // case R.id.btnScan:
            // scan();
            // break;
            case R.id.btnActionNotification:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        if (etCreditCardNum.getText().length() == 0
                || etCvc.getText().length() == 0
                || etMonth.getText().length() == 0
                || etYear.getText().length() == 0) {
            AndyUtils.showToast(getString(R.string.text_enter_proper_data), this);
            return false;
        }
        return true;
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
                            String lastFour = etCreditCardNum.getText()
                                    .toString().toString();
                            lastFour = lastFour.substring(lastFour.length() - 4);
                            addCard(token.getId(), lastFour);
                        }

                        public void onError(Exception error) {
                            AndyUtils.showToast(getString(R.string.text_error),
                                    AddPaymentActivity.this);
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
    public void onTaskCompleted(String response, int serviceCode) {
        AppLog.Log(Const.TAG, response);
        AndyUtils.removeCustomProgressDialog();
        super.onTaskCompleted(response, serviceCode);
        switch (serviceCode) {
            case Const.ServiceCode.ADD_CARD:

                if (new ParseContent(this).isSuccess(response)) {
                    AndyUtils.showToast(getString(R.string.text_add_card_scucess),
                            this);
                    setResult(Activity.RESULT_OK);
                    preference.putPaymentMode(Const.CREDIT);
                } else {
                    AndyUtils.showToast(
                            getString(R.string.text_not_add_card_unscucess), this);
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
