package kwic;

/** Keyword detail holder
 * This is a helper class to encapsulate the relevant data for one
 * instance of a given keyword
 * 
 * @author Michael Chalson
 * 
 */

public class KeywordData {
	private int lineNumber;
	private String contextStr;
	
	// Basic Constructor
	protected KeywordData(){
		this.lineNumber = 0;
		this.contextStr = null;
	}
	
	// Constructor that sets both fields
	protected KeywordData(int line,String context){
		this.lineNumber = line;
		this.contextStr = context;
	}
	
	// getter for lineNumer
	protected int getLineNubmer(){
		return lineNumber;
	}
	
	// setter for lineNumber
	protected void setLineNumber(int line){
		this.lineNumber = line;
	}
	
	// getter for contextStr
	protected String getContextStr(){
		return contextStr;
	}
	
	// setter for contextStr
	protected void setContextStr(String context){
		this.contextStr = context;
	}
	
	// Method to analyze and store contextStr
	protected void storeContextStr(String rawContext){
		// Stub for future enhancements
		// TODO: fill in this method
	}
	
	// Method to build and return detailed context report
	protected String getDetailedData(){
		// Stub for future enhancements
		// TODO: replace with real detail builder
		return "Still Need to Implement This";
	}
}
