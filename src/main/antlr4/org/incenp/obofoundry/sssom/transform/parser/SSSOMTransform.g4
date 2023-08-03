grammar SSSOMTransform;

ruleSet   : WS* (prefixDecl+ WS+)* (rule WS+)+ EOF;

prefixDecl: 'prefix' WS+ PREFIX WS+ IRI;

rule      : (tags WS+)? filterSet WS+ '->' WS+ actionSet
          | (tags WS+)? filterSet WS+ '{' (WS+ rule WS+)+ '}'
          ;
          
tags      : '[' TAG (',' TAG)* ']';

filterSet : filterItem (WS+ (binaryOp WS+)? filterItem)*;

filterItem: idFilterItem
          | predicateModifierFilterItem
          | groupFilterItem
          | negatedFilterItem
          ;

idFilterItem                : idField '==' idValue;

predicateModifierFilterItem : 'predicate_modifier==Not';

groupFilterItem             : '(' WS* filterSet WS* ')';

negatedFilterItem           : '!' WS* filterItem;

idField   : 'subject'
          | 'predicate'
          | 'object'
          | 'mapping_justification'
          | 'subject_source'
          | 'object_source'
          | 'mapping_source'
          ;

idValue   : CURIE
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
