package ru.practicum.shareit.exception;

public class UnsupportedStatus extends IllegalStateException {
    public UnsupportedStatus(String message) {
        super(message);
    }
}
