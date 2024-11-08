package dev.carlinhos.lox.entities;

public class Break extends RuntimeException {
    public Break() {
        super(null, null, false, false);
    }
}
