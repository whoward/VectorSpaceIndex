package index;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Contains a mapping of documents to term frequency.  Since PostingLists can grow to be of very large
 * size it is useful to be able to serialize the data to secondary memory when the data is not needed.  In future
 * implementations it would also be useful to compress serialized data to reduce overall I/O bandwidth.</p>
 * 
 * <p>In better implementations the effective size of each mapping could be reduced from ~40-bytes to 6-bytes through
 * the use of a c++ struct defined as:</p>
 * <code>
 * struct entry
 * {
 * 		uint id;
 * 		ushort value;
 * };
 * </code>
 * @author William Howard
 */
public class PostingList {
	/** File to serialize and load from **/
	private File file = null;
	
	/** Mapping of the document id to the term frequency */
	public Map<Integer,Integer> freq = null;
	
	/** Used to determine if the serialized and main memory form of this posting list are out of sync */
	private boolean modified = false;
	
	/** Forces the posting list to read its data from the serialized form */
	public void load() {
		//TODO: Implement Load
	}
	
	/** Causes the posting list to free much of its allocated memory by deleting the map. */
	public void free() {
		this.freq = null;
	}
	
	/** Forces the posting list to write its contents to file */
	public void serialize() {
		this.serialize(this.file);
	}
	
	/** Forces the posting list to write its contents to file */
	public void serialize(File to) {
		//TODO: Implement Serialize
	}
	
	/** For internal use.  Do not be called externally. */
	public void addFrequency(int val, int doc_id) {
		if(this.freq == null) {
			//TODO: check if serialization exists, if so load from hdd
			this.freq = new HashMap<Integer,Integer>();
		}
		
		Integer entry = this.freq.get(doc_id);
		
		if(entry == null)
			entry = new Integer(0);
		
		entry = Math.max( entry + val, 1 );
		this.freq.put(doc_id, entry);
	}
	
	/** Returns the length of the posting list. */
	public int size() {
		return this.freq.size();
	}
	
	/** Returns true if the serialized form is inconsistent with the main memory instance */
	public boolean isModified() {
		return this.modified;
	}
	
	@Override
	public String toString() {
		return this.freq.toString();
	}
}
