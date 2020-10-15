package com.project.libraryapi.services;

import java.util.List;
import java.util.Optional;

import com.project.libraryapi.api.dtos.LoanFilterDTO;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.entities.Loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO loanFilterDTO, Pageable pageable);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();
}
