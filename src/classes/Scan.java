package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import objects.Token;
import objects.TokenType;

// Provides functionality for the lexical analysis of a given file.
public class Scan
{
	private List<Character> IDENTITY_CHARACTERS;
	private List<Character> NUMBER_CHARACTERS;
	private List<Character> STRING_CHARACTERS;
	private List<Character> CHAR_CHARACTERS;
	private List<Character> ANNOYING_CHARACTERS;
	private List<String> RESERVED_WORDS;
	
	public PushbackReader READER;
	public boolean END_REACHED;
	
	private int LINE_NUMBER;
	
	// Scan constructor.
	public Scan(String aFilePath) throws IOException
	{
		// Read file path and prep READER value.
		File theFileFromPath = new File(aFilePath);
		this.ReadSourceFile(theFileFromPath);
		
		// Initialize variables.
		IDENTITY_CHARACTERS = CreateLegalIdentifierCharacterList();
		NUMBER_CHARACTERS = CreateLegalNumberCharacterList();
		STRING_CHARACTERS = CreateLegalStringCharacterList();
		CHAR_CHARACTERS = CreateLegalCharacterCharacterList();
		ANNOYING_CHARACTERS = CreateAnnoyingCharacterList();
		RESERVED_WORDS = CreateReservedWords();
		
		LINE_NUMBER = 1;
		END_REACHED = false;
	}
	
