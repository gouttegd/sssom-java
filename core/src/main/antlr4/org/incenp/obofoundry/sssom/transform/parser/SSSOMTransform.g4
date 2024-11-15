grammar SSSOMTransform;

ruleSet   : WS* (prefixDecl+ WS+)* (directive+ WS+)* (rule WS+)+ EOF;

prefixDecl: 'prefix' WS+ PREFIX WS+ IRI;

directive : action;

rule      : (tags WS+)? filterSet WS+ '->' WS+ actionSet
          | (tags WS+)? filterSet WS+ '{' (WS+ rule WS+)+ '}'
          ;
          
tags      : '[' TAG (',' TAG)* ']';

filterSet : filterItem (WS+ (binaryOp WS+)? filterItem)*;

filterItem: idFilterItem
          | multiIdFilterItem
          | textFilterItem
          | multiTextFilterItem
          | numFilterItem
          | cardFilterItem
          | predicateModifierFilterItem
          | entityTypeFilterItem
          | applicationFilterItem
          | groupFilterItem
          | negatedFilterItem
          ;

idFilterItem                : idField '==' idValue;

multiIdFilterItem           : mulIdField '==' idValue;

textFilterItem              : txField '==' string;

multiTextFilterItem         : mulTxField '==' string;

numFilterItem               : numField numOp DOUBLE
                            | numField '==' EMPTY;

cardFilterItem              : cardField '==' CARDVALUE
                            | cardField '==' EMPTY;

predicateModifierFilterItem : 'predicate_modifier==Not';

entityTypeFilterItem        : entField '==' string;

applicationFilterItem       : FUNCTION WS* arglist? WS* ')';

groupFilterItem             : '(' WS* filterSet WS* ')';

negatedFilterItem           : '!' WS* filterItem;

idField   : 'issue_tracker_item'
          | 'justification'
          | 'mapping_justification'
          | 'mapping_source'
          | 'object'
          | 'object_source'
          | 'predicate'
          | 'subject'
          | 'subject_source'
          ;

mulIdField: 'author'
          | 'creator'
          | 'curation_rule'
          | 'object_match_field'
          | 'object_preprocessing'
          | 'reviewer'
          | 'subject_match_field'
          | 'subject_preprocessing'
          ;

txField   : 'comment'
          | 'license'
          | 'mapping_provider'
          | 'mapping_tool'
          | 'mapping_tool_version'
          | 'object_category'
          | 'object_label'
          | 'object_source_version'
          | 'other'
          | 'predicate_label'
          | 'similarity_measure'
          | 'subject_category'
          | 'subject_label'
          | 'subject_source_version'
          ;

mulTxField: 'author_label'
          | 'creator_label'
          | 'curation_rule_text'
          | 'reviewer_label'
          | 'see_also'
          ;

cardField : 'mapping_cardinality' | 'cardinality';

entField  : 'object_type'
          | 'subject_type'
          ;

idValue   : CURIE
          | EMPTY
          | '*'
          ;

numField  : 'confidence'
          | 'semantic_similarity_score'
          | 'similarity_score'
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

argument  : nonKeyedArgument
          | keyedArgument
          ;

keyedArgument: KEY nonKeyedArgument;

nonKeyedArgument: string
                | PLACEHOLDER
                | CURIE
                | IRI
                ;

string    : SQ_STRING
          | DQ_STRING
          ;

EMPTY     : '~';

KEY       : '/' [a-z_]+ '=';

FUNCTION  : [a-zA-Z_] [a-zA-Z0-9_]+ '(';

SQ_STRING : '\'' (SQ_ESCAPE|.)*? '\'';

DQ_STRING : '"' (DQ_ESCAPE|.)*? '"';

TAG       : [a-zA-Z0-9-]+;

CARDVALUE : '1:1' | '1:n' | 'n:1' | '1:0' | '0:1' | 'n:n' | '*:n' | '*:1' | 'n:*' | '1:*';

CURIE     : PREFIX [a-zA-Z0-9_*-]+;

PREFIX    : [a-zA-Z0-9_]+ ':';

IRI       : '<' [a-zA-Z0-9_/:#=.?&-]+ '>';

DOUBLE    : [0-9]+ '.' [0-9]+;

PLACEHOLDER: '%{' [a-zA-Z] [a-zA-Z0-9_]* ('|' [a-zA-Z] [a-zA-Z0-9_]*)? '}';

COMMENT   : '#' .*? NL -> skip;

WS        : ' ' | '\t' | NL;

NL        : '\r'? '\n';

fragment
SQ_ESCAPE : '\\\'' | '\\\\';

fragment
DQ_ESCAPE : '\\"' | '\\\\';
