package dev.carlinhos.lox.runtime;

import dev.carlinhos.lox.entities.Return;
import dev.carlinhos.lox.entities.Stmt;
import dev.carlinhos.lox.entities.Expr;
import dev.carlinhos.lox.entities.Token;
import dev.carlinhos.lox.passes.Interpreter;

import java.util.List;

public class LoxLambda implements LoxCallable {

    private final List<Token> params;
    private final List<Stmt> body;
    private final Environment closure;

    public LoxLambda(Stmt.Lambda declaration, Environment closure) {
        this.params = declaration.params;
        this.body = declaration.body;
        this.closure = closure;
    }

    public LoxLambda(Expr.Lambda declaration, Environment closure) {
        this.params = declaration.params;
        this.body = declaration.body;
        this.closure = closure;
    }

    @Override
    public String toString() {
        return "<anonymous fn>";
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        Environment environment = new Environment(closure);

        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }
}
