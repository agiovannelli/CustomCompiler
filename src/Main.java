import java.util.ArrayList;
import java.util.List;

import classes.ErrorLogger;
import classes.Scan;
import objects.SymbolTable;
import classes.Parse;

public class Main
{

	public static void main(String[] args) throws Exception
	{
		List<String> PathList = new ArrayList<String>();
		for(int i = 0; i < args.length; i++)
		{
			PathList.add(args[i]);
		}
		

		for(String path : PathList)
		{
			// Initialize Scan class.
			ErrorLogger theLogger = new ErrorLogger();
			SymbolTable theSymbolTable = new SymbolTable(theLogger);
			Scan theScanner = new Scan(path);
			Parse theParser = new Parse(theScanner, theLogger, theSymbolTable);

			try
			{
				theParser.ParseTokens();
				System.out.println();
			} 
			catch (Exception e)
			{
				// Error logger has printed log.
				System.out.println("Failed to read file path.");
			}
		}
	}

}
