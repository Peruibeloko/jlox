package dev.carlinhos.lox.runtime;

import dev.carlinhos.lox.passes.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}