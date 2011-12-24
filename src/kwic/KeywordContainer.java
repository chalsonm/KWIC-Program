package kwic;

import java.util.ArrayList;
import java.util.List;

/** Keyword summary container
 * This is a helper class to hold the info on all discovered instances
 * of a given keyword
 * 
 * @author Michael Chalson
 */

public class KeywordContainer {
	// Counter of instances found for this keyword
	private int wordCount;
	// List of Keyword instances with details
	private List<KeywordData> detailList = new ArrayList<KeywordData>();
	
	// Basic Constructor
	protected KeywordContainer(){
		this.wordCount = 0;
	}
	
	// Constructor that sets wordCount and adds to detailList
	protected KeywordContainer(int count,KeywordData details){
		this.wordCount = count;
		detailList.add(details);
	}
	
	// Attempt to add another instance to the list
	protected void addKeywordDetailAttempt(KeywordData wordData){
		// Stub for future enhancements
	}
	
	// getter for wordCount
	protected int getWordCount(){
		return wordCount;
	}
	
	// setter for wordCount
	protected void setWordCount(int count){
		this.wordCount = count;
	}
	
	// incrementer for wordCount (probably more convenient than using setter)
	protected void incrementWordCount(){
		wordCount++;
	}
	
	// getter for detailList
	protected List<KeywordData> getDetailList(){
		return detailList;
	}
}
