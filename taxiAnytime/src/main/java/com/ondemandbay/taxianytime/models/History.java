/**
 *
 */
package com.ondemandbay.taxianytime.models;

import java.io.Serializable;

/**
 * @author Elluminati elluminati.in
 */
@SuppressWarnings("serial")
public class History implements Serializable {

    private int id;
    private String date, distance, time, basePrice, distanceCost, timecost,
            total, currency, firstName, lastName, phone, email, picture, bio,
            mapImage, vehicle, unit, type, sourceLat, sourceLng, destLat,
            destLng, srcAdd = "", destAdd = "", pricePerDistance, pricePerTime;

    // private LatLng sourceLatLng, destLatLng;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the diatance
     */
    public String getDistance() {
        return distance;
    }

    /**
     * @param diatance the diatance to set
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return the basePrice
     */
    public String getBasePrice() {
        return basePrice;
    }

    /**
     * @param basePrice the basePrice to set
     */
    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    /**
     * @return the distanceCost
     */
    public String getDistanceCost() {
        return distanceCost;
    }

    /**
     * @param distanceCost the distanceCost to set
     */
    public void setDistanceCost(String distanceCost) {
        this.distanceCost = distanceCost;
    }

    /**
     * @return the timecost
     */
    public String getTimecost() {
        return timecost;
    }

    /**
     * @param timecost the timecost to set
     */
    public void setTimecost(String timecost) {
        this.timecost = timecost;
    }

    /**
     * @return the total
     */
    public String getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * @return the firstName
     */

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     * @param picture the picture to set
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * @return the bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * @param bio the bio to set
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(String sourceLat) {
        this.sourceLat = sourceLat;
    }

    public String getSourceLng() {
        return sourceLng;
    }

    public void setSourceLng(String sourceLng) {
        this.sourceLng = sourceLng;
    }

    public String getDestLat() {
        return destLat;
    }

    public void setDestLat(String destLat) {
        this.destLat = destLat;
    }

    public String getDestLng() {
        return destLng;
    }

    public void setDestLng(String destLng) {
        this.destLng = destLng;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(String mapImage) {
        this.mapImage = mapImage;
    }

    public String getSrcAdd() {
        return srcAdd;
    }

    public void setSrcAdd(String srcAdd) {
        this.srcAdd = srcAdd;
    }

    public String getDestAdd() {
        return destAdd;
    }

    public void setDestAdd(String destAdd) {
        this.destAdd = destAdd;
    }

    public String getPricePerDistance() {
        return pricePerDistance;
    }

    public void setPricePerDistance(String pricePerDistance) {
        this.pricePerDistance = pricePerDistance;
    }

    public String getPricePerTime() {
        return pricePerTime;
    }

    public void setPricePerTime(String pricePerTime) {
        this.pricePerTime = pricePerTime;
    }
}
