package com.khovanskiy.haskell.core;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.*;
import java.util.*;

public class HaskellToPascalListener extends HaskellBaseListener {
    private boolean isFirstDefinition;
    private boolean hasCondition;
    private Map<String, String> argsNames = new HashMap<>();
    private LinkedList<Integer> args = new LinkedList<>();
    private final PrintStream os;

    public HaskellToPascalListener(PrintStream os) {
        this.os = os;
    }

    private String convertType(String type) {
        switch (type) {
            case "Integer":
                return "integer";
            case "Float":
                return "real";
            case "Char":
                return "char";
            case "String":
                return "string";
            case "Bool":
                return "boolean";
        }
        throw new AssertionError();
    }

    @Override public void enterFnDecl(@NotNull HaskellParser.FnDeclContext ctx) {
        String decl = "function " + ctx.ID() + "(";
        char cur = 'a';
        for (int i = 0; i < ctx.TYPE().size() - 1; i++) {

            decl = decl + cur + ": " +  convertType(ctx.TYPE().get(i).getText()) + ", ";
            ++cur;
        }

        TerminalNode each = ctx.TYPE().get(ctx.TYPE().size() - 1);
        if(ctx.TYPE().size() == 1) {
            decl += "): " + convertType(each.getText()) + ";";
        } else {
            decl = decl.substring(0, decl.length() - 2) + "): " + convertType(each.getText()) + ";";
        }

        os.println(decl);
        os.println("begin");
        this.isFirstDefinition = true;
    }

    @Override public void exitFn(@NotNull HaskellParser.FnContext ctx) {
        os.println("end;\n");
    }

    @Override public void enterFnDef(@NotNull HaskellParser.FnDefContext ctx) {
        //argsNames = new HashMap<>();
        argsNames.clear();
        boolean isFirstCondition = true;
        hasCondition = false;
        for(int i = 0; i < ctx.i; ++i)
        {
            if(ctx.arg(i).ID != null) {
                if(argsNames.containsKey(ctx.arg(i).ID().toString()))
                {
                    if(isFirstCondition) {
                        if(isFirstDefinition) {
                            os.print("\tif");
                            isFirstDefinition = false;
                        }
                        else {
                            os.print("\telse if");
                        }
                        isFirstCondition = false;
                    } else {
                        os.print(" &&");
                    }
                    os.print(" (" + argsNames.get(ctx.arg(i).ID().toString()) + " = " + String.valueOf((char) ('a' + i)) + ")");
                    hasCondition = true;
                } else {
                    argsNames.put(ctx.arg(i).ID().toString(), String.valueOf((char) ('a' + i)));
                }
            } else {
                if(isFirstCondition) {
                    if(isFirstDefinition) {
                        os.print("\tif");
                        isFirstDefinition = false;
                    }
                    else {
                        os.print("\telse if");
                    }
                    isFirstCondition = false;
                } else {
                    os.print(" &&");
                }
                os.print(" (" + String.valueOf((char)('a' + i)) + " = " + ctx.arg(i).getText() + ")");
                hasCondition = true;
            }
        }

        if (ctx.cond() == null) {
            if(hasCondition) {
                os.print(" then\n\t\t");
            } else {
                os.print("\t");
                isFirstDefinition = true;
            }
            os.print(ctx.ID().toString() + " := ");
        }
    }

    @Override public void exitFnDef(@NotNull HaskellParser.FnDefContext ctx) {
        os.println(';');
    }

    @Override public void enterCond(@NotNull HaskellParser.CondContext ctx) {
        if (!hasCondition) {
            if (isFirstDefinition) {
                os.print("\tif ");
                isFirstDefinition = false;
            } else {
                os.print("\telse if ");
            }
        } else {
            os.print(" && ");
        }
        os.print("(");
    }

    @Override public void exitCond(@NotNull HaskellParser.CondContext ctx) {
        os.print(") then\n\t\t");
        os.print(ctx.getParent().getChild(0).getText() + " := ");
    }

    @Override public void enterFoo_call(@NotNull HaskellParser.Foo_callContext ctx) {
        os.print(ctx.ID() + "(");
        args.push(ctx.i);
    }

    @Override public void exitFoo_call(@NotNull HaskellParser.Foo_callContext ctx) {
        os.print(")");
    }

    @Override public void exitCall_arg(@NotNull HaskellParser.Call_argContext ctx) {
        int v = args.pop() - 1;
        if(v != 0) {
            args.push(v);
            os.print(", ");
        }
    }

    @Override public void enterAnd(@NotNull HaskellParser.AndContext ctx) {
        os.print(" and ");
    }

    @Override public void enterOr(@NotNull HaskellParser.OrContext ctx) {
        os.print(" or ");
    }

    @Override public void enterEquality(@NotNull HaskellParser.EqualityContext ctx) {
        if (ctx.getText().equals("==")) {
            os.print(" = ");
        } else if (ctx.getText().equals("/=")) {
            os.print(" <> ");
        } else {
            os.print(" " + ctx.getText() + " ");
        }
    }

    @Override public void enterMult_div(@NotNull HaskellParser.Mult_divContext ctx) {
        os.print(" " + ctx.getText() + " ");
    }

    @Override public void enterPlus_minus(@NotNull HaskellParser.Plus_minusContext ctx) {
        os.print(" " + ctx.getText() + " ");
    }

    @Override public void enterNegate(@NotNull HaskellParser.NegateContext ctx) {
        if (ctx.getText().equals("!")) {
            os.print("not ");
        } else {
            os.print(ctx.getText());
        }
    }

    @Override public void enterPrimitive(@NotNull HaskellParser.PrimitiveContext ctx) {
        if (ctx.getText().equals("True") || ctx.getText().equals("False")) {
            os.print(ctx.getText().toLowerCase());
        } else {
            os.print(ctx.getText());
        }
    }

    @Override
    public void enterOpenBracket(@NotNull HaskellParser.OpenBracketContext ctx) {
        os.print(ctx.getText());
    }

    @Override
    public void enterCloseBracket(@NotNull HaskellParser.CloseBracketContext ctx) {
        os.print(ctx.getText());
    }

    @Override public void enterId(@NotNull HaskellParser.IdContext ctx) {
        os.print(argsNames.get(ctx.getText()));
    }
}