// IBookManager.aidl
package com.haijun.mystudyway;

// Declare any non-default types here with import statements

import com.haijun.mystudyway.Book;

interface IBookManager{
    List<Book> getBookList();
    void addBook(in Book book);
}