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

tokens {  STORE_CMD;
          INSTALL_CMD;
	      DEPLOY_CMD;
	      UNDEPLOY_CMD;
	      UNINSTALL_CMD;
	      REMOVE_CMD;
	      AUTH_CMD;
	      LIST_CMD;
	      USE_CMD;
	      PARAMETER;
	      PARAM_USERNAME;
	      PARAM_PASSWORD;
	      FILE;
	      PARAM_CONTRIBUTION_NAME;
	      PARAM_DOMAIN_NAME;
	      PARAM_PLAN_NAME;
	      PARAM_PLAN_FILE;
}

@header { package org.fabric3.admin.cli; }
@lexer::header { package org.fabric3.admin.cli; }

@members {

   protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
      super.mismatch(input, ttype, follow);
      // throw new MismatchedTokenException(ttype, input);
   }

   public void recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
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

subcommand	: (store | install | deploy | auth | list | undeploy | uninstall | remove | use) NEWLINE?;

// main commands
store    	    : STORE file WS? param* -> ^(STORE_CMD file param*);
install 	    : INSTALL contribution_name WS? param* -> ^(INSTALL_CMD contribution_name param*);
auth            : AUTH auth_param auth_param -> ^(AUTH_CMD auth_param auth_param);
list            : LIST auth_param* -> ^(LIST_CMD auth_param*);
deploy 		    : DEPLOY contribution_name plan_name?  plan_file? WS? param* -> ^(DEPLOY_CMD contribution_name plan_name? plan_file? param*);
undeploy 	    : UNDEPLOY contribution_name WS? param* -> ^(UNDEPLOY_CMD contribution_name param*);
uninstall 	    : UNINSTALL contribution_name WS? param* -> ^(UNINSTALL_CMD contribution_name param*);
remove          : REMOVE contribution_name WS? param* -> ^(REMOVE_CMD contribution_name param*);
use             : USE domain_name -> ^(USE_CMD) domain_name;
file		    : STRING -> ^(FILE STRING);
param		    : operator STRING WS? -> ^(PARAMETER operator STRING) ;
auth_param	    : auth_operator STRING WS? -> ^(PARAMETER auth_operator STRING) ;
operator        : (username | password | contribution);
auth_operator   : (username | password);
username        : USERNAME -> ^(PARAM_USERNAME);
password        : PASSWORD -> ^(PARAM_PASSWORD);
contribution    : CONTRIBUTION -> ^(PARAM_CONTRIBUTION_NAME);
contribution_name : STRING -> ^(PARAM_CONTRIBUTION_NAME STRING);
domain_name     : STRING -> ^(PARAM_DOMAIN_NAME STRING);
plan_name       : STRING -> ^(PARAM_PLAN_NAME STRING);
plan_file       : PLAN STRING -> ^(PARAM_PLAN_FILE STRING);

STORE 	        : ('store' | 'stor');
INSTALL 	    : ('install' | 'ins');
AUTH 	        : ('authenticate' | 'auth');
LIST     	    : ('list' | 'ls');
DEPLOY		    : ('deploy' | 'dep');
REMOVE          : ('remove' | 'rem');
UNINSTALL 	    : ('uninstall' | 'uins');
UNDEPLOY	    : ('undeploy' | 'udep');
USE     	    : ('use');
USERNAME	    : ('-u' | '-username' | '-USERNAME') ;
PASSWORD	    : ('-p' | '-P'|'-PASSWORD' | '-password');
CONTRIBUTION    : ('-n' | '-N'|'-NAME'| '-name');
PLAN            : ('-plan' | '-PLAN');

STRING	        : ('a'..'z'|'A'..'Z'|'0'..'9'|'.' |'-'|'_' | '/' | ':')+;
NEWLINE    	    : '\r'? '\n';
WS		        : (' '|'\t'|'\n'|'\r')+ {skip();};