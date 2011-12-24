package kwic;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * KwicLauncher is the launching class of a Keyword In Context (KWIC)
 * Application.  The application prompts the user for the name of an
 * ASCII text file to index.  The file is parsed to create a searchable
 * index of keywords, along with the context in which the keywords were
 * found.  When the index is complete, the user is prompted for a keyword
 * to lookup.  The first N (10 by default) instances of the keyword are 
 * printed to the screen, along with the line number where it was found
 * and the leading and trailing M (30 by default) characters around the 
 * original keyword location.
 * 
 *  For now, this is implemented to only allow one file to be indexed
 *  and searched.  The implementation is easily extensible to allow
 *  the handling of multiple files.  The necessary changes are as follows:
 *  1) The control logic needs to allow the user to navigate back to 
 *  State 1 more than once
 *  2) The "chrome" method for state 2 has to display the available file
 *  indices and allow the user to choose which file to search
 *  3) Each new file should automatically spawn it's own worker thread to
 *  perform the indexing process, but Thread safety should definitely be
 *  checked before attempting to use this program on multiple files
 *  
 * @author Michael Chalson
 *
 */

public class KwicLauncher {

	// File name of the file to be indexed
	private String mFileName;
	// Full path to file
	private String mFileFullPath;
	// Define file object for ascii file
	private File mFileObj;
	
	// Flag to show input argument was given
	private boolean mGivenInput = false;
	
	// Create command line input scanner
	Scanner mCmndIn = new Scanner(System.in);
	
	/*
	 *  Define main search object holder
	 *  Do this as a list because in the future we 
	 *  may want to allow multiple files to be indexed at once
	 */
	List<KwicSearch> mKwickers = new ArrayList<KwicSearch>();

	// Basic Constructor
	public KwicLauncher(){

	}
	
	// Constructor with input file name
	public KwicLauncher(String filename){
		this.mFileName = filename;
		this.mGivenInput = true;
	}
	
	/*
	 * Method to enforce the desired rules for "what is a word"
	 * How should all contiguous, non-white space characters be
	 * treated, especially numbers, punctuation, etc.
	 */
	private String cleanupWord(String inWord){
		// Stub for future use
		return inWord;
	}
	
	/*
	 * Method to cleanup user input for File Name
	 * This may include handling usage of quotation marks, etc.
	 */
	private String cleanupFileNameInput(String inStr){
		// Stub for future use
		return inStr;
	}
	
	
	
