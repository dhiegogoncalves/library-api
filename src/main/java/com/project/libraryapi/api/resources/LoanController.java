package com.project.libraryapi.api.resources;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.project.libraryapi.api.dtos.LoanDTO;
import com.project.libraryapi.api.dtos.LoanFilterDTO;
import com.project.libraryapi.api.dtos.ReturnedLoanDTO;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.entities.Loan;
import com.project.libraryapi.services.BookService;
import com.project.libraryapi.services.LoanService;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO loanFilterDTO, Pageable pageRequest) {
        Page<Loan> result = loanService.find(loanFilterDTO, pageRequest);
        List<LoanDTO> list = result.getContent().stream().map(entity -> modelMapper.map(entity, LoanDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanFilterDTO loanDTO) {
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));
        Loan loan = Loan.builder().book(book).customer(loanDTO.getCustomer()).loanDate(LocalDate.now()).build();

        loan = loanService.save(loan);
        return loan.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO returnedLoanDTO) {
        Loan loan = loanService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(returnedLoanDTO.getReturned());
        loanService.update(loan);
    }
}
