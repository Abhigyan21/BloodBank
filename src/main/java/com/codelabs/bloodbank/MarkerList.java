package com.codelabs.bloodbank;

import java.io.Serializable;

/**
 * Created by abhigyan on 29/11/16.
 */
public class MarkerList implements Serializable {

    String name;
    String address;
    String mail;
    String number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
