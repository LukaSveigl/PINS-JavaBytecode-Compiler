package pins.data.symbol;

public enum Token {

	EOF,

	LPARENTHESIS, RPARENTHESIS, // ()

	LBRACE, RBRACE,	// {}

	LBRACKET, RBRACKET, // []

	COMMA, COLON,  SEMIC, // , : ;

	AND, OR, NOT, // & | !

	EQU, NEQ, LTH, GTH, LEQ, GEQ, // == != < > <= >=

	MUL, DIV, MOD, ADD, SUB, PTR, ASSIGN, // * / % + - ^ =

	// Keywords
	CHAR, DEL, DO, ELSE, END, FUN, IF, INT, NEW, THEN, TYP, VAR , VOID, WHERE, WHILE,

	// Constants
	CONSTVOID, CONSTCHAR, CONSTINT, CONSTPTR,

	IDENTIFIER
	
}
