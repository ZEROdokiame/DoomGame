package org.nico.ratel.landlords.enums;

public enum ClientType {

    LANDLORD("赖盖特之赐"),

    PEASANT("地狱幸存者");

    private final String zhName;

    ClientType(String zhName){
        this.zhName = zhName;
    }

    public String zh(){
        return zhName;
    }

}
