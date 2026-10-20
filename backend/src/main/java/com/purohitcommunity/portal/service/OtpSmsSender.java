package com.purohitcommunity.portal.service;

public interface OtpSmsSender {
    default void requireAvailable() {}
    void send(String registeredPhone, String otp);
}
