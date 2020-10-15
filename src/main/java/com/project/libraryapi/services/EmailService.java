package com.project.libraryapi.services;

import java.util.List;

public interface EmailService {

    void sendMails(List<String> mailList, String message);
}
