grammar SSSOMTransform;

ruleSet   : WS* (prefixDecl+ WS+)* (rule WS+)+ EOF;

prefixDecl: 'prefix' WS+ PREFIX WS+ IRI;

rule      : (tags WS+)? filterSet WS+ '->' WS+ actionSet
          | (tags WS+)? filterSet WS+ '{' (WS+ rule WS+)+ '}'
          ;
          
tags      : '[' TAG (',' TAG)* ']';

filterSet : filterItem (WS+ (binaryOp WS+)? filterItem)*;

filterItem: idFilterItem
          | multiIdFilterItem
          | numFilterItem
          | predicateModifierFilterItem
          | groupFilterItem
          | negatedFilterItem
          ;

idFilterItem                : idField '==' idValue;

multiIdFilterItem           : mulIdField '==' idValue;

numFilterItem               : numField numOp DOUBLE;

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

mulIdField: 'creator'
          | 'author'
          | 'reviewer'
          | 'curation_rule'
          | 'subject_match_field'
          | 'object_match_field'
          | 'subject_preprocessing'
          | 'object_preprocessing'
          ;

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

DOUBLE    : [0-9]+ '.' [0-9]+;

COMMENT   : '#' .*? NL -> skip;

WS        : ' ' | '\t' | NL;

NL        : '\r'? '\n';

fragment
SQ_ESCAPE : '\\\'' | '\\\\';

fragment
DQ_ESCAPE : '\\"' | '\\\\';
