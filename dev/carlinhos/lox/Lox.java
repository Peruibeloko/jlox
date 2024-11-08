package dev.carlinhos.lox;

import dev.carlinhos.lox.entities.Stmt;
import dev.carlinhos.lox.entities.Token;
import dev.carlinhos.lox.entities.TokenType;
import dev.carlinhos.lox.passes.Interpreter;
import dev.carlinhos.lox.passes.Parser;
import dev.carlinhos.lox.passes.Resolver;
import dev.carlinhos.lox.passes.Scanner;
import dev.carlinhos.lox.runtime.RuntimeError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            System.out.println(run(line));
            hadError = false;
        }
    }

    private static Object run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return null;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if (hadError) return null;

        if (statements.size() == 1) return interpreter.interpret(statements.getFirst());

        interpreter.interpret(statements);
        return null;
    }

    public static void error(int line, String message) {
        reportError(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            reportError(token.line, " at end", message);
        } else {
            reportError(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void warn(Token token, String message) {
        if (token.type == TokenType.EOF) {
            reportWarning(token.line, " at end", message);
        } else {
            reportWarning(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[line " + error.token.line + "] " + error.getMessage());
        hadRuntimeError = true;
    }

    private static void reportError(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    private static void reportWarning(int line, String where, String message) {
        System.out.println("[line " + line + "] Warning" + where + ": " + message);
    }
}
