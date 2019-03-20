package com.sparklit.adbutler;


import android.location.Location;
import android.os.Bundle;

import java.util.Date;
import java.util.Set;

/**
 * An AdButler AdMob SDK ad request used to load an ad.
 */
public class AdRequest {

    // Mediation data
    private Boolean isTestMode;
    private Date birthday;
    private int gender;
    private Location location;
    private int age = 0;
    private int yearOfBirth = 0;
    private int coppa = 0;
    private Bundle customExtras;
    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    private int accountID;
    private int zoneID;

    /**
     * Creates a new {@link AdRequest}.
     */
    public AdRequest(int accountID, int zoneID) {
        this.accountID = accountID;
        this.zoneID = zoneID;
    }

    /* SETTERS */

    /**
     * Sets keywords for targeting purposes.
     *
     * @param keywords A set of keywords.
     */
    public void setKeywords(Set<String> keywords) {
        // Normally we'd save the keywords. But since this is a sample network, we'll do nothing.
    }

    /**
     * Designates a request for test mode.
     *
     * @param useTesting {@code true} to enable test mode.
     */
    public void setTestMode(boolean useTesting) {
        this.isTestMode = useTesting;
    }

    public Boolean getTestMode() {
        return isTestMode;
    }

    /**
     * Sets the mediation location data.
     *
     * @param location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public int getCoppa() {
        return coppa;
    }

    public void setCoppa(int coppa) {
        this.coppa = coppa;
    }

    public Bundle getCustomExtras() {
        return customExtras;
    }

    public void setCustomExtras(Bundle customExtras) {
        this.customExtras = customExtras;
    }

    public int getAccountID(){return this.accountID;}

    public int getZoneID(){return this.zoneID;}
}
