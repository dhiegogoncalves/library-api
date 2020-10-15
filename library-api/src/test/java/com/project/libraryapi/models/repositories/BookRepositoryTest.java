package com.project.libraryapi.models.repositories;

import java.util.Optional;

import com.project.libraryapi.models.entities.Book;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    void returnTrueWhenIsbnExistsTest() {
        String isbn = "123456";
        Book book = Book.builder().title("The legend").author("Test").isbn(isbn).build();
        entityManager.persist(book);

        boolean exists = bookRepository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    void returnFalseWhenIsbnExistsTest() {
        String isbn = "123456";

        boolean exists = bookRepository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    void findByIdTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();
        entityManager.persist(book);

        Optional<Book> foundBook = bookRepository.findById(book.getId());

        Assertions.assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    void saveBookTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();

        Book savedBook = bookRepository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest() {
        Book book = Book.builder().title("The legend").author("Test").isbn("123456").build();
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());
        bookRepository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        Assertions.assertThat(deletedBook).isNull();
    }
}
