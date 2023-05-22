package ru.practicum.shareit.exception;

public class WrongParam extends IllegalArgumentException {
    public WrongParam(String message) {
        super(message);
    }
}
