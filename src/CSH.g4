//////////////////////////////////////////////////////////////////////
//
//    CompSh (grammar)
//
//////////////////////////////////////////////////////////////////////

grammar CSH;

//////////////////////////////////////////////////
/// Parser Rules
//////////////////////////////////////////////////

@parser::header {
        import java.util.Map;
        import java.util.HashMap;
}

@parser::members {
        static protected Map<String,Type> symbolTable = new HashMap<>();
}

main : lineStat* EOF;

lineStat
        : loop #loopExpr
        | decision #decisionExpr
        | statList #pipeList
        | variableInitializer #variableInit
        ;

lineStat2
        : lineStat
        ;

statList
        : stat (PIPE stat)*
        ;

stat returns[Statement statement]
        : 'stdin' (STRINGVAL?)         #statStdin
        | expr                      #statExpr
        | execute                   #statExecuteExpression
        | internalFunctionsStat     #statInternalFunctions
        | 'stdout'                  #statStdout
        | 'stderr'                  #statStderr
        | store                     #statStore
        ;

expr returns[Type eType,String varName]
        : e1=expr op=('*'|'/'|'%') e2=expr    #ExprMultDivMod 
        | e1=expr op=('+'|'-') e2=expr        #ExprAddSub
        | '(' e=expr ')'                    #ExprParent
        | REAL                          #exprReal
        | STRINGVAL                     #exprStr
        | VAR                           #exprVar
        | INT                           #exprInt
        | ('text' | 'integer' | 'real') '(' expr ')'  #exprConversion
        | op=('+'|'-') e=expr             #ExprNegPos 
        ;

internalFunctionsStat
        : 'NL'
        ;

variableInitializer returns[String varName, Type eType]
        : VAR ':' varType (SEMICOLON?)
        ;

varType returns[Type res]
        : 'text' {$res = new TextType();}
        | 'program' {$res = new ProgramType();}
        | 'integer' {$res = new IntegerType();}
        | 'real' {$res = new RealType();}
        ;

loop
        : 'loop' (lineStat)* 'until' condition 'end' #loopTail
        | 'loop while' condition 'do' (lineStat)* 'end' #loopHead
        | 'loop' (lineStat)* 'while' condition 'do' (lineStat2)* 'end' #loopMiddle
        ;

decision
        : 'if' condition 'then' (lineStat)* 'end' #decisionSimple
        | 'if' condition 'then' (lineStat)* 'else' (lineStat2)* 'end' #decisionWithElse
        ;

condition
        : e1=expr op=operator e2=expr
        ;

operator returns[String op]
        : '=' {$op = "==";}
        | '/=' {$op = "!=";}
        | '<' {$op = "<";}
        | '>' {$op = ">";}
        ;

//maybe need to review this part if the label dosent 
//make the visitor easier should be 
//"!"(STRINGVAL | VAR)"!"
execute returns[String cmd]
        : '!'command=(STRINGVAL|VAR)'!'         #executeStringVal
        | '!!'STRINGVAL'!!'             #excuteSecondGrammar
        ;
store returns[String varName, Type eType]
    : ('store in'|'store') (VAR | variableInitializer)    #storeVar
    ;
//////////////////////////////////////////////////
/// Lexer Rules
//////////////////////////////////////////////////

VAR : ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'_'|'0'..'9')* ;
SEMICOLON : ';' ;
COMMA : ',' ;
QUOTE : '"' ;
REAL: [0-9]+'.'[0-9]+;
INT : ('0'..'9')+ ;
STRINGVAL : QUOTE (~'"'|'\\"')* QUOTE ;
FILENAME : ('a'..'z'|'A'..'Z'|'_'|'-'|'0'..'9')* '.' ('a'..'z'|'A'..'Z'|'0'..'9')* ;
PIPE: '|';




fragment STRING : (~'"'|'\\"')* ;

/*INTVAL    : ('1'..'9')+ ;
EQUAL     : '==' ;
NOTEQUAL  : '!=' ;
LESS      : '<' ;
LESSEQ    : '<=' ;
BIGGER    : '>' ;
BIGGEREQ  : '>=' ;
PLUS      : '+' ;
MINUS     : '-' ;
MUL       : '*' ;
DIV       : '/' ;
NOT       : '!' ;
AND       : '&&' ;
OR        : '||' ;
IF        : 'if' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
FOR       : 'for' ;
CASE      : 'case' ;
DEFAULT   : 'default' ;
BREAK     : 'break' ;
SWITCH    : 'switch' ;
CONTINUE  : 'continue';
BOOLEAN   : TRUE | FALSE ;
TRUE      : 'true' ;
FALSE     : 'false' ;
DOUBLEVAL : (('1'..'9')* '.' ('0'..'9')+) ;
*/

WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '#' .*? '\n' -> skip;
ERROR: .;
