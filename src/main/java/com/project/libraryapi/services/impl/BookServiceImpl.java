package com.project.libraryapi.services.impl;

import com.project.libraryapi.models.entities.Book;
import com.project.libraryapi.models.repositories.BookRepository;
import com.project.libraryapi.services.BookService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

}