	// Determines the token type of a passed character.
	public Token GetToken() throws IOException
	{
		Token theToken = null;
		
		TokenType theTokenType = null;
		int theCharacterInt=-1;
		StringBuilder theTokenValue = new StringBuilder();
		boolean theTokenIsValid = true;
		
		if ((theCharacterInt = READER.read()) == -1)
		{
			END_REACHED = true;
			return theToken;
		}

		char theChar = (char) theCharacterInt;
		
		while(ANNOYING_CHARACTERS.contains(theChar))
		{
			if(theChar == '/')
			{
				if(CommentHandler(theChar))
				{
					theToken = new Token(TokenType.DIVIDE, "/", LINE_NUMBER);
					return theToken;
				}
			}
			else if(theChar == '\n')
				LINE_NUMBER++;

			return GetToken();
		}
		
		// Determine token based on characters.
		switch(theChar)
		{
			// Identifier case.
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': 
			case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': 
			case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
				theTokenType = TokenType.IDENTITY;
				theTokenValue.append(theChar);
				while (theTokenType == TokenType.IDENTITY)
				{
					theChar = (char) READER.read();
					if (!IDENTITY_CHARACTERS.contains(theChar))
					{
						if (RESERVED_WORDS.contains(theTokenValue.toString().toLowerCase()))
							theTokenType = GetReservedIdentifier(theTokenValue.toString().toLowerCase());
						theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
						READER.unread((int)theChar);
						break;
					}
					else
						theTokenValue.append(theChar);
				}
				break;
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				theTokenType = TokenType.INTEGER;
				theTokenValue.append(theChar);
				boolean thePeriodCount = false;
				while (theTokenType == TokenType.INTEGER || theTokenType == TokenType.FLOAT)
				{
					theChar = (char) READER.read();
					if (!NUMBER_CHARACTERS.contains(theChar) && theChar != '.')
					{
						break;
					}
					else if (theChar == '.' && !thePeriodCount)
					{
						thePeriodCount = true;
						theTokenType = TokenType.FLOAT;
					}
					else if (theChar == '.' && thePeriodCount)
					{
						theTokenIsValid = false;
					}
					
					theTokenValue.append(theChar);
				}
				
				if(!theTokenIsValid)
					theTokenType = TokenType.ERROR;
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				READER.unread((int)theChar);
				break;
			case '\'':
				theTokenType = TokenType.CHAR;
				theTokenValue.append(theChar);
				while (theTokenType == TokenType.CHAR)
				{
					theChar = (char) READER.read();
					if (!CHAR_CHARACTERS.contains(theChar) && theChar != '\'')
					{
						theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
						READER.unread((int)theChar);
						break;
					}
					else if (theChar == '\'')
					{
						theTokenValue.append(theChar);
						theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
						break;
					}
					else
						theTokenValue.append(theChar);
				}
				break;
			case '"':
				theTokenType = TokenType.STRING;
				theTokenValue.append(theChar);
				while (theTokenType == TokenType.STRING)
				{
					theChar = (char) READER.read();
					if (!STRING_CHARACTERS.contains(theChar) && theChar != '"')
					{
						theTokenType = TokenType.ERROR;
						boolean buildError = true;
						while(buildError)
						{
							if (theChar == '"')
							{
								theTokenValue.append(theChar);
								buildError = false;
								break;
							}
							
							if (theChar == '\n')
							{
								LINE_NUMBER++;
								buildError = false;
								break;
							}
							theTokenValue.append(theChar);
							theChar = (char) READER.read();
						}
						theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
						break;
					}
					else if (theChar == '"')
					{
						theTokenValue.append(theChar);
						theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
						break;
					}
					else
						theTokenValue.append(theChar);
				}
				break;
			case '*':
				theTokenValue.append(theChar);
				theChar = (char) READER.read();
				if (theChar == '/')
				{
					theTokenType = TokenType.END_BLOCK_COMMENT;
					theTokenIsValid = false;
					theTokenValue.append(theChar);
				}
				else
				{
					theTokenType = TokenType.MULTIPLY;
					READER.unread((int)theChar);
				}
				if(!theTokenIsValid)
					theTokenType = TokenType.ERROR;
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case ':':
				theChar = (char) READER.read();
				if (theChar == '=')
				{
					theTokenType = TokenType.ASSIGN;
					theTokenValue.append(":=");
				}
				else
				{
					READER.unread((int) theChar);
					theTokenValue.append(':');
					theTokenType = TokenType.COLON;
				}
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '!':
				theChar = (char) READER.read();
				if (theChar == '=')
				{
					theTokenType = TokenType.NOT_EQUIVALENT;
					theTokenValue.append("!=");
				}
				else
				{
					READER.unread((int) theChar);
					theTokenValue.append('!');
					theTokenType = TokenType.ERROR;
					theTokenIsValid = false;
				}
				if(!theTokenIsValid)
					theTokenType = TokenType.ERROR;
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '=':
				theChar = (char) READER.read();
				if (theChar == '=')
				{
					theTokenType = TokenType.EQUIVALENT;
					theTokenValue.append("==");
				}
				else
				{
					READER.unread((int) theChar);
					theTokenValue.append('=');
					theTokenType = TokenType.ERROR;
					theTokenIsValid = false;
				}
				if(!theTokenIsValid)
					theTokenType = TokenType.ERROR;
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '<':
				theChar = (char) READER.read();
				if (theChar == '=')
				{
					theTokenType = TokenType.LESS_THAN_EQ;
					theTokenValue.append("<=");
				}
				else
				{
					READER.unread((int) theChar);
					theTokenValue.append('<');
					theTokenType = TokenType.LESS_THAN;
				}
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '>':
				theChar = (char) READER.read();
				if (theChar == '=')
				{
					theTokenType = TokenType.GREATER_THAN_EQ;
					theTokenValue.append(">=");
				}
				else
				{
					READER.unread((int) theChar);
					theTokenValue.append('>');
					theTokenType = TokenType.GREATER_THAN;
				}
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '+':
				theTokenType = TokenType.PLUS;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;	
			case '-':
				theTokenType = TokenType.MINUS;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case ',':
				theTokenType = TokenType.COMMA;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '&':
				theTokenType = TokenType.AND;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '|':
				theTokenType = TokenType.OR;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case ';':
				theTokenType = TokenType.SEMICOLON;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '[':
				theTokenType = TokenType.LEFT_BRACKET;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case ']':
				theTokenType = TokenType.RIGHT_BRACKET;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case '(':
				theTokenType = TokenType.LEFT_PARENTHESIS;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
			case ')':
				theTokenType = TokenType.RIGHT_PARENTHESIS;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break; 
			case '.':
				theTokenType = TokenType.PERIOD;
				theTokenValue.append(theChar);
				theToken = new Token(theTokenType, theTokenValue.toString().trim(), LINE_NUMBER);
				break;
		}
		
		return theToken;
	}
	
	// Ignores comments till complete. If not comment, return true to indicate divisor. 
	private boolean CommentHandler(char aChar) throws IOException
	{
		int theBlockCount = 0;
		boolean isDivider = false;
		aChar = (char) READER.read();
		int aCharInt;
		
		switch(aChar)
		{
			case '/':
				while ((aChar = (char) READER.read()) != '\n')
				{
					// Place holder...
				}
				LINE_NUMBER++;
				break;
			case '*':
				theBlockCount++;
				while ((aCharInt = READER.read()) != -1)
				{
					aChar = (char)aCharInt;
					switch (aChar)
					{
						case '/':
							if ((aChar = (char)READER.read()) == '*')
								theBlockCount++;
							break;
						case '*':
							if ((aChar = (char)READER.read()) == '/')
								theBlockCount--;
							break;
						case '\n':
							break;
					}
					if (theBlockCount == 0)
						break;
				}
				break;
			default:
				isDivider = true;
				READER.unread((int)aChar);
				break;
		}
		return isDivider;
	}
	
