package kwic;

/**
 * This class holds the program constants
 * 
 * @author Michael Chalson
 *
 */
public final class KwicConstants {
	// Define the max number of keyword instances we will hold onto
	protected static final int MAX_KEY_COUNT = 10;
	// Define the max number of keyword instances we will display
	protected static final int MAX_KEY_DISPLAY = 10;
	// Define the number of leading and trailing characters that 
	// make up the "context" of the keyword
	protected static final int CONTEXT_SIZE = 30;
	// Define the max number of lines to search, just to be safe
	protected static final long MAX_TEXT_LINES = 10000000;

}
