package classes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import objects.Symbol;
import objects.TokenType;

public class Translator
{
	// Public properties.
	public Queue<String> theGlobalDeclarationQueue;
	public Queue<String> theHeaderQueue;
	public Queue<List<String>> theBodyQueue;
	
	public Stack<String> theIncompleteHeaderStack;
	public Stack<List<String>> theIncompleteBodyStack;
	public Stack<Integer> theIncompleteTempIntegerStack;
	
	public String theCurrentHeader;
	public List<String> theCurrentBody;
	public Integer theCurrentTempInteger;
	
	// Private properties.
	Path theFilePath;
	
	// Constructor.
	public Translator(String aProgramName) throws IOException
	{
		// Initialize queues.
		theGlobalDeclarationQueue = new ArrayDeque<String>();
		theHeaderQueue = new ArrayDeque<String>();
		theBodyQueue = new ArrayDeque<List<String>>();
		
		// Initialize stacks.
		theIncompleteHeaderStack = new Stack<String>();
		theIncompleteBodyStack = new Stack<List<String>>();
		theIncompleteTempIntegerStack = new Stack<Integer>();
		
		theCurrentBody = new ArrayList<String>();
		theCurrentTempInteger = 1;
		
		// Generate .ll file with passed program name.
		theFilePath = Paths.get(aProgramName + ".ll");
	}
	
	// Takes all data from stack and adds it to constructed .ll file.
	public void WriteCodeToFile() throws IOException
	{
		// Initialize collection to hold ordered code.
		List<String> finalList = new ArrayList<String>();
		
		// Add final queue items. 
		theHeaderQueue.add(theCurrentHeader);
		theBodyQueue.add(theCurrentBody);
		
		// Add each collection from stacks into final list.
		while(!theGlobalDeclarationQueue.isEmpty())
		{
			finalList.add(theGlobalDeclarationQueue.remove());
		}
		
		finalList.add("");
		
		while(!theHeaderQueue.isEmpty())
		{
			// Body queue pop goes after. Occurs once, since body for each header. 
			finalList.add(theHeaderQueue.remove());
			finalList.addAll(theBodyQueue.remove());
			finalList.add("");
		}
		
		// On end of header stack, add to complete main() call.
		finalList.add("ret i32 0\n}");
		
		// Write code to .ll file.
		Files.write(theFilePath, finalList, Charset.forName("UTF-8"));
	}
	
	// Allows pushing of program sections to local Stack.
	public void AddHeaderStrings(String aHeader)
	{
		theCurrentHeader = aHeader;
	}
	
	// We need to hold incomplete header and program data till complete write.
	public void HoldIncompleteData()
	{
		theIncompleteHeaderStack.push(theCurrentHeader);
		theIncompleteBodyStack.push(theCurrentBody);
		theIncompleteTempIntegerStack.push(theCurrentTempInteger);
		
		theCurrentHeader = new String();
		theCurrentBody = new ArrayList<String>();
		theCurrentTempInteger = 1;
	}
	
	// On completion of a procedure/program body, we need to pop the incomplete data from stack and migrate it to complete stack.
	public void ReturnToIncompleteData()
	{
		// Add closing string to body.
		theCurrentBody.add("ret void\n}");
		theHeaderQueue.add(theCurrentHeader);
		theBodyQueue.add(theCurrentBody);
		
		String aHeader = theIncompleteHeaderStack.pop();
		List<String> aBody = theIncompleteBodyStack.pop();
		
		theCurrentHeader = aHeader;
		theCurrentBody = aBody;
	}
	
	// This will return a string object containing the corresponding LLVM declaration for a given symbol.
	public String VariableDeclarationBuilder(String aVariableName, Symbol aSymbol)
	{
		// Init.
		StringBuilder theVariableDeclaration = new StringBuilder();
		
		// Lets determine the starting character for the LLVM syntax.
		if(aSymbol.isGlobal())
			theVariableDeclaration.append('@');
		else
			theVariableDeclaration.append('%');
		
		// Tack the variable name onto init char.
		theVariableDeclaration.append(aVariableName);
		
		// Allocate according to scope.
		if(aSymbol.isGlobal())
			theVariableDeclaration.append(" = common global ");
		else
			theVariableDeclaration.append(" = alloca ");
		
		// Determine type to append.
		theVariableDeclaration.append(SetupTypeInIR(aSymbol));
		
		return theVariableDeclaration.toString().trim();
	}
	
	// This will return a string object containing the LLVM procedure equivalent for a given symbol.
	public void ProcedureDeclarationBuilder(String aVariableName, Symbol aSymbol)
	{
		StringBuilder aProcedureHeader = new StringBuilder();
		
		aProcedureHeader.append("define void @" + aVariableName + "(");
		if(!aSymbol.getParameters().isEmpty())
		{
			// Update header.
			aProcedureHeader.append(SetupProcedureParameters(aSymbol));
		}

		aProcedureHeader.append(") {");		
		
		theCurrentHeader = aProcedureHeader.toString().trim();
	}
	
