package com.example.appshop.model;

public class Shop {
    private String uid, email, name, shopName, deliveryFee
            , phone, address, password, timestamp
            , shopOpen, online, userType, profileImage;

    public Shop() {
    }

    public Shop(String uid, String email, String name, String shopName
            , String deliveryFee, String phone, String address
            , String password, String timestamp, String shopOpen
            , String online, String userType, String profileImage) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.shopName = shopName;
        this.deliveryFee = deliveryFee;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.timestamp = timestamp;
        this.shopOpen = shopOpen;
        this.online = online;
        this.userType = userType;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(String shopOpen) {
        this.shopOpen = shopOpen;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
