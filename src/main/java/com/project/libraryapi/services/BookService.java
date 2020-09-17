package com.project.libraryapi.services;

import java.util.Optional;

import com.project.libraryapi.models.entities.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    Page<Book> find(Book filter, Pageable pageRequest);

    Optional<Book> getById(Long id);

    Book save(Book book);

    Book update(Book book);

    void delete(Book book);
}
