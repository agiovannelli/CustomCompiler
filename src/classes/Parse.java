package classes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import objects.Symbol;
import objects.SymbolTable;
import objects.Token;
import objects.TokenType;

public class Parse
{
	// Private properties.
	private Scan theScanner;
	private Token theCurrentToken;
	private Token theNextToken;
	private ErrorLogger theLogger;
	private SymbolTable theSymbolTable;
	
	private Symbol theCurrentSymbol;
	private Translator theTranslator;
	
	// Constructor.
	public Parse(Scan aScanner, ErrorLogger aErrorLogger, SymbolTable aSymbolTable) throws Exception
	{
		// Make sure scanner is not null.
		if(aScanner == null)
		{
			System.out.println("Scanner provided to parser is null. Exiting.");
			throw new Exception();
		}
		
		// Initialize private properties.
		theLogger = aErrorLogger;
		theScanner = aScanner;
		theSymbolTable = aSymbolTable;
		theCurrentToken = theScanner.GetToken();
		theNextToken = theScanner.GetToken();
		
		theCurrentSymbol = null;
		theTranslator = null;
	}
	
	// Calls functionality to test validity of tokens obtained through scanner method calls.
	public void ParseTokens() throws IOException
	{	
		boolean result = isValidProgram();
		System.out.println(result);
	}
	
	// Update the global tokens to allow "look ahead" functionality.
	private void updateToken() throws IOException
	{
		theCurrentToken = theNextToken;
		theNextToken = theScanner.GetToken();
	}
	
	// Determine if valid program.
	private boolean isValidProgram() throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
		
		// If the current token is program type, confirm if valid program header and body exist.
		if(theCurrentToken.TokenType == TokenType.PROGRAM)
		{
			// Set Symbol Type for table. 
			theSymbolTable.CURR_SYMBOL.setType(theCurrentToken.TokenType);
			
			// Update current token before checking if program header.
			updateToken();
			if(isProgramHeader())
			{
				System.out.println("Program Header!");
				// Update current token before checking if program body.
				updateToken();
				if(isProgramBody())
				{
					// Update current token before checking if end of program.
					if(theNextToken != null)
						updateToken();
					if(theCurrentToken.TokenType == TokenType.PERIOD)
					{
						updateToken();
						isValid = true;
						theTranslator.WriteCodeToFile();
					}
					else
						theLogger.LogParseError(theCurrentToken);
				}
			}
		}
		else
			theLogger.LogParseError(theCurrentToken);
		
