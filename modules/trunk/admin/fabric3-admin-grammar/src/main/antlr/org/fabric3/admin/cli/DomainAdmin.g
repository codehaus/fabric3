/**
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * The Antlr grammar for the domain admin interpreter.
 */
grammar DomainAdmin;

options {
output=AST;
ASTLabelType=CommonTree;
}

tokens {  INSTALL_CMD;
	      DEPLOY;
	      PARAMETER;
	      PARAM_USERNAME;
	      PARAM_PASSWORD;
	      FILE;
	      CONTRIBUTION_NAME;
}

@header { package org.fabric3.admin.cli; }
@lexer::header { package org.fabric3.admin.cli; }

command		: (subcommand)+ EOF;

subcommand	: (install | deploy) NEWLINE?;
install 	: INSTALL file contribution? WS? param+ -> ^(INSTALL_CMD file contribution? param+);
uninstall 	: UNINSTALL contribution? WS? param+;
deploy 		: DEPLOY contribution WS? param+ -> ^(DEPLOY contribution? param+);
undeploy 	: UNDEPLOY contribution WS? param+;
file		: STRING -> ^(FILE STRING);
contribution: STRING -> ^(CONTRIBUTION_NAME STRING);
param		: operator STRING WS? -> ^(PARAMETER operator STRING) ;
operator    : (username | password);

username : USERNAME -> ^(PARAM_USERNAME);
password : PASSWORD -> ^(PARAM_PASSWORD);


INSTALL 	: ('install' | 'ins');
UNINSTALL 	: ('uninstall' | 'uins');
DEPLOY		: ('deploy' | 'dep');
UNDEPLOY	: ('undeploy' | 'udep');
USERNAME	: ('-u' | '-username' | '-USERNAME') ;
PASSWORD	: ('-p' | '-P'|'-PASSWORD');

STRING	: ('a'..'z'|'A'..'Z'|'0'..'9'|'.')+;
NEWLINE    	: '\r'? '\n';
WS		: (' '|'\t'|'\n'|'\r')+ {skip();};