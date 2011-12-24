package kwic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the main class for performing a Key Word In Context (KWIC)
 * search on an ASCII file.  It contains the main data structures that 
 * hold the index generated from the text file and provides all the 
 * mechanisms to parse the file, generate the index, and lookup 
 * keywords from the index. 
 * 
 * @author Michael Chalson
 *
 */

public class KwicSearch {
	// Define the max number of keyword instances we will hold onto
	private static final int MAX_KEY_COUNT = KwicConstants.MAX_KEY_COUNT;
	// Define the max number of keyword instances we will display
	protected static final int MAX_KEY_DISPLAY = KwicConstants.MAX_KEY_DISPLAY;
	// Define the number of leading and trailing characters that 
	// make up the "context" of the keyword
	private static final int CONTEXT_SIZE = KwicConstants.CONTEXT_SIZE;
	
	
	// File name of the file to be indexed (Default to OliverTwist.txt)
	private String mFileName = "OliverTwist.txt";
	
	// Status flag for index generation process
	private boolean mIndexDone = false;
	
	// Worker Thread to generate keyword index
	private Thread mIndexWorkThread;
	
	
	/* Main keyword index
	 * This is the main data structure for holding the keyword index
	 */
	private Map<String, KeywordContainer> mKeywordMap = new HashMap<String, KeywordContainer>();
	
	/* Dead word list
	 * This is a list of words that have already been seen too many times
	 * It may be quicker to check this list before attempting to add new 
	 * words to the main keyword map
	 */
	private Set<String> mDeadWordsList = new HashSet<String>();
	
	// Define the index worker runnable
	private class IndexWorker implements Runnable {

		// Define run method
		@Override
		public void run() {
			// Execute code to build the index for the specified text file
			try{
				buildIndex();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			
		}
	}

	
	// Basic Constructor
	/*
	 *  Note: the default file name is "OliverTwist.txt", which
	 *  may not exist or be findable from the context where this
	 *  application is run.  This constructor is not used in the 
	 *  original implementation of the KWIC application 
	 */
	public KwicSearch(){
		// Create worker thread
		this.mIndexWorkThread = new Thread(new IndexWorker());
	}
		
	// Constructor that sets file name to search
	public KwicSearch(String fileName){
		// Set file name to search
		this.mFileName = fileName;
		// Create worker thread
		this.mIndexWorkThread = new Thread(new IndexWorker());
	}
	
