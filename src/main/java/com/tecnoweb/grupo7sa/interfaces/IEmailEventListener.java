package com.tecnoweb.grupo7sa.interfaces;

import com.tecnoweb.grupo7sa.utils.Email;

import java.util.List;

public interface IEmailEventListener {
    void onReceiveEmailEvent(List<Email> emails);
}
