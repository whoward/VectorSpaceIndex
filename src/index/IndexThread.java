package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to perform indexing on a file on a different thread.  When the thread has completed it will
 * notify the InvertedIndex it belongs to that it has completed indexing.
 * @author William Howard
 */
public class IndexThread extends Thread {

	/** the id of this index thread */
	private int id = 0;
	
	/** the file to index */
	private File file = null;
	
	/** buffered file reader for the document */
	private BufferedReader in = null;
	
	/** the owner of this thread */
	private InvertedIndex owner = null;
	
	/** the frequency of terms in this document as parsed */
	private Map<String,Integer> freq = null;
	
	/** the progress of this thread for indexing */
	private float progress = 0.0f;
	
	/** indicates if this thread is currently indexing a file */
	private boolean idle = true;
	
	/** Instantiate this index thread with a reference to its owner */
	public IndexThread(InvertedIndex owner, int id) {
		this.owner = owner;
		this.id = id;
	}
	
	/** Returns the ID of this thread set in the Constructor */
	public int getID() {
		return this.id;
	}
	
	/** Returns the file this thread is indexing */
	public File getFile() {
		return this.file;
	}
	
	/** Returns if this thread is currently indexing a file */
	public boolean isIdle() {
		return this.idle;
	}
	
	/** Sets this thread to idle, empties the collected frequencies and file references */
	public void setIdle() {
		this.interrupt();
		
		try {
			this.in.close();
		} catch(IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		this.freq = null;
		this.file = null;
		this.progress = 0.0f;
		this.idle = true;
	}
	
	/** returns the progress of this thread */
	public float progress() {
		return this.progress;
	}
	
	/**
	 * Sets this thread to index the file supplied
	 * @param file the document to index
	 * @throws IllegalArgumentException the document is not readable
	 * @throws FileNotFoundException the document couldn't be found
	 */
	public void index(File file) throws IllegalArgumentException, FileNotFoundException {
		if(!this.isIdle())
			this.setIdle();
		
		this.file = file;
		
		if(!this.file.exists())
			throw new FileNotFoundException("Document could not be found");
		
		if(!this.file.canRead())
			throw new IllegalArgumentException("Cannot read from document");
		
		this.in = new BufferedReader(new FileReader(this.file));
		
		this.idle = false;
		
		this.run();
	}
	
	/** Returns the frequency of terms in the indexed file */
	public Map<String,Integer> getTermFrequency() {
		return Collections.unmodifiableMap(this.freq);
	}
	
	/** Starts indexing the file, should not be called externally */
	@Override
	public void run() {
		this.freq = new HashMap<String,Integer>();
		
		long sz = this.file.length();
		long numchars = 0;
		int eol_sz = System.getProperty("line.separator").length();
		
		try {
			String line = "";
			while((line = in.readLine()) != null) {
				//Increment the number of characters read
				numchars += line.length() + eol_sz; 
					
				//Force all terms to lower case
				line = line.toLowerCase();
				
				//Parse the line
				String[] terms = line.split("\\W+");
				
				for(String s : terms) {
					Integer f = this.freq.get(s);
					
					if(f == null)
						f = new Integer(0);
					
					this.freq.put(s, f+1);
				}
				
				//Recalculate the progress
				this.progress = ((float)numchars/(float)sz);
			}
			in.close();
		} catch(IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		//Set the progress to complete
		this.progress = 100.00f;
		
		//Notify the inverted index that indexing has completed
		this.owner.notifyCompletion(this);
	}
}