	// Method to build the index for the named text file
	private void buildIndex() throws IOException {
		// Define file input stream
		BufferedReader inputStream = null;
		
		/*
		 *  Define Strings to hold lines of text
		 *  In order to allow context to wrap over line breaks we need
		 *  to always have "prev" and "next" line data available
		 */
		String prevLine = null, currLine = null, nextLine = null;
		String lineWords, allLines;
		// Define variables for length of line buffers
		int prevLen, currLen, nextLen;
		
		
		// Define and initialize line number counter
		// (count one behind because we need to buffer the input)
		int lineNum = 0; 
		
		// Define loop done flag for main loop over lines in file
		// Default to true so that if the first readline fails we don't process anything
		boolean loopDone = true;
		
		// Initialize safety counter for while loop
		// TODO: Consider getting rid of this, it may be sloppy design
		int safeCntr = 0;

		// --- Define variables used in context string creation ---

		Pattern pattern = Pattern.compile("[a-zA-Z]+'?[a-zA-Z]");
		Matcher matcher;
		// Holders for start and stop index from Regex matches
		int startInd, endInd;
		// Holder for matched word from Regex
		String parsedWord;
		
		// Holder for length of full buffer
		int fullLen;
		// Holders for updated start and end indices of keyword within full buffer
		int fullStartInd, fullEndInd;
		
		// Initialize sizes of left-side and right-side padding strings
		int leftPadSize = 0, rightPadSize = 0;
		// Initialize left-side and right-side padding strings
		String leftPad = "", rightPad = "";
		// Declare left and right context strings
		String leftContext, rightContext, fullContext;
		
		// Define String to mark line breaks (new line) in context window
		String lineBreakStr = " ";
		// ---------- End context string create variables ---------
		
		// Define flag to check if last word of prev line wrapped to curr line
		/*
		 *  Note: this will probably be handy if I add functionality to handle
		 *  hyphenated word continuations on the next line 
		 */
		boolean wrapWord = false;
		
		// Define a KeywordContainer to use
		KeywordContainer keyContainer = null;
		
		/*
		 * Read in one line at a time from text file and generate keyword index
		 */
		try{
			// Create file input streams
			inputStream 	= new BufferedReader(new FileReader(mFileName));
			
			// Read in first line and initialize buffers 
			lineWords = cleanupFirstLine(inputStream.readLine());
			if (lineWords != null){
				nextLine = lineWords;
				loopDone = false;
			}
			
			// Loop over lines of text in file
			while( !loopDone && safeCntr < KwicConstants.MAX_TEXT_LINES){
				// Increment safety counter
				// TODO: consider getting rid of this, it may be sloppy
				safeCntr++;
				
				// Get next line from file
				lineWords = inputStream.readLine();
				
				// Check if this is the last pass
				if (lineWords == null){
					loopDone = true;
				} 
				
				// Increment line counter (count one behind because we need to buffer the input)
				lineNum++;
				
				// Update line buffers for this round
				prevLine = currLine;
				currLine = nextLine;
				nextLine = lineWords;
				
				// Update line buffer metrics
				prevLen = (prevLine == null) ? 0 : prevLine.length();
				currLen = (currLine == null) ? 0 : currLine.length();
				nextLen = (nextLine == null) ? 0 : nextLine.length();
								
				
				//--- Build full buffer ---
				if (prevLen > 0){
					allLines = prevLine+lineBreakStr+currLine;
					prevLen+=lineBreakStr.length();
				} else {
					allLines = currLine;
				}
				if (nextLen > 0){
					allLines = allLines+lineBreakStr+nextLine;
				}
				// Compute length of full buffer
				fullLen = allLines.length();
				
				
				// Create pattern matcher for current line
				matcher = pattern.matcher(currLine);
				
				// Loop over matching patterns in current line
				while (matcher.find()){
					// Grab keyword
					parsedWord 	= matcher.group().toLowerCase();
					// Grab start and end indices of keyword
					startInd 	= matcher.start();
					endInd 		= matcher.end();
										
					
					/*
					 * Check each word in this line, add to Map and increment word count
					 * 
					 * This implementation uses a "dead list" and a main index.  
					 * The idea is that there may be an implementation of a list of "dead"
					 * words that can be checked more quickly than checking for
					 * the number of instances of a queried keyword in the main index.  
					 * Additionally, if a word is known to be "dead", there is no need to
					 * perform the processing to create the context data.
					 * For now, the dead list is implemented as a Java HashSet, which
					 * does not allow duplicate values.  If search times are slow, it
					 * may be a good idea to switch to a red-black tree or prioritized
					 * set so that commonly checked words can be found quickly. 
					 */
					// Check dead list
					if (mDeadWordsList.contains(parsedWord)){
						// This word is already on the dead list
						
						// TODO: Do stuff to get more benefit from deadlist
						
					} else {
						// This word is not dead yet
					
						//-------- Create context Data --------
						// Compute new keyword start and stop indices for full buffer
						fullStartInd 	= startInd + prevLen;
						fullEndInd 		= endInd + prevLen;
						
						/*
						 * Create any necessary extra white space characters
						 * 
						 * If there are not enough extra characters before the 
						 * keyword in the full buffer, fill the rest of the 
						 * left-side context buffer with characters.  Likewise 
						 * fill up the right-side context buffer with extra
						 * white space characters if necessary 
						 */
						leftPadSize = CONTEXT_SIZE-fullStartInd;
						// Right pad size is N - (len - endInd)
						rightPadSize = CONTEXT_SIZE - fullLen + fullEndInd;
						
						//--- Generate Padding and Context Data ---
						//-- Left side context 
						if ( leftPadSize > 0){
							leftPad = String.format("%1$"+leftPadSize+"s","");
							leftContext = leftPad+allLines.substring(0, fullStartInd);
						} else {
							leftContext = allLines.substring(fullStartInd - CONTEXT_SIZE,fullStartInd);
						}
						//-- Right side context
						if (rightPadSize > 0){
							rightPad = String.format("%1$"+rightPadSize+"s","");
							rightContext = allLines.substring(fullEndInd) + rightPad;
						} else {
							rightContext = allLines.substring(fullEndInd,fullEndInd + CONTEXT_SIZE);
						}
						
						// Construct full context string
						fullContext = leftContext+allLines.substring(fullStartInd, fullEndInd)+rightContext;
						//-------- End Create context Data --------				
						
						
												
						// Check if this word is already in the index
						keyContainer = mKeywordMap.get(parsedWord);
						if (keyContainer == null){
							// Add this keyword to the index
							/*
							 *  Note: building index on all lower case keywords, this will
							 *  have implications on expected results with regard to
							 *  case-sensitivity 
							 */
							mKeywordMap.put(parsedWord, new KeywordContainer(1,new KeywordData(lineNum,fullContext)));
						} else {
							// Add another instance of this keyword to the index
							keyContainer.incrementWordCount();
							keyContainer.getDetailList().add(new KeywordData(lineNum,fullContext));
							
							// Check if we need to kill this keyword now
							if (keyContainer.getWordCount()>=MAX_KEY_COUNT){
								// Add this word to the dead list so we don't keep adding to the index
								mDeadWordsList.add(parsedWord);
							}
						} // End of adding new instance to index	
					} // End of checking dead list
					
				} // End of while loop over matching regex patterns		
				
			} // End of while loop over file
			// Check if we only broke out because of loop limit
			if (!loopDone){
				System.out.format("%nThe whole file may not have been indexed."+
									"%nConsider increasing the MAX_TEXT_LINES variable.%n");
			}
			
		} // End of try block (mostly for inputStream FileReader)
		/*
		 * TODO: Consider adding exception handling for some other
		 * activities going on in here, such as the regex processing
		 * and potential null pointer issues in the dead list and
		 * main index
		 */
		finally {
			// Set index finished flag to true
			mIndexDone = true;
			
			if(inputStream != null){
				inputStream.close();
			}			
		}
	}
	
