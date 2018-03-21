package com.ondemandbay.taxianytime.models;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Booking implements Serializable {

    int id, requestId;
    String startTime, source_add, dest_add, mapImage, vehicleType, driverStatus, ongoingTripTime;
    //public static boolean isCurrentRequest = false;
    //public static boolean isFutureRequest = false;

    private boolean isFutureRequest;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source_add;
    }

    public void setSource(String source_add) {
        this.source_add = source_add;
    }

   /* public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }*/

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getDest() {
        return dest_add;
    }

    public void setDest(String dest_add) {
        this.dest_add = dest_add;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(String mapImage) {
        this.mapImage = mapImage;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public String getRequestCreatedTime() {
        return ongoingTripTime;
    }

    public void setRequestCreatedTime(String ongoingTripTime) {
        this.ongoingTripTime = ongoingTripTime;
    }

    public boolean isFutureRequest() {
        return isFutureRequest;
    }

    public void setIsFutureRequest(boolean isFutureRequest) {
        this.isFutureRequest = isFutureRequest;
    }
}
