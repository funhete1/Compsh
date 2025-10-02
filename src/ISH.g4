grammar ISH;

// Parser Rules

prog    : statement* EOF;

statement   : assignStat ';' 
            | outStat  
            ;

assignStat
            : inputExpr '|' storeExpr
            ;

inputExpr   : 'integer' '(' 'stdin' STRING ')' ;

storeExpr   : 'store' 'in' ID ':' 'integer' ;

outStat     : expr '|'  'stdout'    
            ;

expr        : exprAddSub ;

exprAddSub  
            : exprAddSub op=('+'|'-'|'*'|'/') exprAtom   #AddSubExpr
            | exprAtom                           #ToAtomFromAdd
            ;

exprAtom
            : INT                                #Int
            | ID                                 #Var
            | '(' exprAddSub ')'                 #Parens
            ;


// ---------- Lexer Rules ----------

INT     : [0-9]+ ;
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
STRING  : '"' (~["\r\n])* '"' ;       // cadeia entre aspas
WS      : [ \t\r\n]+ -> skip ;
COMMENT : '#' ~[\r\n]* -> skip ;