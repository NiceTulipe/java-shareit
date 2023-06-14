package ru.practicum.shareit.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String s) {
        super(s);
    }
}