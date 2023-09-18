grammar SSSOMTransform;

ruleSet   : WS* (prefixDecl+ WS+)* (headerDecl+ WS+)* (rule WS+)+ EOF;

prefixDecl: 'prefix' WS+ PREFIX WS+ IRI;

headerDecl: action;

rule      : (tags WS+)? filterSet WS+ '->' WS+ actionSet
          | (tags WS+)? filterSet WS+ '{' (WS+ rule WS+)+ '}'
          ;
          
tags      : '[' TAG (',' TAG)* ']';

filterSet : filterItem (WS+ (binaryOp WS+)? filterItem)*;

filterItem: idFilterItem
          | multiIdFilterItem
          | numFilterItem
          | cardFilterItem
          | predicateModifierFilterItem
          | groupFilterItem
          | negatedFilterItem
          ;

idFilterItem                : idField '==' idValue;

multiIdFilterItem           : mulIdField '==' idValue;

numFilterItem               : numField numOp DOUBLE;

cardFilterItem              : cardField '==' CARDVALUE;

predicateModifierFilterItem : 'predicate_modifier==Not';

groupFilterItem             : '(' WS* filterSet WS* ')';

negatedFilterItem           : '!' WS* filterItem;

idField   : 'subject'
          | 'predicate'
          | 'object'
          | 'mapping_justification' | 'justification'
          | 'subject_source'
          | 'object_source'
          | 'mapping_source'
          ;

mulIdField: 'creator'
          | 'author'
          | 'reviewer'
          | 'curation_rule'
          | 'subject_match_field'
          | 'object_match_field'
          | 'subject_preprocessing'
          | 'object_preprocessing'
          ;

cardField : 'mapping_cardinality' | 'cardinality';

idValue   : CURIE
          | '*'
          ;

numField  : 'confidence'
          | 'semantic_similarity_score'
          ;

numOp     : '==' | '>' | '<' | '>=' | '<=';

binaryOp  : '&&'
          | '||'
          ;

actionSet : action
          | '{' ( WS* action WS* )+ '}'
          ;

action    : FUNCTION WS* arglist? WS* ')' WS* ';';

arglist   : argument (WS* ',' WS* argument)*;

argument  : string
          | CURIE
          | IRI
          ;

string    : SQ_STRING
          | DQ_STRING
          ;

FUNCTION  : [a-zA-Z_] [a-zA-Z0-9_]+ '(';

SQ_STRING : '\'' (SQ_ESCAPE|.)*? '\'';

DQ_STRING : '"' (DQ_ESCAPE|.)*? '"';

TAG       : [a-zA-Z0-9-]+;

CARDVALUE : '1:1' | '1:n' | 'n:1' | '1:0' | '0:1' | 'n:n' | '*:n' | '*:1' | 'n:*' | '1:*';

CURIE     : PREFIX [a-zA-Z0-9_*-]+;

PREFIX    : [a-zA-Z0-9_]+ ':';

IRI       : '<' [a-zA-Z0-9_/:#=.-]+ '>';

DOUBLE    : [0-9]+ '.' [0-9]+;

COMMENT   : '#' .*? NL -> skip;

WS        : ' ' | '\t' | NL;

NL        : '\r'? '\n';

fragment
SQ_ESCAPE : '\\\'' | '\\\\';

fragment
DQ_ESCAPE : '\\"' | '\\\\';
