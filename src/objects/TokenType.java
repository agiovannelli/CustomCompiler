package objects;

// Enum for the types of tokens possible.
public enum TokenType
{
	// Reserved tokens.
	PROGRAM,
	IS,
	BEGIN,
	END,
	GLOBAL,
	PROCEDURE,
	IN,
	OUT,
	INOUT,
	INTEGER,
	FLOAT,
	BOOL,
	IF,
	ELSE,
	THEN,
	TRUE,
	FALSE,
	NOT,
	RETURN,
	FOR,
	
	// Built-in functions.
	GETBOOL,
	GETINTEGER,
	GETFLOAT,
	GETSTRING,
	GETCHAR,
	PUTBOOL,
	PUTINTEGER,
	PUTFLOAT,
	PUTSTRING,
	PUTCHAR,
	
	// Bitwise tokens.
	AND,
	OR,
	
	// Operator tokens.
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	
	// Relational tokens.
	LESS_THAN,
	LESS_THAN_EQ,
	GREATER_THAN,
	GREATER_THAN_EQ,
	EQUIVALENT,
	NOT_EQUIVALENT,
	ASSIGN,
	
	// Separator tokens.
	COMMA,
	SEMICOLON,
	COLON,
	PERIOD,
	SLASHY_BOI,
	
	// Grouping tokens.
	LEFT_BRACKET,
	RIGHT_BRACKET,
	LEFT_PARENTHESIS,
	RIGHT_PARENTHESIS,
	
	// Comment tokens.
	COMMENT,
	START_BLOCK_COMMENT,
	END_BLOCK_COMMENT,
	
	// General tokens.
	ERROR,
	NUMBER,
	IDENTITY,
	STRING,
	CHAR;
}