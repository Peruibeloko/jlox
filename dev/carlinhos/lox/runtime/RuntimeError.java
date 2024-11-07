package dev.carlinhos.lox.runtime;

import dev.carlinhos.lox.entities.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
