package dev.carlinhos.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: ast_printer <input file>");
            System.exit(64);
        }

        // Reads file
        String inputFile = args[0];
        byte[] bytes = Files.readAllBytes(Paths.get(inputFile));

        // Scans and parses
        Scanner scanner = new Scanner(new String(bytes, Charset.defaultCharset()));
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        AstPrinter printer = new AstPrinter();

        for (Stmt stmt : statements) {
            System.out.println(stmt.accept(printer));
        }
    }

    private <T> String parenthesize(String name, T... nodes) {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        if (!name.isEmpty()) builder.append(name);

        for (T node : nodes) {
            if (!name.isEmpty()) builder.append(" ");
            if (node instanceof Expr) {
                builder.append(((Expr) node).accept(this));
            } else if (node instanceof Stmt) {
                builder.append(((Stmt) node).accept(this));
            }
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {

        List<String> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            args.add("(" + arg.accept(this) + ")");
        }
        return parenthesize(expr.callee.accept(this), args.toArray());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("=", expr.value, new Expr.Literal(expr.name.lexeme));
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmts) {
        StringBuilder builder = new StringBuilder();

        for (Stmt stmt : stmts.statements) {
            builder.append(stmt.accept(this));
        }

        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {

        String params = "(";
        params += stmt.params.stream().map(tk -> tk.lexeme).collect(Collectors.joining(" "));
        params += ")";

        return parenthesize("fun", stmt.name, params, new Stmt.Block(stmt.body));
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();

        builder
                .append("(if ")
                .append(parenthesize("", stmt.condition))
                .append(parenthesize("then", stmt.thenBranch))
                .append(parenthesize("else", stmt.elseBranch))
                .append(")");

        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return parenthesize("return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return parenthesize("var", new Expr.Literal(stmt.name.lexeme));
        }

        return parenthesize("var", new Expr.Literal(stmt.name.lexeme), stmt.initializer);
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        StringBuilder builder = new StringBuilder();

        builder
                .append("(while ")
                .append(stmt.condition.accept(this))
                .append(stmt.body.accept(this))
                .append(")");

        return builder.toString();
    }
}
