package com.project.libraryapi.models.repositories;

import java.time.LocalDate;
import java.util.Optional;

import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.entities.Loan;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository loanRepository;

    @Test
    @DisplayName("Deve verificar se existe emprestimo nao devolvido para o livro")
    void returnTrueWhenIsbnExistsTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("user").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        boolean exists = loanRepository.existsByBookAndNotReturned(book);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar emprestimo pelo isbn do livro ou customer")
    void findByBookIsbnOrCustomerTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("user").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Loan> result = loanRepository.findByBookIsbnOrCustomer("123456", "user", pageRequest);

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    }
}