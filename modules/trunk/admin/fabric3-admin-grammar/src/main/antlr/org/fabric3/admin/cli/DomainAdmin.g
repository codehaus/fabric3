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

@members {
    
   protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
      System.out.println("-------------->");
      super.mismatch(input, ttype, follow);
      // throw new MismatchedTokenException(ttype, input);
   }

   public void recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
      System.out.println("--------------x>");
      super.recoverFromMismatchedSet(input, e, follow);
      // throw e;
   }

}

// Alter code generation so catch-clauses get replace with this action.
@rulecatch {
   catch (RecognitionException e) {
      throw e;
   }
}

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