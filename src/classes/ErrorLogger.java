package classes;

//import java.io.IOException;

import objects.Token;

public class ErrorLogger
{	
	// Constructor.
	public ErrorLogger()
	{
	}
	
	public void LogScanError(Token aCurrentToken)
	{
		System.out.println("Failed to scan value. Line location: " + aCurrentToken.TokenLineNumber);
	}
	
	public void LogScanError()
	{
		System.out.println("Failed to scan value. Unable to determine line location.");
	}
	
	// Overload of LogParseError for less informative case.
	public void LogParseError(Token aCurrentToken) //throws IOException
	{
		System.out.println("Failed to parse token value '" + aCurrentToken.TokenValue + "'."
				+ " Line location: " + aCurrentToken.TokenLineNumber + ".");
	}
	
	public void LogSymbolError(String aSymbolKey) //throws IOException
	{
		System.out.println("Failed to add symbol for given key '" + aSymbolKey + "'.");
	}
	
	public void LogInvalidProcedureParameter(String aCurrentToken) //throws IOException
	{
		System.out.println("Failed to run procedure due to invalid input argument: '" + aCurrentToken + "'.");
	}
}
