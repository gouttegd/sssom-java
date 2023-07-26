grammar SSSOMTransform;

ruleSet   : WS* (prefixDecl+ WS+)* (rule WS+)+ EOF;

prefixDecl: 'prefix' WS+ PREFIX WS+ IRI;

rule      : (tags WS+)? filterSet WS+ '->' WS+ actionSet
          | (tags WS+)? filterSet WS+ '{' (WS+ rule WS+)+ '}'
          ;
          
tags      : '[' TAG (',' TAG)* ']';

filterSet : filterItem (WS+ (binaryOp WS+)? filterItem)*;

filterItem: field '==' value                               #singleFilterItem
          | 'predicate_modifier==Not'                      #predicateModifierFilterItem
          | '(' WS* filterSet WS* ')'                      #groupFilterItem
          | '!' WS* filterItem                             #negatedFilterItem
          ;

field     : 'subject'
          | 'predicate'
          | 'object'
          ;

value     : CURIE
          | '*'
          ;

binaryOp  : '&&'
          | '||'
          ;

actionSet : action
          | '{' ( WS* action WS* )+ '}'
          ;

action    : stop
          | invert
          | generate
          ;

stop      : 'stop();';

invert    : 'invert();';

generate  : 'gen(' string ');';

string    : SQ_STRING
          | DQ_STRING
          ;

SQ_STRING : '\'' (SQ_ESCAPE|.)*? '\'';

DQ_STRING : '"' (DQ_ESCAPE|.)*? '"';

TAG       : [a-zA-Z0-9-]+;

CURIE     : PREFIX [a-zA-Z0-9_*]+;

PREFIX    : [a-zA-Z0-9_]+ ':';

IRI       : '<' [a-zA-Z0-9_/:#=.-]+ '>';

COMMENT   : '#' .*? NL -> skip;

WS        : ' ' | '\t' | NL;

NL        : '\r'? '\n';

fragment
SQ_ESCAPE : '\\\'' | '\\\\';

fragment
DQ_ESCAPE : '\\"' | '\\\\';