	/*
	 * Method to implement query processing
	 */
	protected void processQueryKeyword(String keyword){
		int lineNum;
		int lineNumStrSize = 10;
		String contextStr;
		String lineNumStr;
		String padding;
		
		KeywordContainer keyContainer = mKeywordMap.get(keyword.toLowerCase());
		KeywordData wordData;
		
		if (keyContainer == null){
			// Keyword was not found in index
			System.out.format("%nSorry, the keyword \""+keyword+"\" was not found in the index");
		} else {
			System.out.format("%nThe keyword \""+keyword+"\" was found in the following contexts:%n");
			// Loop over all found indices of the keyword
			for (int keywordInd = 0; keywordInd < Math.min(keyContainer.getWordCount(),MAX_KEY_DISPLAY); keywordInd++){
				// Get the data for this instance of the keyword
				wordData = keyContainer.getDetailList().get(keywordInd);
				
				//--- Get the line number and the context string
				lineNum = wordData.getLineNubmer();
				contextStr = wordData.getContextStr();
				
				//--- Print the summary for this instance
				// Format line number string
				lineNumStr = Integer.toString(lineNum);
				padding = String.format("%1$"+(lineNumStrSize-lineNumStr.length())+"s","");
				lineNumStr = lineNumStr+padding;
				// Print summary
				System.out.format("%n%s:%s",lineNumStr,contextStr);
			}
		}
	}
	
	// Method to start the Index worker thread
	protected void startIndexWorker(){
		// Start the indexWorkerThread Thread
		mIndexWorkThread.start();
	}
	
	// Getter for IndexDone flag
	protected boolean isIndexDone(){
		return mIndexDone;
	}
	
	// Setter for IndexDone flag
	// This should not be needed, but it is available just in case
	protected void setIndexDone(boolean isDone){
		mIndexDone = isDone;
	}
	
	// Method to ensure some data quality of the first line in the file
	protected String cleanupFirstLine(String inStr){
		String cleanStr = null;
		// Define known utf Byte Order Markings (BOM) code point
		int bomCodePoint = 65279;
		// Grab the first character in the 
		int firstCode = inStr.codePointAt(0);
		
		
		// Check if first character is BOM and remove it from string if so
		/*
		 * If text files are properly saved as ASCII, these characters should not be present,
		 * but let's check just to be safe.
		 */
		if (firstCode == bomCodePoint){
			cleanStr = inStr.substring(1);
		} else {
			cleanStr = inStr;
		}
		
		return cleanStr;
	}
}
