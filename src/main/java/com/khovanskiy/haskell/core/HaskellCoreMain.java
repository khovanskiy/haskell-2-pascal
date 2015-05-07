package com.khovanskiy.haskell.core;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class HaskellCoreMain {
    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        ANTLRInputStream input = new ANTLRInputStream(is);

        HaskellLexer lexer = new HaskellLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HaskellParser parser = new HaskellParser(tokens);
        ParseTree tree = parser.program(); // parse

        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        HaskellToPascalListener extractor = new HaskellToPascalListener(System.out);
        walker.walk(extractor, tree); // initiate walk of tree with listener
    }
}