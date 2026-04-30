package com.retail.copilot.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(String storeCode) {
        super("Store with code " + storeCode + " not found");
    }
}
