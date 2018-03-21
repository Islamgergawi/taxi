/**
 *
 */
package com.ondemandbay.taxianytime.models;

/**
 * @author Elluminati elluminati.in
 */
public class Reffral {

    private String referralCode;
    private String totalReferrals;
    private String amountEarned;
    private String amountSpent;
    private String balanceAmount;
    private String referralBonus;

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getTotalReferrals() {
        return totalReferrals;
    }

    public void setTotalReferrals(String totalReferrals) {
        this.totalReferrals = totalReferrals;
    }

    public String getAmountEarned() {
        return amountEarned;
    }

    public void setAmountEarned(String amountEarned) {
        this.amountEarned = amountEarned;
    }

    public String getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(String amountSpent) {
        this.amountSpent = amountSpent;
    }

    public String getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getReferralBonus() {
        return referralBonus;
    }

    public void setReferralBonus(String referralBonus) {
        this.referralBonus = referralBonus;
    }
}
