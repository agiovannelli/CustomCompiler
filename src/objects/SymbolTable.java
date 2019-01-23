package objects;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import classes.ErrorLogger;

public class SymbolTable
{
	// Public properties.
	public Symbol CURR_SYMBOL;
	public StringBuilder GLOBAL_SCOPE_KEY;
	public StringBuilder SCOPE_KEY;
	
	// Private properties.
	Stack<String> SCOPE_STACK;
	HashMap<String, Symbol> SYMBOL_TABLE;
	ErrorLogger theErrorLogger;
	
	// Constructor.
	public SymbolTable(ErrorLogger aErrorLogger)
	{
		// Set local error logger to passed parameter.
		theErrorLogger = aErrorLogger;
		
		// Set scope key to empty StringBuilder.
		SCOPE_KEY = new StringBuilder();
		
		// Set global scope key to empty StringBuilder.
		GLOBAL_SCOPE_KEY = new StringBuilder();
		
		// Initialize CURR_SYMBOL to null.
		CURR_SYMBOL = new Symbol();
		
		// Initialize SCOPE_STACK to empty stack.
		SCOPE_STACK = new Stack<String>();
		
		// Initialize HashMap on instantiation.
		SYMBOL_TABLE = new HashMap<String, Symbol>();
	}
	
	// Places the given values into SYMBOL_TABLE after instantiating a new Symbol Object.
	public void PutSymbolInTable() throws IOException
	{
		// If the key is unique, put in symbol table. Otherwise, throw exception. 
		if(!SYMBOL_TABLE.containsKey(SCOPE_KEY.toString().trim().toLowerCase()))
			SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase(), 
					new Symbol(CURR_SYMBOL.getType(), CURR_SYMBOL.getLineNumber(), CURR_SYMBOL.getBoundLower(), CURR_SYMBOL.getBoundUpper(),
							CURR_SYMBOL.getParameters(), CURR_SYMBOL.getParameterTypes(), CURR_SYMBOL.getParameterReturnTypes(), CURR_SYMBOL.isGlobal()));
		else
			theErrorLogger.LogSymbolError(SCOPE_KEY.toString().trim());
		
