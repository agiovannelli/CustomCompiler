package objects;

import java.util.ArrayList;
import java.util.List;

public class Symbol
{
	// Private variables.
	TokenType Type;
	int LineNumber;
	String BoundLower;
	String BoundUpper;
	boolean Global;
	
	// Procedure specific variables.
	List<String> Parameters;
	List<TokenType> ParameterTypes;
	List<TokenType> ParameterReturnTypes;
	
	// Empty constructor.
	public Symbol()
	{
		// Placeholder.
		Type = null;
		LineNumber = -1;
		Global = false;
		
		Parameters = new ArrayList<String>();
		ParameterTypes = new ArrayList<TokenType>();
		ParameterReturnTypes = new ArrayList<TokenType>();
	}
	
	// Constructor.
	public Symbol(TokenType aType, int aLineNumber, String aBoundLower, String aBoundUpper, 
			List<String> aParameters, List<TokenType> aParameterTypes, List<TokenType> aParameterReturnTypes,
			boolean aGlobal)
	{
		Type = aType;
		LineNumber = aLineNumber;
		BoundLower = aBoundLower;
		BoundUpper = aBoundUpper;
		Global = aGlobal;
		
		Parameters = aParameters;
		ParameterTypes = aParameterTypes;
		ParameterReturnTypes = aParameterReturnTypes;
	}

	public TokenType getType()
	{
		return Type;
	}

	public void setType(TokenType type)
	{
		Type = type;
	}

	public int getLineNumber()
	{
		return LineNumber;
	}

	public void setLineNumber(int lineNumber)
	{
		LineNumber = lineNumber;
	}

	public String getBoundLower()
	{
		return BoundLower;
	}

	public void setBoundLower(String boundLower)
	{
		BoundLower = boundLower;
	}

	public String getBoundUpper()
	{
		return BoundUpper;
	}

	public void setBoundUpper(String boundUpper)
	{
		BoundUpper = boundUpper;
	}

	public List<String> getParameters()
	{
		return Parameters;
	}

	public void setParameters(List<String> parameters)
	{
		Parameters = parameters;
	}

	public List<TokenType> getParameterTypes()
	{
		return ParameterTypes;
	}

	public void setParameterTypes(List<TokenType> parameterTypes)
	{
		ParameterTypes = parameterTypes;
	}

	public List<TokenType> getParameterReturnTypes()
	{
		return ParameterReturnTypes;
	}

	public void setParameterReturnTypes(List<TokenType> parameterReturnTypes)
	{
		ParameterReturnTypes = parameterReturnTypes;
	}
	
	public void addParameters(String aParameter)
	{
		Parameters.add(aParameter);
	}
	
	public void addParameterTypes(TokenType aParameterType)
	{
		ParameterTypes.add(aParameterType);
	}
	
	public void addParameterReturnTypes(TokenType aParameterReturnType)
	{
		ParameterReturnTypes.add(aParameterReturnType);
	}

	public boolean isGlobal()
	{
		return Global;
	}

	public void setGlobal(boolean global)
	{
		Global = global;
	}
}