	// Implement the bells and whistles for UI in State 1: File selection
	private int uiFileStateChrome(int stateFlag) throws IOException {
		// Define holder for user inputs
		String inputStr;
		// Define holder for file name
		String fileName;
		
		// Try catch for Scanner actions (mCmndIn)
		try {
			
			// If file name was not set as argument, prompt for file name
			if (mGivenInput){
				inputStr = mFileName;
				// Kill Given input flag so we don't keep trying to use it
				mGivenInput = false;
			}
			else {
				// Main file load prompt
				System.out.format("%nUser Options:%n  0: Exit Program%n  [filename]: Create searchable index of specified file%n");
				inputStr = mCmndIn.nextLine();
				
				// Cleanup File Name Input
				inputStr = cleanupFileNameInput(inputStr);
			}
			
			
			// Check for exit
			if (inputStr.equalsIgnoreCase("0")){
				// Double Check Before Exiting
				System.out.format("%nYou entered: %s%n", inputStr);
				System.out.format("Do you really want to exit (Y/y):%n");
				inputStr = mCmndIn.nextLine();
				
				if (inputStr.equalsIgnoreCase("y")){
					System.out.format("%nSorry to see you go.  Goodbye%n");
					// Set return code to 0, which should cause the program to exit
					stateFlag = 0;
				} else {
					System.out.format("%nGreat!  Glad you are sticking around%n");
				}
			} else { // Error check the specified file name
				
				// TODO: Research potential TOCTTOU security flaw
				System.out.format("%nYou entered: %s%n", inputStr);
				// Save off file name so we can reuse inputStr 
				fileName = inputStr;
				
				// TODO: Can I do a better job checking for ASCII file type?
				try {
					// Create File object based on fileName
					mFileObj = new File(fileName);
					// Check for read permissions
					if(mFileObj.canRead()){
						// Get full path to file
						mFileFullPath = mFileObj.getAbsolutePath();
						
						// Double check that they want to index this file
						System.out.format("%nAre you sure you want to index this file (Y/y):%n");
						inputStr = mCmndIn.nextLine();
						if (inputStr.equalsIgnoreCase("y")){
							System.out.format("%nGreat!  Let's start indexing that file%n");
							// Update file name field
							mFileName = fileName;
							// Change UI state flag
							stateFlag = 2;						
						} else {
							System.out.format("%nNo problem!  Let's try again%n");
						}
					} else {
						System.out.format("%n\"%s\" could not be found.%nCheck that file name and file permissions%n",fileName);
					}

				} catch (NullPointerException e){
					e.printStackTrace();
				} 			
			}
			
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {

		} // End Try catch for Scanner actions (mCmndIn)
		
		// Send back the state code
		return stateFlag;
	}
	
	// Implement the bells and whistles for UI in State 2: Keyword search
	private int uiQueryStateChrome(int stateFlag){
		/*
		 * Define local KWIC search object
		 * For now we will always assume we want the first
		 * kwicker, even if several are available 
		 */
		KwicSearch localKwicker;
		
		// Define holder for user inputs
		String inputStr;
			
		// Try catch for Scanner actions (mCmndIn)
		try {
			// Main keyword search prompt
			System.out.format("%nUser Options:%n  0: Exit Program%n  [keyword]: Search for keyword in file index%n");
			inputStr = mCmndIn.nextLine();
			
			// Check for exit
			if (inputStr.equalsIgnoreCase("0")){
				// Double Check Before Exiting
				System.out.format("%nYou entered: %s%n", inputStr);
				System.out.format("Do you really want to exit (Y/y):%n");
				inputStr = mCmndIn.nextLine();
				if (inputStr.equalsIgnoreCase("y")){
					System.out.format("%n%nSorry to see you go.  Goodbye%n");
					// Set return code to 0, which should cause the program to exit
					stateFlag = 0;
				} else {
					System.out.format("%nGreat!  Glad you are sticking around%n");
				}
			} else {
				/*
				 * Grab the desired KWIC search object
				 * For now we will always assume we want the first
				 * kwicker, even if several are available 
				 */
				localKwicker = mKwickers.get(0);
				
				if (localKwicker != null){
					// Check if index is complete yet
					if (!localKwicker.isIndexDone()){ // Index not yet complete
						System.out.format("%nOh, the file is still being indexed.  Please try again in a bit%n");
					} else { // Index is complete, check for results
						// TODO: Consider doing cleanup on input string
						localKwicker.processQueryKeyword(inputStr);
					}
				} else {
					System.out.format("%nHmm, that's weird. Couldn't find that search object%n");
				}
				
			}
			
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			
		}
		// End Try catch for Scanner actions (mCmndIn)
		
		// Send back the state code
		return stateFlag;
	}
	
	
	// Method to Execute Main User Interface State Machine
	private void launchMainControl(){
		// Define state flag (Default to file input state)
		int stateFlag = 1;
				
		
		// --------- STATE 1 --------- 
		// Implement UI State for Selecting the File to Index
		do {
			
			try {
				// Execute main UI code for File selection state
				stateFlag = uiFileStateChrome(stateFlag);
				
				// Check for immediate exit
				if (stateFlag == 0){
					return;
				}
			} catch (IOException e){
				e.printStackTrace();
			}
			
		} while(stateFlag == 1);
		// -------- END STATE 1 --------  
		
		
		// ----- Create new KWIC search object and start indexing a file ----
		mKwickers.add(new KwicSearch(mFileName));
		// Start Indexing process on newest search object
		mKwickers.get(mKwickers.size()-1).startIndexWorker();
		
		
		// ---------- STATE 2 ----------
		// Implement UI State for Querying Keywords in Index
		do {
			// Execute main UI code for Query state
			stateFlag = uiQueryStateChrome(stateFlag);
			
			// Check for immediate exit
			if (stateFlag == 0){
				return;
			}
			
		} while (stateFlag == 2);		
		// -------- END STATE 2 --------
		
		
		// Close Scanner
		mCmndIn.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KwicLauncher kwicker;
		
		// Create KWIC Object (with or without input argument)
		if (args.length > 0){
			kwicker = new KwicLauncher(args[0]);
		} else {
			kwicker = new KwicLauncher();
		}
					
		// Launch User Interface
		kwicker.launchMainControl();
	}

}
