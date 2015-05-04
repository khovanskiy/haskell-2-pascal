package com.khovanskiy.haskell.core;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class HaskellToPascalListener extends HaskellBaseListener {
    HaskellParser parser;
    boolean first_def;
    boolean cond;
    Map<String, String> m;
    Stack<Integer> args;

    public HaskellToPascalListener(HaskellParser parser) {
        this.parser = parser;
        args = new Stack<Integer>();
    }

    @Override public void enterFnDecl(@NotNull HaskellParser.FnDeclContext ctx) {
        String decl = "function " + ctx.ID() + "(";
        char cur = 'a';
        for (int i = 0; i < ctx.TYPE().size() - 1; i++) {
            TerminalNode each = ctx.TYPE().get(i);
            decl = decl + cur + ": " + each + ", ";
            ++cur;
        }

        TerminalNode each = ctx.TYPE().get(ctx.TYPE().size() - 1);
        if(ctx.TYPE().size() == 1) {
            decl += "): " + each + ";";
        } else {
            decl = decl.substring(0, decl.length() - 2) + "): " + each + ";";
        }

        System.out.println(decl);
        System.out.println("begin");
        this.first_def = true;
    }

    @Override public void exitFn(@NotNull HaskellParser.FnContext ctx) {
        System.out.println("end;\n");
    }

    @Override public void enterFnDef(@NotNull HaskellParser.FnDefContext ctx) {
        m = new HashMap<String, String>();
        boolean first_if = true;
        cond = false;
        for(int i = 0; i < ctx.i; ++i)
        {
            if(ctx.arg(i).ID != null) {
                if(m.containsKey(ctx.arg(i).ID().toString()))
                {
                    if(first_if) {
                        if(first_def) {
                            System.out.print("\tif");
                            first_def = false;
                        }
                        else {
                            System.out.print("\telse if");
                        }
                        first_if = false;
                    } else {
                        System.out.print(" &&");
                    }
                    System.out.print(" (" + m.get(ctx.arg(i).ID().toString()) + " = " + String.valueOf((char) ('a' + i)) + ")");
                    cond = true;
                } else {
                    m.put(ctx.arg(i).ID().toString(), String.valueOf((char) ('a' + i)));
                }
            } else {
                if(first_if) {
                    if(first_def) {
                        System.out.print("\tif");
                        first_def = false;
                    }
                    else {
                        System.out.print("\telse if");
                    }
                    first_if = false;
                } else {
                    System.out.print(" &&");
                }
                System.out.print(" (" + String.valueOf((char)('a' + i)) + " = " + ctx.arg(i).getText() + ")");
                cond = true;
            }
        }

        if (ctx.cond() == null) {
            if(cond) {
                System.out.print(" then\n\t\t");
            } else {
                System.out.print("\t");
                first_def = true;
            }
            System.out.print(ctx.ID().toString() + " := ");
        }
    }

    @Override public void exitFnDef(@NotNull HaskellParser.FnDefContext ctx) {
        System.out.println(';');
    }

    @Override public void enterCond(@NotNull HaskellParser.CondContext ctx) {
        if (!cond) {
            if (first_def) {
                System.out.print("\tif ");
                first_def = false;
            } else {
                System.out.print("\telse if ");
            }
        } else {
            System.out.print(" && ");
        }
        System.out.print("(");
    }

    @Override public void exitCond(@NotNull HaskellParser.CondContext ctx) {
        System.out.print(") then\n\t\t");
        System.out.print(ctx.getParent().getChild(0).getText() + " := ");
    }

    @Override public void enterFoo_call(@NotNull HaskellParser.Foo_callContext ctx) {
        System.out.print(ctx.ID() + "(");
        args.push(ctx.i);
    }

    @Override public void exitFoo_call(@NotNull HaskellParser.Foo_callContext ctx) {
        System.out.print(")");
    }

    @Override public void exitCall_arg(@NotNull HaskellParser.Call_argContext ctx) {
        int v = args.pop() - 1;
        if(v != 0) {
            args.push(v);
            System.out.print(", ");
        }
    }

    @Override public void enterAnd_or(@NotNull HaskellParser.And_orContext ctx) {
        System.out.print(" " + ctx.getText() + " ");
    }

    @Override public void enterEquality(@NotNull HaskellParser.EqualityContext ctx) {
        System.out.print(" " + ctx.getText() + " ");
    }

    @Override public void enterMult_div(@NotNull HaskellParser.Mult_divContext ctx) {
        System.out.print(" " + ctx.getText() + " ");
    }

    @Override public void enterPlus_minus(@NotNull HaskellParser.Plus_minusContext ctx) {
        System.out.print(" " + ctx.getText() + " ");
    }

    @Override public void enterNegate(@NotNull HaskellParser.NegateContext ctx) {
        System.out.print(ctx.getText());
    }

    @Override public void enterPrimitive(@NotNull HaskellParser.PrimitiveContext ctx) {
        System.out.print(ctx.getText());
    }

    @Override
    public void enterOpenBracket(@NotNull HaskellParser.OpenBracketContext ctx) {
        System.out.print(ctx.getText());
    }

    @Override
    public void enterCloseBracket(@NotNull HaskellParser.CloseBracketContext ctx) {
        System.out.print(ctx.getText());
    }

    @Override public void enterId(@NotNull HaskellParser.IdContext ctx) {
        System.out.print(m.get(ctx.getText()));
    }
}