		CURR_SYMBOL.setType(null);
		CURR_SYMBOL.setLineNumber(-1);
		CURR_SYMBOL.setBoundLower(null);
		CURR_SYMBOL.setBoundUpper(null);
		CURR_SYMBOL.setGlobal(false);
	}
	
	// Append substring to the scope key value.
	public void AddToScopeKey(String aScopeAddition)
	{
		// For later entries onto SCOPE_KEY, format accordingly.
		if(SCOPE_KEY.length() != 0)
		{
			SCOPE_KEY.append("." + aScopeAddition.toLowerCase());
		}
		// Unique entry format for first scope written.
		else
		{
			GLOBAL_SCOPE_KEY.append(aScopeAddition.toLowerCase());
			SCOPE_KEY.append(aScopeAddition.toLowerCase());
			AddBuiltInFunctionsToSymbolTable();
		}
	}
	
	// Update the scope for global case. 
	public void UpdateScopeForGlobal()
	{
		SCOPE_STACK.push(SCOPE_KEY.toString().trim());
		SCOPE_KEY.setLength(0);
		SCOPE_KEY.append(GLOBAL_SCOPE_KEY.toString().trim());
		CURR_SYMBOL.setGlobal(true);
	}
	
	// Returns the scope for the global value.
	public void ReturnScopeForGlobal()
	{
		SCOPE_KEY.setLength(0);
		SCOPE_KEY.append(SCOPE_STACK.pop().toString().trim());
	}
	
	// Returns the last string on end of scope key from '.' character.
	public String ReturnEndOfScopeKey()
	{
		return SCOPE_KEY.toString().trim().substring(SCOPE_KEY.lastIndexOf(".") + 1);
	}
	
	// Remove substring after last period in SCOPE_KEY.
	public void RemoveFromScopeKey()
	{
		//SCOPE_KEY.setLength(SCOPE_KEY.lastIndexOf("."));
		SCOPE_KEY = SCOPE_KEY.delete(SCOPE_KEY.lastIndexOf("."), SCOPE_KEY.length());
	}
	
	// Returns a symbol for given key value.
	public Symbol ReturnSymbolValueForKey(String aKey)
	{
		Symbol aReturnSymbol = null;
		
		if(SYMBOL_TABLE.containsKey(aKey.toLowerCase()))
		{
			aReturnSymbol = SYMBOL_TABLE.get(aKey.toLowerCase());
		}
		
		return aReturnSymbol;
	}
	
	// For a procedure symbol, creates symbol objects in table.
	public void CreateSymbolsForProcedureParameters()
	{
		for(int idx = 0; idx < CURR_SYMBOL.Parameters.size(); idx++)
		{
			if(!SYMBOL_TABLE.containsKey(SCOPE_KEY.toString().trim().toLowerCase() + "." + CURR_SYMBOL.Parameters.get(idx)))
				SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + "." + CURR_SYMBOL.Parameters.get(idx), 
						new Symbol(CURR_SYMBOL.ParameterTypes.get(idx), CURR_SYMBOL.getLineNumber(), null, null,
								null, null, Arrays.asList(CURR_SYMBOL.ParameterReturnTypes.get(idx)),false));
		}
	}
	
	// Used for translation purposes.
	public String ReturnScopeKeyForTranslation()
	{
		String aScopeString = SCOPE_KEY.toString().trim();
		String aReturnString = aScopeString.replace(".", "_");
		return aReturnString;
	}
	
	// Adds built-in functions to table. Includes corresponding parameters.
	private void AddBuiltInFunctionsToSymbolTable()
	{
		Symbol aGetBoolSymbol = new Symbol(TokenType.GETBOOL, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.BOOL), Arrays.asList(TokenType.OUT), true);
		Symbol aGetIntegerSymbol = new Symbol(TokenType.GETINTEGER, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.INTEGER), Arrays.asList(TokenType.OUT), true);
		Symbol aGetFloatSymbol = new Symbol(TokenType.GETFLOAT, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.FLOAT), Arrays.asList(TokenType.OUT), true);
		Symbol aGetStringSymbol = new Symbol(TokenType.GETSTRING, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.STRING), Arrays.asList(TokenType.OUT), true);
		Symbol aGetCharSymbol = new Symbol(TokenType.GETCHAR, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.CHAR), Arrays.asList(TokenType.OUT), true);
		
		Symbol aPutBoolSymbol = new Symbol(TokenType.PUTBOOL, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.BOOL), Arrays.asList(TokenType.IN), true);
		Symbol aPutIntegerSymbol = new Symbol(TokenType.PUTINTEGER, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.INTEGER), Arrays.asList(TokenType.IN), true);
		Symbol aPutFloatSymbol = new Symbol(TokenType.PUTFLOAT, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.FLOAT), Arrays.asList(TokenType.IN), true);
		Symbol aPutStringSymbol = new Symbol(TokenType.PUTSTRING, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.STRING), Arrays.asList(TokenType.IN), true);
		Symbol aPutCharSymbol = new Symbol(TokenType.PUTCHAR, 0, null, null, Arrays.asList("val"), Arrays.asList(TokenType.CHAR), Arrays.asList(TokenType.IN), true);
		
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".getbool", aGetBoolSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".getinteger", aGetIntegerSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".getfloat", aGetFloatSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".getstring", aGetStringSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".getchar", aGetCharSymbol);
		
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".putbool", aPutBoolSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".putinteger", aPutIntegerSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".putfloat", aPutFloatSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".putstring", aPutStringSymbol);
		SYMBOL_TABLE.put(SCOPE_KEY.toString().trim().toLowerCase() + ".putchar", aPutCharSymbol);
	}
}