	// Read provided file and prepare READER global. 
	private void ReadSourceFile(File theFileToRead) throws IOException
	{
		// Initialize Input Stream.
		InputStream theInputStream = null;
		
		try
		{
			theInputStream = new FileInputStream(theFileToRead);
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Failed to find file. Exception code: " + e.toString());
			return;
		}
		
		// Generate buffer for read efficiency.
        Reader theReader = new InputStreamReader(theInputStream);
        Reader theBuffer = new BufferedReader(theReader);
        READER = new PushbackReader(theBuffer);
	}
	
	// Updates the TokenType value for a reserved word. 
	private TokenType GetReservedIdentifier(String aReservedWord)
	{
		// Initialize return variable.
		TokenType theReturnType = TokenType.IDENTITY;
		
		switch(aReservedWord)
		{
			case "program":
				theReturnType = TokenType.PROGRAM;
				break;
			case "is":
				theReturnType = TokenType.IS;
				break;
			case "end":
				theReturnType = TokenType.END;
				break;
			case "begin":
				theReturnType = TokenType.BEGIN;
				break;
			case "global":
				theReturnType = TokenType.GLOBAL;
				break;
			case "procedure":
				theReturnType = TokenType.PROCEDURE;
				break;
			case "if":
				theReturnType = TokenType.IF;
				break;
			case "then":
				theReturnType = TokenType.THEN;
				break;
			case "else":
				theReturnType = TokenType.ELSE;
				break;
			case "for":
				theReturnType = TokenType.FOR;
				break;
			case "in":
				theReturnType = TokenType.IN;
				break;
			case "out":
				theReturnType = TokenType.OUT;
				break;
			case "inout":
				theReturnType = TokenType.INOUT;
				break;
			case "true":
				theReturnType = TokenType.TRUE;
				break;
			case "false":
				theReturnType = TokenType.FALSE;
				break;
			case "not":
				theReturnType = TokenType.NOT;
				break;
			case "integer":
				theReturnType = TokenType.INTEGER;
				break;
			case "float":
				theReturnType = TokenType.FLOAT;
				break;
			case "bool":
				theReturnType = TokenType.BOOL;
				break;
			case "char":
				theReturnType = TokenType.CHAR;
				break;
			case "return":
				theReturnType = TokenType.RETURN;
				break;
			case "string":
				theReturnType = TokenType.STRING;
				break;
			case "putbool":
				theReturnType = TokenType.PUTBOOL;
				break;
			case "putinteger":
				theReturnType = TokenType.PUTINTEGER;
				break;
			case "putfloat":
				theReturnType = TokenType.PUTFLOAT;
				break;
			case "putstring":
				theReturnType = TokenType.PUTSTRING;
				break;
			case "putchar":
				theReturnType = TokenType.PUTCHAR;
				break;
			case "getbool":
				theReturnType = TokenType.GETBOOL;
				break;
			case "getinteger":
				theReturnType = TokenType.GETINTEGER;
				break;
			case "getfloat":
				theReturnType = TokenType.GETFLOAT;
				break;
			case "getstring":
				theReturnType = TokenType.GETSTRING;
				break;
			case "getchar":
				theReturnType = TokenType.GETCHAR;
				break;
		}
			
			return theReturnType;
	}
	
	// Creates the list of all legal identifier characters.
	private List<Character> CreateLegalIdentifierCharacterList()
	{
		return Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
				'0','1','2','3','4','5','6','7','8','9','_');
	}
	
	// Creates the list of all legal number characters.
	private List<Character> CreateLegalNumberCharacterList()
	{
		return Arrays.asList('0','1','2','3','4','5','6','7','8','9','_');
	}
	
	// Creates the list of all legal string characters.
	private List<Character> CreateLegalStringCharacterList()
	{
		return Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
				'0','1','2','3','4','5','6','7','8','9','_',' ',';',':','.','\'',',');
	}
	
	// Creates the list of all legal char characters.
	private List<Character> CreateLegalCharacterCharacterList()
	{
		return Arrays.asList('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
				'0','1','2','3','4','5','6','7','8','9','_',' ',';',':','.','\"',',');
	}
	
	// Creates list of annoying characters.
	private List<Character> CreateAnnoyingCharacterList()
	{
		return Arrays.asList('\n', '\r', ' ', '\t', '/');
	}
	
	// Creates the list of reserved words.
	private List<String> CreateReservedWords()
	{
		return Arrays.asList("program", "is", "begin", "end", "global", "procedure",
				"in","out","inout","integer","float","bool","char","string","if",
				"else","then","true","false","not","return","for", 
				"putbool","putinteger","putfloat","putstring","putchar",
				"getbool","getinteger","getfloat","getstring","getchar");
	}
}
