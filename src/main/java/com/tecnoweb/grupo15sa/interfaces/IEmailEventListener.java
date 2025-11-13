package com.tecnoweb.grupo15sa.interfaces;

import com.tecnoweb.grupo15sa.utils.Email;

import java.util.List;

public interface IEmailEventListener {
    void onReceiveEmailEvent(List<Email> emails);
}
