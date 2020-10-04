package com.project.libraryapi.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.project.libraryapi.api.dtos.LoanFilterDTO;
import com.project.libraryapi.api.exceptions.BusinessException;
import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.entities.Loan;
import com.project.libraryapi.models.repositories.LoanRepository;
import com.project.libraryapi.services.impl.LoanServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class LoanServiceTest {

    LoanService loanService;

    @MockBean
    LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve filtrar emprestimos pelas propriedades")
    void findBooksTest() {
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().isbn("123456").customer("user").build();
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan loan = Loan.builder().id(1l).book(book).customer("user").loanDate(LocalDate.now()).returned(false).build();

        List<Loan> loanList = Arrays.asList(loan);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Loan> page = new PageImpl<Loan>(loanList, pageRequest, 1);

        Mockito.when(loanRepository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(PageRequest.class))).thenReturn(page);

        Page<Loan> result = loanService.find(loanFilterDTO, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(loanList);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve salvar o livro")
    void saveLoanTest() {
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan savingLoan = Loan.builder().book(book).customer("user").loanDate(LocalDate.now()).build();
        Loan savedLoan = Loan.builder().id(1l).book(book).customer("user").loanDate(LocalDate.now()).build();

        Mockito.when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(loanRepository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = loanService.save(savingLoan);

        Assertions.assertThat(loan.getId()).isNotNull();
        Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lancar erro ao salvar um emprestimo com livro ja emprestado")
    void loanedBookSaveTest() {
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan savingLoan = Loan.builder().book(book).customer("user").loanDate(LocalDate.now()).build();

        Mockito.when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> loanService.save(savingLoan));

        Assertions.assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Book already loaned");

        Mockito.verify(loanRepository, Mockito.never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informacoes de um emprestimo pelo Id")
    void getLoanDetailsTest() {
        Long id = 1l;
        Book book = Book.builder().id(id).title("The legend").author("Test").isbn("123456").build();
        Loan loan = Loan.builder().id(id).book(book).customer("user").loanDate(LocalDate.now()).build();

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.getById(id);

        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());
        Mockito.verify(loanRepository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    void updateLoanTest() {
        Book book = Book.builder().id(1l).title("The legend").author("Test").isbn("123456").build();
        Loan loan = Loan.builder().id(1l).book(book).customer("user").loanDate(LocalDate.now()).returned(false).build();

        Mockito.when(loanRepository.save(loan)).thenReturn(loan);

        Loan updatedLoan = loanService.save(loan);

        Assertions.assertThat(updatedLoan.getId()).isEqualTo(loan.getId());
        Assertions.assertThat(updatedLoan.getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(updatedLoan.getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(updatedLoan.getLoanDate()).isEqualTo(loan.getLoanDate());
        Mockito.verify(loanRepository).save(loan);
    }
}
