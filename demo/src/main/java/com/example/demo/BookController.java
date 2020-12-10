package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.jooq.tables.pojos.Book;

@RestController
public class BookController {

    @Autowired
    BookService bookService;

    @GetMapping
    public List<Book> getBooks(){
        return this.bookService.getBooks();
    }

    @PostMapping
    public void postBook(@RequestBody Book book) throws Exception{
        this.bookService.insertBook(book);
    }
}