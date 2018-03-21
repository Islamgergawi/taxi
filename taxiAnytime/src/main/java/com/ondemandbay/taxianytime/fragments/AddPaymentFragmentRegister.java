package com.ondemandbay.taxianytime.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ondemandbay.taxianytime.MainDrawerActivity;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.PaymentListAdapter;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
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

public class AddPaymentFragmentRegister extends BaseFragmentRegister
{

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

    static final Pattern CODE_PATTERN = Pattern
            .compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");
    Dialog addCardDialog;
    EditText etCreditCardNum, etCvc, etYear, etMonth;
    private ListView listViewPayment;
    private PaymentListAdapter adapter;
    private ArrayList<com.ondemandbay.taxianytime.models.Card> listCards;
    private ImageView tvNoHistory;
    private TextView tvHeaderText, tvSkipCard;
    private TextView tvAddPayment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_view_payment, container,
                false);

        activity.setTitle(getString(R.string.text_cards));
        activity.setIconMenu(R.drawable.car);
        activity.btnNotification.setVisibility(View.GONE);
        listViewPayment = (ListView) view.findViewById(R.id.listViewPayment);
        // llPaymentList = (LinearLayout) findViewById(R.id.llPaymentList);
        tvNoHistory = (ImageView) view.findViewById(R.id.ivEmptyPayment);
        tvHeaderText = (TextView) view.findViewById(R.id.tvHeaderText);
        tvAddPayment = (TextView) view
                .findViewById(R.id.tvAddPayment);
        tvAddPayment.setOnClickListener(this);
        listCards = new ArrayList<com.ondemandbay.taxianytime.models.Card>();
        adapter = new PaymentListAdapter(activity, listCards,
                new PreferenceHelper(activity).getDefaultCard());
        listViewPayment.setAdapter(adapter);
        tvHeaderText.setVisibility(View.INVISIBLE);
        tvNoHistory.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // token = getArguments().getString(Const.Params.TOKEN);
        // id = getArguments().getString(Const.Params.ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddPayment:
                showAddCardDialog();
                // startActivityForResult(new Intent(this,
                // UberAddPaymentActivity.class), REQUEST_ADD_CARD);
                break;
            // case R.id.btnScan:
            // scan();
            // break;
            case R.id.btnAddPayment:
                if (isValidate()) {
                    saveCreditCard();
                }
                break;

            case R.id.tvSkipCard:
                tvSkipCard.setVisibility(View.GONE);
                addCardDialog.dismiss();
                startActivity(new Intent(activity, MainDrawerActivity.class));
                activity.finish();

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
        if (etCreditCardNum.getText().length() == 0
                || etCvc.getText().length() == 0
                || etMonth.getText().length() == 0
                || etYear.getText().length() == 0) {
            AndyUtils.showToast("Enter Proper data", activity);
            return false;
        }
        return true;
    }


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
            case Const.ServiceCode.ADD_CARD:
                if (new ParseContent(activity).isSuccess(response)) {
                    AndyUtils.showToast(getString(R.string.text_add_card_scucess),
                            activity);
                    activity.setResult(Activity.RESULT_OK);
                } else {
                    AndyUtils.showToast(
                            getString(R.string.text_not_add_card_unscucess),
                            activity);
                    activity.setResult(Activity.RESULT_CANCELED);
                }
                addCardDialog.dismiss();

                startActivity(new Intent(activity, MainDrawerActivity.class));
                activity.finish();

                break;
        }
    }


    private void addCard(String stripeToken, String lastFour) {
        // AppLog.Log(TAG, "Final token : " + peachToken.substring(3));
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.ADD_CARD);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN,
                new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.STRIPE_TOKEN, stripeToken);
        map.put(Const.Params.LAST_FOUR, lastFour);
        // map.put(Const.Params.CARD_TYPE, type);
        new HttpRequester(activity, map, Const.ServiceCode.ADD_CARD, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.ADD_CARD, this, this));
    }

    public void showAddCardDialog() {
        ImageView btnAddPayment;

        addCardDialog = new Dialog(activity);
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
        tvSkipCard = (TextView) addCardDialog.findViewById(R.id.tvSkipCard);
        tvSkipCard.setVisibility(View.VISIBLE);
        tvSkipCard.setOnClickListener(this);

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
            AndyUtils.showCustomProgressDialog(activity,
                    getString(R.string.adding_payment), false, null);
            new Stripe().createToken(card, Const.PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // getTokenList().addToList(token);
                            // AndyUtils.showToast(token.getId(), activity);
                            String lastFour = etCreditCardNum.getText()
                                    .toString();
                            lastFour = lastFour.substring(lastFour.length() - 4);
                            addCard(token.getId(), lastFour);
                            // finishProgress();
                        }

                        public void onError(Exception error) {
                            AndyUtils.showToast(getString(R.string.text_error), activity);
                            // finishProgress();
                            AndyUtils.removeCustomProgressDialog();
                        }
                    });
        } else if (!card.validateNumber()) {
            // handleError("The card number that you entered is invalid");
            AndyUtils.showToast(getString(R.string.error_invalid_card_number),
                    activity);
        } else if (!card.validateExpiryDate()) {
            // handleError("");
            AndyUtils
                    .showToast(
                            getString(R.string.error_expiration_date_invalid),
                            activity);
        } else if (!card.validateCVC()) {
            // handleError("");
            AndyUtils.showToast(getString(R.string.error_cvc_invalid),
                    activity);

        } else {
            // handleError("");
            AndyUtils.showToast(
                    getString(R.string.error_card_detail_invalid), activity);
        }
    }

}