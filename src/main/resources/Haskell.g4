grammar Haskell;

@header {
package com.khovanskiy.haskell.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
}

@parser::members {
Map<String, Integer> foos = new HashMap<String, Integer>();
}

program : fn* ;

fn : fnDecl fnDef+ ;

fnDecl : ID {foos.put($ID.text, 0);} '::' TYPE ('->' TYPE {foos.put($ID.text, foos.get($ID.text) + 1);})* ;

fnDef
locals [Set<String> args = new HashSet<String>();, int i = 0]
     : {foos.containsKey(getCurrentToken().getText())}? ID ({$i < foos.get($ID.text)}? arg {$i++;})* {$i == foos.get($ID.text)}? cond? '=' expr ;

cond : '|' expr ;

expr : expr and_or expr
     | expr equality expr
	 | expr mult_div expr
     | expr plus_minus expr
	 | foo_call
	 | {$fnDef::args.contains(getCurrentToken().getText())}? id
	 | negate expr
     | primitive
     | openBracket expr closeBracket
     ;

foo_call
locals [int i = 0;]
     : {foos.containsKey(getCurrentToken().getText())}? ID ({$i < foos.get($ID.text)}? call_arg {$i++;})*;

call_arg : expr ;

primitive : NUMBER | STRING | CHAR ;
arg : ID {$fnDef::args.add($ID.text);} | NUMBER | STRING | CHAR ;

and_or : '&&' | '||';
equality : '==' | '!=' | '<=' | '>=' | '>' | '<' ;
mult_div : '*' | '/' ;
plus_minus : '+' | '-'  ;
negate : '-' | '!' ;
openBracket : '(' ;
closeBracket : ')' ;
id : ID ;

NUMBER : '0' | ('-'? ('1'..'9') DIGIT*)
	   | '-'? DIGIT '.'? DIGIT+ ;
CHAR : '\'' .*? '\'' ;
STRING : '"' (ESC | .)*? '"' ;

TYPE : 'Integer' | 'Char' | 'Float' | 'String' ;
ID : LETTER (LETTER | DIGIT)* ;

fragment LETTER : [a-zA-Z_] ;
fragment DIGIT : [0-9] ;
fragment ESC : '\\' [btnr"\\] ;

WS : [ \t\r\n]+ -> channel(HIDDEN) ;