		return isValid;
	}
	
	// Determine if valid program header.
	private boolean isProgramHeader() throws IOException
	{
		boolean isValid = false;
		
		// Check if current token matches given syntax.
		if(theCurrentToken.TokenType == TokenType.IDENTITY)
		{
			// Update scope key.
			theSymbolTable.AddToScopeKey(theCurrentToken.TokenValue);
			theTranslator = new Translator(theCurrentToken.TokenValue);
			
			updateToken();
			if(theCurrentToken.TokenType == TokenType.IS)
			{
				// Update current symbol line number to be end of header. Header of program is position 0 (always).
				theSymbolTable.CURR_SYMBOL.setLineNumber(theCurrentToken.TokenLineNumber);
				
				// Put symbol in table.
				theTranslator.AddHeaderStrings("define i32 @main() {");
				theSymbolTable.PutSymbolInTable();
				isValid = true;
			}
			else
				theLogger.LogParseError(theCurrentToken);
		}
		else
			theLogger.LogParseError(theCurrentToken);
		return isValid;
	}

	// Determine if valid program body.
	private boolean isProgramBody() throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
			
		// Iterate through declarations.
		while(isDeclaration())
		{
			updateToken();
			if(theCurrentToken.TokenType == TokenType.SEMICOLON)
			{
				System.out.println("Declaration!");
				updateToken();
			}
			else
			{
				theLogger.LogParseError(theCurrentToken);
				while(theCurrentToken.TokenType != TokenType.SEMICOLON)
				{
					updateToken();
				}
				updateToken();
			}
		}
		
		if(theCurrentToken.TokenType == TokenType.BEGIN)
		{	
			System.out.println("Begin!");
			updateToken();
			
			while(isStatement())
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.SEMICOLON)
				{
					System.out.println("Statement!");
					updateToken();
				}
				else
				{
					theLogger.LogParseError(theCurrentToken);
					while(theCurrentToken.TokenType != TokenType.SEMICOLON)
					{
						updateToken();
					}
					updateToken();
				}
			}
			
			if(theCurrentToken == null)
			{
				theLogger.LogScanError();
				return isValid;
			}
			
			if(theCurrentToken.TokenType == TokenType.END)
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.PROGRAM)
				{
					System.out.println("End program!");
					isValid = true;
				}
				else
					theLogger.LogParseError(theCurrentToken);
			}
			else
				theLogger.LogParseError(theCurrentToken);
		}
		else
			theLogger.LogParseError(theCurrentToken);
		
		return isValid;
	}
	
	// Determine if type of declaration.
	private boolean isDeclaration() throws IOException
	{	
		return isGlobalDeclaration();
	}
	
	// Determine if declaration is of global scope.
	private boolean isGlobalDeclaration() throws IOException
	{
		boolean isGlobal = false;
		
		if(theCurrentToken.TokenType == TokenType.GLOBAL)
		{
			isGlobal = true;
			updateToken();
			System.out.println("Global!");
		}
		
		// Determine declaration type.
		if(isProcedureDeclaration(isGlobal))
			return true;
		else if(isVariableDeclaration(isGlobal))
			return true;
		else
			return false;
	}
	
	// Determine if type of variable declaration.
	private boolean isVariableDeclaration(boolean aGlobal) throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
		
		if(aGlobal)
		{
			theSymbolTable.UpdateScopeForGlobal();
		}
		
		if(isTypeMark())
		{
			theSymbolTable.CURR_SYMBOL.setType(theCurrentToken.TokenType);
			
			updateToken();
			if(theCurrentToken.TokenType == TokenType.IDENTITY)
			{
				theSymbolTable.AddToScopeKey(theCurrentToken.TokenValue);
				theSymbolTable.CURR_SYMBOL.setLineNumber(theCurrentToken.TokenLineNumber);
				
				try
				{
					if(theNextToken.TokenType == TokenType.LEFT_BRACKET)
					{
						updateToken();
						if(isBoundStatement())
						{
							String aDeclaration = 
									theTranslator.VariableDeclarationBuilder(theSymbolTable.ReturnScopeKeyForTranslation(),
											theSymbolTable.CURR_SYMBOL);
							theSymbolTable.PutSymbolInTable();
							
							if(aGlobal)
							{
								theTranslator.theGlobalDeclarationQueue.add(aDeclaration);
								theSymbolTable.ReturnScopeForGlobal();
							}
							else
							{
								theTranslator.theCurrentBody.add(aDeclaration);
								theSymbolTable.RemoveFromScopeKey();
							}
							isValid = true;
						}
					}
					else
					{
						String aDeclaration = 
								theTranslator.VariableDeclarationBuilder(theSymbolTable.ReturnScopeKeyForTranslation(), 
										theSymbolTable.CURR_SYMBOL);
						theSymbolTable.PutSymbolInTable();	
						
						if(aGlobal)
						{
							theTranslator.theGlobalDeclarationQueue.add(aDeclaration);
							theSymbolTable.ReturnScopeForGlobal();
						}
						else
						{
							theTranslator.theCurrentBody.add(aDeclaration);
							theSymbolTable.RemoveFromScopeKey();
						}
						
						isValid = true;
					}
				}
				catch(Exception e)
				{
					theLogger.LogScanError(theCurrentToken);
					updateToken();
					while(theNextToken != null && theNextToken.TokenType != TokenType.SEMICOLON)
						updateToken();
					isValid = true;
				}
			}
		}
		// Invalid type provided.
		else if (theCurrentToken.TokenType == TokenType.IDENTITY && theNextToken.TokenType == TokenType.IDENTITY)
		{
			theLogger.LogParseError(theCurrentToken);
			while(theNextToken.TokenType != TokenType.SEMICOLON)
				updateToken();
			isValid = true;
		}
		
		return isValid;
	}
	
	// Determine if type of procedure declaration.
	private boolean isProcedureDeclaration(boolean aGlobal) throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
		
		if(theCurrentToken.TokenType == TokenType.PROCEDURE)
		{
			// If we are in the global scope, we must update the scope and push old scope to stack.
			if(aGlobal)
			{
				theSymbolTable.UpdateScopeForGlobal();
			}
			
			theSymbolTable.CURR_SYMBOL.setType(theCurrentToken.TokenType);
			
			updateToken();
			if(isProcedureHeader())
			{
				updateToken();
				if(isProcedureBody())
				{
					if(aGlobal)
						theSymbolTable.ReturnScopeForGlobal();
					else
						theSymbolTable.RemoveFromScopeKey();
					
					isValid = true;
					System.out.println("Procedure Declaration!");
				}
			}
		}
		
		return isValid;
	}
	
	// Determine if type of procedure header.
	private boolean isProcedureHeader() throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
		Symbol aSymbol = new Symbol();
		
		if(theCurrentToken.TokenType == TokenType.IDENTITY)
		{
			theSymbolTable.AddToScopeKey(theCurrentToken.TokenValue);
			theSymbolTable.CURR_SYMBOL.setLineNumber(theCurrentToken.TokenLineNumber);
			
			updateToken();
			if(theCurrentToken.TokenType == TokenType.LEFT_PARENTHESIS)
			{
				if(theNextToken.TokenType != TokenType.RIGHT_PARENTHESIS)
				{
					updateToken();
					if(isParameterList())
					{
						updateToken();
						if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
						{	
							theSymbolTable.CreateSymbolsForProcedureParameters();
							aSymbol = theSymbolTable.CURR_SYMBOL;
							theSymbolTable.PutSymbolInTable();
							isValid = true;
						}
					}
				}
				else
				{
					updateToken();
					isValid = true;
				}
				
				// TODO: Revisit.
				theTranslator.HoldIncompleteData();
				theTranslator.ProcedureDeclarationBuilder(theSymbolTable.ReturnScopeKeyForTranslation(), aSymbol);
				// TODO: add parameters to proc body.
			}
		}
		
		return isValid;
	}
	
	// Determine if type of parameter list.
	private boolean isParameterList() throws IOException
	{
		boolean isValid = false;
		
		if(isParameter())
		{
			if(theNextToken.TokenType == TokenType.COMMA)
			{
				updateToken();
				updateToken();
				
				if(isParameterList())
				{
					System.out.println("Parameter list!");
					isValid = true;
				}
			}
			else
			{
				System.out.println("Parameter list!");
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Determine if type of parameter.
	private boolean isParameter() throws IOException
	{
		boolean isValid = false;
		
		if(isParameterDeclaration())
		{
			updateToken();
			if(isParameterMark())
			{
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Check if valid parameter declaration.
	private boolean isParameterDeclaration() throws IOException
	{
		boolean isValid = false;
		
		if(isTypeMark()) 
		{
			theSymbolTable.CURR_SYMBOL.addParameterTypes(theCurrentToken.TokenType);
			updateToken();
			
			if(theCurrentToken.TokenType == TokenType.IDENTITY)
			{
				theSymbolTable.CURR_SYMBOL.addParameters(theCurrentToken.TokenValue);
			
				if(theNextToken.TokenType == TokenType.LEFT_BRACKET)
				{
					updateToken();
					if(isBoundStatement())
					{
						isValid = true;
					}
				}
				else
				{	
					isValid = true;
				}
			}
		}
		
		return isValid;
	}
	
	// Determine if valid parameter mark.
	private boolean isParameterMark() throws IOException
	{
		boolean isValid = false;
		
		switch(theCurrentToken.TokenType)
		{
			case INOUT: case IN: case OUT:
				theSymbolTable.CURR_SYMBOL.addParameterReturnTypes(theCurrentToken.TokenType);
				isValid = true;
				break;
			default:
				isValid = false;
				theLogger.LogParseError(theCurrentToken);
				break;
		}
		
		return isValid;
	}
	
	// Determine if procedure body.
	private boolean isProcedureBody() throws IOException
	{
		boolean isValid = false;
		
		// Iterate through declarations.
		while(isDeclaration())
		{
			updateToken();
			if(theCurrentToken.TokenType == TokenType.SEMICOLON)
			{
				System.out.println("Declaration!");
				updateToken();
			}
			else
			{
				theLogger.LogParseError(theCurrentToken);
				while(theCurrentToken.TokenType != TokenType.SEMICOLON)
				{
					updateToken();
				}
				updateToken();
			}
		}
		
		if(theCurrentToken.TokenType == TokenType.BEGIN)
		{
			System.out.println("Begin!");
			updateToken();
			
			while(isStatement())
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.SEMICOLON)
				{
					System.out.println("Statement!");
					updateToken();
				}
				else
				{
					theLogger.LogParseError(theCurrentToken);
					while(theCurrentToken.TokenType != TokenType.SEMICOLON)
					{
						updateToken();
					}
					updateToken();
				}
			}
			
			if(theCurrentToken == null)
			{
				theLogger.LogScanError();
				return isValid;
			}
			
			if(theCurrentToken.TokenType == TokenType.END)
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.PROCEDURE)
				{
					// TODO: Revisit.
					theTranslator.ReturnToIncompleteData();
					
					System.out.println("End procedure!");
					isValid = true;
				}
				else
					theLogger.LogParseError(theCurrentToken);
			}
			else
				theLogger.LogParseError(theCurrentToken);
		}
		
		return isValid;
	}
	
	// Determine if type of statement.
	private boolean isStatement() throws IOException
	{
		boolean isValid = false;
		
		if(theCurrentToken == null)
			return isValid;
		
		// Check if return_statement.
		if(isReturnStatement())
		{
			isValid = true;
		}
		else if(isBuiltInProcedureCall())
		{
			isValid = true;
		}
		// Check if either procedure_call or assignment_statement.
		else if(theCurrentToken.TokenType == TokenType.IDENTITY)
		{
			theCurrentSymbol = theSymbolTable.ReturnSymbolValueForKey((theSymbolTable.SCOPE_KEY + "." + theCurrentToken.TokenValue).toString().trim());
			
			if(theCurrentSymbol == null)
				theCurrentSymbol = theSymbolTable.ReturnSymbolValueForKey((theSymbolTable.GLOBAL_SCOPE_KEY + "." + theCurrentToken.TokenValue).toString().trim());
			
			updateToken();
			
			// Check if assignment_statement.
			if(isDestination())
			{
				if(theCurrentToken.TokenType == TokenType.ASSIGN)
				{
					updateToken();
					if(isExpression())
					{
						isValid = true;
					}
				}
			}
			// Check if procedure_call.
			else if(theCurrentToken.TokenType == TokenType.LEFT_PARENTHESIS)
			{
				updateToken();
				if(isProcedureCall())
				{
					isValid = true;
				}
			}
			
			theCurrentSymbol = null;
		}
		// Check if if_statement.
		else if(isIfStatement())
		{
			isValid = true;
		}
		// Check if for_statement.
		else if(isForStatement())
		{
			isValid = true;
		}
		
		// Error handling.
		if(theCurrentToken.TokenType == TokenType.ERROR)
		{
			//theLogger.LogScanError(theCurrentToken);
			while(theNextToken.TokenType != TokenType.SEMICOLON)
			{
				updateToken();
			}
			isValid = true;
		}
		
		return isValid;
	}
	
	// Determine if type of destination.
	private boolean isDestination() throws IOException
	{
		boolean isValid = false;
		
		if(theCurrentToken.TokenType != TokenType.LEFT_BRACKET && theCurrentToken.TokenType != TokenType.LEFT_PARENTHESIS)
		{
			isValid = true;
		}
		else if(theCurrentToken.TokenType == TokenType.LEFT_BRACKET)
		{
			updateToken();
			if(isExpression())
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.RIGHT_BRACKET)
				{
					updateToken();
					isValid = true;
				}
			}
		}
		
		return isValid;
	}
	
	// Determine if type of procedure call.
	private boolean isProcedureCall() throws IOException
	{
		boolean isValid = false;
		
		if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
		{
			isValid = true;
		}
		else
		{
			if(isArgumentList(0, theCurrentSymbol.getParameterTypes(), theCurrentSymbol.getParameterReturnTypes()))
			{
				if(theCurrentToken.TokenType != TokenType.RIGHT_PARENTHESIS)
					updateToken();
				if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
				{
					isValid = true;
				}
			}
		}
		
		return isValid;
	}
	
	private boolean isBuiltInProcedureCall() throws IOException
	{
		boolean isValid = false;
		
		if(isBuiltInProcedureTokenType())
		{
			try
			{
				theCurrentSymbol = theSymbolTable.ReturnSymbolValueForKey((theSymbolTable.GLOBAL_SCOPE_KEY + "." + theCurrentToken.TokenValue).toString().trim());
				updateToken();
				
				if(theCurrentToken.TokenType == TokenType.LEFT_PARENTHESIS)
				{
					updateToken();
						if(isArgumentList(0, theCurrentSymbol.getParameterTypes(), theCurrentSymbol.getParameterReturnTypes()))
						{
							if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
							{
								isValid = true;
							}
							else if(theNextToken.TokenType == TokenType.RIGHT_PARENTHESIS)
							{
								isValid = true;
								updateToken();
							}
						}
					}
				}
			catch(Exception e)
			{
				theLogger.LogParseError(theCurrentToken);
			}
			theCurrentSymbol = null;
		}
		
		return isValid;
	}
	
	// Determine if built-in function.
	private boolean isBuiltInProcedureTokenType()
	{
		switch(theCurrentToken.TokenType)
		{
			case GETBOOL: case GETFLOAT: case GETINTEGER: case GETSTRING: case GETCHAR:
			case PUTBOOL: case PUTFLOAT: case PUTINTEGER: case PUTSTRING: case PUTCHAR:
				return true;
			default:
				return false;
		}
}
	
	// Determine if type of argument list.
	private boolean isArgumentList(int idx, List<TokenType> aArgumentTypeList, List<TokenType> aArgumentReturnTypeList) throws IOException
	{
		boolean isValid = false;
		
		if(isExpression())
		{
			// TYPE CHECKING!
			Symbol aSymbol = null;
			
			if(theCurrentToken.TokenType == TokenType.RIGHT_BRACKET)
			{
				updateToken();
				isValid = true;
			}
			else if(theCurrentToken.TokenType == TokenType.IDENTITY)
			{
				aSymbol = theSymbolTable.ReturnSymbolValueForKey((theSymbolTable.SCOPE_KEY + "." + theCurrentToken.TokenValue).toString().trim());
				
				// If variable is null, check the global scope.
				if(aSymbol == null)
					aSymbol = theSymbolTable.ReturnSymbolValueForKey((theSymbolTable.GLOBAL_SCOPE_KEY + "." + theCurrentToken.TokenValue).toString().trim());
				
				if(aArgumentTypeList != null && aSymbol != null && aArgumentTypeList.get(idx) == aSymbol.getType())
				{
					System.out.println("Argument types match.");
					isValid = true;
				}
				else
				{
					theLogger.LogInvalidProcedureParameter(theCurrentToken.TokenValue);
				}
			}
			else if(aArgumentReturnTypeList.get(idx) == TokenType.IN)
			{
				if(aArgumentTypeList.get(idx) == theCurrentToken.TokenType)
				{
					System.out.println("Raw data types match.");
					isValid = true;
				}
				else
				{
					theLogger.LogInvalidProcedureParameter(theCurrentToken.TokenValue);
				}
			}
		}
		
		if(theNextToken.TokenType == TokenType.COMMA)
		{
			updateToken();
			updateToken();
			if(isArgumentList(idx + 1, aArgumentTypeList, aArgumentReturnTypeList))
			{
				isValid = true;
			}
		}
		else if(theNextToken.TokenType != TokenType.COMMA)
		{
			isValid = true;
		}
		
		return isValid;
	}
	
	// Determine if type of return statement.
	private boolean isReturnStatement()
	{
		boolean isValid = false;
		
		if(theCurrentToken.TokenType == TokenType.RETURN)
		{
			isValid = true;
			System.out.println("Return!");
		}	
		
		return isValid;
	}
	
	// Determine if type of if statement.
	private boolean isIfStatement() throws IOException
	{
		boolean isValid = false;
		
		if(theCurrentToken.TokenType == TokenType.IF)
		{
			theCurrentSymbol = new Symbol(TokenType.IF, theCurrentToken.TokenLineNumber, null, null, null, null, null, false);
			
			updateToken();
			if(theCurrentToken.TokenType == TokenType.LEFT_PARENTHESIS)
			{
				updateToken();
				if(isExpression())
				{
					updateToken();
					if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
					{
						updateToken();
						if(theCurrentToken.TokenType == TokenType.THEN)
						{
							updateToken();
							if(isStatement())
							{
								updateToken();
								if(theCurrentToken.TokenType == TokenType.SEMICOLON)
								{
									updateToken();
									while(isStatement())
									{
										updateToken();
										if(theCurrentToken.TokenType == TokenType.SEMICOLON)
										{
											updateToken();
										}
										else
										{
											theLogger.LogParseError(theCurrentToken);
											while(theCurrentToken.TokenType != TokenType.SEMICOLON)
											{
												updateToken();
											}
											updateToken();
										}
									}
									
									if(theCurrentToken.TokenType == TokenType.ELSE)
									{
										updateToken();
										if(isStatement())
										{
											updateToken();
											if(theCurrentToken.TokenType == TokenType.SEMICOLON)
											{
												updateToken();
												while(isStatement())
												{
													updateToken();
													if(theCurrentToken.TokenType == TokenType.SEMICOLON)
													{
														updateToken();
													}
													else
													{
														theLogger.LogParseError(theCurrentToken);
														while(theCurrentToken.TokenType != TokenType.SEMICOLON)
														{
															updateToken();
														}
														updateToken();
													}
												}
												
												if(theCurrentToken.TokenType == TokenType.END)
												{
													updateToken();
													if(theCurrentToken.TokenType == TokenType.IF)
													{
														isValid = true;
														System.out.println("If!");
													}
												}
											}
										}
									}
									else if(theCurrentToken.TokenType == TokenType.END)
									{
										updateToken();
										if(theCurrentToken.TokenType == TokenType.IF)
										{
											isValid = true;
											System.out.println("If!");
										}
									}
								}
							}
						}
					}
				}
			}
			
			theCurrentSymbol = null;
		}
		
		return isValid;
	}
	
	// Determine if type of for statement.
	private boolean isForStatement() throws IOException
	{
		boolean isValid = false;
		
		if(theCurrentToken.TokenType == TokenType.FOR)
		{
			theCurrentSymbol = new Symbol(TokenType.IF, theCurrentToken.TokenLineNumber, null, null, null, null, null, false);
			updateToken();
			if(theCurrentToken.TokenType == TokenType.LEFT_PARENTHESIS)
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.IDENTITY)
				{
					updateToken();
					if(isDestination())
					{
						if(theCurrentToken.TokenType == TokenType.ASSIGN)
						{
							updateToken();
							if(isExpression())
							{
								updateToken();
								if(theCurrentToken.TokenType == TokenType.SEMICOLON)
								{
									updateToken();
									if(isExpression())
									{
										updateToken();
										if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
										{
											updateToken();
											while(isStatement())
											{
												System.out.println("LOOP STATEMENT");
												updateToken();
												if(theCurrentToken.TokenType == TokenType.SEMICOLON)
												{
													updateToken();
												}
												else
												{
													theLogger.LogParseError(theCurrentToken);
													while(theCurrentToken.TokenType != TokenType.SEMICOLON)
													{
														updateToken();
													}
													updateToken();
												}
											}
											
											if(theCurrentToken.TokenType == TokenType.END)
											{
												updateToken();
												if(theCurrentToken.TokenType == TokenType.FOR)
												{
													isValid = true;
													System.out.println("For!");
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			theCurrentSymbol = null;
		}
		
		return isValid;
	}
	
	// Determine if valid bound statement.
	private boolean isBoundStatement() throws IOException
	{
		boolean isValid = false;
		StringBuilder lowerBound = new StringBuilder();
		StringBuilder upperBound = new StringBuilder();
		updateToken();
		
		if(theCurrentToken.TokenType == TokenType.MINUS)
		{
			lowerBound.append('-');
			updateToken();
		}
		
		if(isExpression() && theNextToken.TokenType == TokenType.RIGHT_BRACKET)
		{
			updateToken();
			isValid = true;
		}
		else if(theCurrentToken.TokenType == TokenType.FLOAT || theCurrentToken.TokenType == TokenType.INTEGER)
		{
			lowerBound.append(theCurrentToken.TokenValue);
			theSymbolTable.CURR_SYMBOL.setBoundLower(lowerBound.toString().trim());
			
			updateToken();
			if(theCurrentToken.TokenType == TokenType.COLON)
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.MINUS)
				{
					upperBound.append('-');
					updateToken();
				}
				if(theCurrentToken.TokenType == TokenType.FLOAT || theCurrentToken.TokenType == TokenType.INTEGER)
				{
					upperBound.append(theCurrentToken.TokenValue);
					theSymbolTable.CURR_SYMBOL.setBoundUpper(upperBound.toString().trim());
					
					updateToken();
					if(theCurrentToken.TokenType == TokenType.RIGHT_BRACKET)
					{
						isValid = true;
						System.out.println("Bound!");
					}
				}
			}
		}
		
		return isValid;
	}
	
	// Determine if valid type mark.
	private boolean isTypeMark() throws IOException
	{
		// Initialize return value to false.
		boolean isValid = false;
		
		// Check if current token is of "type mark" type.
		switch(theCurrentToken.TokenType)
		{
			case INTEGER: case BOOL: case FLOAT: case CHAR: case STRING:
				isValid = true;
				break;
			default:
				isValid = false;
				break;
		}
		
		return isValid;
	}
	
	// Determine if type of expression.
	private boolean isExpression() throws IOException
	{
		boolean isValid = false;
		
		if(isArithOp())
		{
			if(theNextToken.TokenType == TokenType.AND)
			{
				updateToken();
				updateToken();
				
				if(isExpression())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.OR)
			{
				updateToken();
				updateToken();
				
				if(isExpression())
				{
					isValid = true;
				}
			}
			else
			{
				isValid = true;
			}
		}
		else if(theCurrentToken.TokenType == TokenType.NOT)
		{
			updateToken();
			if(isArithOp())
			{
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Determine if type of arithop.
	private boolean isArithOp() throws IOException
	{
		boolean isValid = false;
		
		if(isRelation())
		{
			if(theNextToken.TokenType == TokenType.PLUS)
			{
				updateToken();
				updateToken();
				
				if(isArithOp())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.MINUS)
			{
				updateToken();
				updateToken();
				
				if(isArithOp())
				{
					isValid = true;
				}
			}
			else
			{
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Determine if type of relation.
	private boolean isRelation() throws IOException
	{
		boolean isValid = false;
		
		if(isTerm())
		{
			if(theNextToken.TokenType == TokenType.LESS_THAN)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.LESS_THAN_EQ)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.GREATER_THAN)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.GREATER_THAN_EQ)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.EQUIVALENT)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.NOT_EQUIVALENT)
			{
				updateToken();
				updateToken();
				if(isRelation())
				{
					isValid = true;
				}
			}
			else
			{
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Determine if type of term.
	private boolean isTerm() throws IOException
	{
		boolean isValid = false;
		
		if(isFactor())
		{
			if(theNextToken.TokenType == TokenType.MULTIPLY)
			{
				updateToken();
				updateToken();
				
				if(isTerm())
				{
					isValid = true;
				}
			}
			else if(theNextToken.TokenType == TokenType.DIVIDE)
			{
				updateToken();
				updateToken();
				
				if(isTerm())
				{
					isValid = true;
				}
			}
			else
			{
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	// Determine if type of factor.
	private boolean isFactor() throws IOException
	{
		boolean isValid = false;
		
		switch(theCurrentToken.TokenType)
		{
			case INTEGER: case TRUE: case FALSE: //case FLOAT:
				if(theCurrentSymbol != null)
				{
					if(theCurrentSymbol.getType() == TokenType.IF || theCurrentSymbol.getType() == TokenType.FOR)
					{
						if(theCurrentToken.TokenType == TokenType.INTEGER || theCurrentToken.TokenType == TokenType.TRUE || theCurrentToken.TokenType == TokenType.FALSE)
						{
							isValid = true;
						}
					}
					else if(theCurrentSymbol.getType() == TokenType.INTEGER || theCurrentSymbol.getType() == TokenType.BOOL  //|| theCurrentSymbol.getType() == TokenType.FLOAT
					|| theCurrentSymbol.getType() == TokenType.GETINTEGER || theCurrentSymbol.getType() == TokenType.GETBOOL //|| theCurrentSymbol.getType() == TokenType.GETFLOAT
					|| theCurrentSymbol.getType() == TokenType.PUTINTEGER || theCurrentSymbol.getType() == TokenType.PUTBOOL //|| theCurrentSymbol.getType() == TokenType.PUTFLOAT
					|| theCurrentSymbol.getType() == TokenType.PROCEDURE)
					{
						isValid = true;
					}
					else if(theCurrentSymbol.getType() == TokenType.FLOAT && theCurrentToken.TokenType == TokenType.INTEGER)
						isValid = true;
				}
				else
					isValid = true;
				break;
			case FLOAT:
				if(theCurrentSymbol != null)
				{
					if( theCurrentSymbol.getType() == TokenType.FLOAT || theCurrentSymbol.getType() == TokenType.GETFLOAT || theCurrentSymbol.getType() == TokenType.PUTFLOAT
					|| theCurrentSymbol.getType() == TokenType.PROCEDURE)
					{
						isValid = true;
					}
				}
				else
					isValid = true;
				break;
			case CHAR:
				if(theCurrentSymbol != null)
				{
					if(theCurrentSymbol.getType() == TokenType.IF || theCurrentSymbol.getType() == TokenType.FOR)
					{
						if(theCurrentSymbol.getParameterTypes() == null)
						{
							theCurrentSymbol.setParameterTypes(Arrays.asList(theCurrentToken.TokenType));
							isValid = true;
						}
						else if(theCurrentSymbol.getParameterTypes().get(0) == theCurrentToken.TokenType)
						{
							isValid = true;
						}
					}
					if(theCurrentSymbol.getType() == TokenType.CHAR || theCurrentSymbol.getType() == TokenType.GETCHAR || 
					theCurrentSymbol.getType() == TokenType.PUTCHAR || theCurrentSymbol.getType() == TokenType.PROCEDURE)
					{
						isValid = true;
					}
				}
				else
					isValid = true;
				break;
			case STRING:
				if(theCurrentSymbol != null)
				{
					if(theCurrentSymbol.getType() == TokenType.IF || theCurrentSymbol.getType() == TokenType.FOR)
					{
						if(theCurrentSymbol.getParameterTypes() == null)
						{
							theCurrentSymbol.setParameterTypes(Arrays.asList(theCurrentToken.TokenType));
							isValid = true;
						}
						else if(theCurrentSymbol.getParameterTypes().get(0) == theCurrentToken.TokenType)
						{
							isValid = true;
						}
					}
					
					if(theCurrentSymbol.getType() == TokenType.STRING || theCurrentSymbol.getType() == TokenType.GETSTRING || 
					theCurrentSymbol.getType() == TokenType.PUTSTRING || theCurrentSymbol.getType() == TokenType.PROCEDURE)
					{
						isValid = true;
					}
				}
				else
					isValid = true;
				break;
			case MINUS:
				updateToken();
				if(isName())
				{
					isValid = true;
				}
				else if(isFactor())
				{
					isValid = true;
				}
				break;
			case LEFT_PARENTHESIS:
				updateToken();
				if(isExpression())
				{
					updateToken();
					if(theCurrentToken.TokenType == TokenType.RIGHT_PARENTHESIS)
					{
						isValid = true;
					}
				}
				break;
			case IDENTITY:
				isValid = isName();
				break;
			default:
				isValid = false;
				break;
		}
		
		return isValid;
	}
	
	// Determine if type of name.
	private boolean isName() throws IOException
	{
		boolean isValid = false;
		
		if(theNextToken.TokenType == TokenType.LEFT_BRACKET)
		{
			updateToken();
			updateToken();
			if(isExpression())
			{
				updateToken();
				if(theCurrentToken.TokenType == TokenType.RIGHT_BRACKET)
				{
					isValid = true;
				}
			}
		}
		else if(theCurrentToken.TokenType == TokenType.IDENTITY)
		{
			isValid = true;
		}
		
		return isValid;
	}
}