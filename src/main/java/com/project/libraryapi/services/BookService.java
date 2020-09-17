package com.project.libraryapi.services;

import java.util.Optional;

import com.project.libraryapi.models.entities.Book;

public interface BookService {

    Optional<Book> getById(Long id);

    Book save(Book book);

    Book update(Book book);

    void delete(Book book);
}
