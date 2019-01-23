package objects;

public class Token
{
	// Properties.
	public TokenType TokenType;
	public String TokenValue;
	public int TokenLineNumber;
	
	// Constructor.
	public Token(TokenType aTokenType, String aTokenValue, int aTokenLineNumber)
	{
		// Initialize global values.
		TokenType = aTokenType;
		TokenValue = aTokenValue;
		TokenLineNumber = aTokenLineNumber;
	}
}