	// Provide parameters to procedure header being generated for LLVM.
	private String SetupProcedureParameters(Symbol aSymbol)
	{
		StringBuilder aProcedureParameterString = new StringBuilder();
		
		List<String> aVariableList = aSymbol.getParameters();
		List<TokenType> aTypeList = aSymbol.getParameterTypes();
		List<TokenType> aReturnList = aSymbol.getParameterReturnTypes();
		
		for (int i = 0; i < aTypeList.size(); i++)
		{
			// Append llvm variable type equivalent.
			aProcedureParameterString.append(ProvideParameterType(aTypeList.get(i)));
			
			// Append * for OUT/INOUT variable types.
			if(aReturnList.get(i) != TokenType.IN)
			{
				aProcedureParameterString.append("*");
			}
			
			// Append variable name and continue list.
			aProcedureParameterString.append(" %" + aVariableList.get(i) + ", ");
			
			// Generate declarations in procedure body.
			ParameterDeclarationBuilder(aTypeList.get(i), aReturnList.get(i));
			InitialStoreStatementBuilder(aVariableList.get(i), aTypeList.get(i), aReturnList.get(i));
		}
		
		// Remove last comma from string.
		String aReturnProcedureParameterString = 
				aProcedureParameterString.toString().trim().substring(0, aProcedureParameterString.toString().length() - 2);
		
		return aReturnProcedureParameterString;
	}
	
	// Creates temp value for parameter declared in procedure. Adds to theCurrentBody on completion.
	private void ParameterDeclarationBuilder(TokenType aType, TokenType aReturnType)
	{
		StringBuilder theParameterDeclaration = new StringBuilder();
		
		theParameterDeclaration.append("%" + theCurrentTempInteger + " = alloca ");
		theParameterDeclaration.append(SetupParameterInIR(aType, aReturnType));
		
		theCurrentBody.add(theParameterDeclaration.toString().trim());
		theCurrentTempInteger++;
	}
	
	// Performs initial store on parameter values provided in function.
	private void InitialStoreStatementBuilder(String aVariableName, TokenType aType, TokenType aReturnType)
	{
		StringBuilder theStoreStatement = new StringBuilder();
		
		// Add pointers onto types as necessary.
		if(aReturnType == TokenType.IN)
		{
			theStoreStatement.append("store " + ProvideParameterType(aType) + " %" + aVariableName);
			theStoreStatement.append(", " + ProvideParameterType(aType) + "* %" + (theCurrentTempInteger-1));
		}
		else
		{
			theStoreStatement.append("store " + ProvideParameterType(aType) + "* %" + aVariableName);
			theStoreStatement.append(", " + ProvideParameterType(aType) + "** %" + (theCurrentTempInteger-1));
		}
		
		theCurrentBody.add(theStoreStatement.toString().trim());
	}
	
	// Returns parameter type IR equivalent from provided token.
	private String ProvideParameterType(TokenType aToken)
	{
		switch(aToken)
		{
			case INTEGER: case BOOL: case FALSE: case TRUE:
				return "i32";
			case FLOAT:
				return "float";
			case CHAR:
				return "i8";
			case STRING:
				return "i8*";
			default:
				return null;
		}
	}
	
	// Generates type ending for parameter declarations in IR.
	private String SetupParameterInIR(TokenType aType, TokenType aReturnType)
	{
		StringBuilder theParameter = new StringBuilder();
		
		switch(aType)
		{
			case INTEGER: case TRUE: case FALSE: case BOOL:
				if(aReturnType != TokenType.IN)
					return "i32*, align 8";
				else
					return "i32, align 4";
			case CHAR:
				if(aReturnType != TokenType.IN)
					return "i8*, align 8";
				else
					return "i8, align 1";
			case FLOAT:
				if(aReturnType != TokenType.IN)
					return "float*, align 8";
				else
					return "float, align 4";
			case STRING:
				// TODO: Determine how to manage this case...
				break;
			default:
				break;
		}
		
		return theParameter.toString().trim();
	}
	
	// Provides LLVM type equivalent from given TokenType of a symbol.
	private String SetupTypeInIR(Symbol aSymbol)
	{
		if(aSymbol.getBoundLower() == null)
		{
			// We create a standard value when the symbol is not bound and return it.
			switch(aSymbol.getType())
			{
				case INTEGER: case TRUE: case FALSE: case BOOL:
					if(aSymbol.isGlobal())
						return "i32 0, align 4";
					else
						return "i32, align 4";
				case CHAR:
					if(aSymbol.isGlobal())
						return "i8 0, align 1";
					else
						return "i8, align 1";
				case FLOAT:
					if(aSymbol.isGlobal())
						return "float 0.000000e+00, align 4";
					else
						return "float, align 4";
				case STRING:
					if(aSymbol.isGlobal())
						return "i8* null, align 8";
					else 
						return "i8*, align 8";
				default:
					break;
			}
		}
		else
		{
			// Create range of array from bound values.
			int aRange = Integer.parseInt(aSymbol.getBoundUpper()) - Integer.parseInt(aSymbol.getBoundLower());
			StringBuilder aReturnString = new StringBuilder();
			
			// Build string value of array data type in LLVM. 
			switch(aSymbol.getType())
			{
			case INTEGER: case TRUE: case FALSE: case BOOL:
				if(aSymbol.isGlobal())
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x i32] zeroinitializer, align 16");
				}
				else
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x i32], align 16");
				}
				return aReturnString.toString().trim();
			case CHAR:
				if(aSymbol.isGlobal())
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x i8] zeroinitializer, align 1");
				}
				else
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x i8], align 1");
				}
				return aReturnString.toString().trim();
			case FLOAT:
				if(aSymbol.isGlobal())
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x float] zeroinitializer, align 16");
				}
				else
				{
					aReturnString.append('[');
					aReturnString.append(aRange);
					aReturnString.append(" x float], align 16");
				}
				return aReturnString.toString().trim();
			case STRING:
				if(aSymbol.isGlobal())
				{
					aReturnString.append("i8** null, align 8");
				}
				else
				{
					aReturnString.append("i8**, align 8");
				}
				return aReturnString.toString().trim();
			default:
				break;
			}
		}
		
		return null;
	}
}
