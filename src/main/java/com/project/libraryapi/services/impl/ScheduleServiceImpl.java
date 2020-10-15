package com.project.libraryapi.services.impl;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.project.libraryapi.models.entities.Loan;
import com.project.libraryapi.services.EmailService;
import com.project.libraryapi.services.LoanService;
import com.project.libraryapi.services.ScheduleService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    @Value("${application.mail.late-loan.message}")
    private String message;

    private final LoanService loanService;
    private final EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    @Override
    public void sendMailToLateLoans() {
        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailList = allLateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());

        emailService.sendMails(mailList, message);
    }
}
