package dev.carlinhos.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Grouping : Expr expression",
                "Assign   : Token name, Expr value",
                "Ternary  : Expr condition, Expr left, Expr right",
                "Variable : Token name",
                "Logical  : Expr left, Token operator, Expr right",
                "Binary   : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Comma    : Expr left, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Lambda   : List<Token> params, List<Stmt> body",
                "Literal  : Object value",
                "Get      : Expr object, Token name",
                "Set      : Expr object, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
                "Var        : Token name, Expr initializer",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "Lambda     : List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Break      : Token keyword",
                "While      : Expr condition, Stmt body",
                "Block      : List<Stmt> statements",
                "Expression : Expr expression"
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package dev.carlinhos.lox.entities;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.println();
        writer.close();
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
        writer.println();
    }

    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println("  public static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("    public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
        writer.println();
    }
}