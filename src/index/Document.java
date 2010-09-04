package index;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Contains header and metadata information for the indexing time of a document.  Used for quick 
 * information and to determine if re-indexing is required, i.e. the file has been modified. 
 * @author William Howard
 */
public class Document {
	/** Used to determine if a file has been modified */
	private long last_modified = 0;
	
	/** Used to determine if the serialized and main memory form of this docheader are out of sync */
	private boolean modified = false;
	
	/** Handle to the document file */
	private File file = null;
	
	/** Handle to the serialization file */
	private File header = null;
	
	/**
	 * Construct a document header around a specified document file
	 * @param f the document file
	 * @throws IllegalArgumentException the document file is not readable
	 * @throws FileNotFoundException the document file could not be found
	 */
	public Document(File f) throws IllegalArgumentException, FileNotFoundException {
		this(f,null);
	}
	
	/**
	 * Construct a document header around a specified document file and a header
	 * @param f the document file
	 * @param header the file to serialize data to and from
	 * @throws IllegalArgumentException the document file is not readable 
	 * @throws FileNotFoundException the document file could not be found
	 */
	public Document(File f, File header) throws IllegalArgumentException, FileNotFoundException {
		this.file = f;
		this.header = header;
		
		//Make sure the file exists
		if(!this.file.exists())
			throw new FileNotFoundException("Document file not found.");
		
		//Make sure the file is readable
		if(!this.file.canRead())
			throw new IllegalArgumentException("Document permissions disallow reading.");
		
		//Check if the file has not been indexed
		if(this.header == null) {
			this.last_modified = this.file.lastModified();
		} else {
			this.load();
		}
	}
	
	/** Load from serialized form the index header data.  The file loaded is the last one provided. */
	public void load() {
		this.load(this.header);
	}
	
	/**
	 * Load serialized document index header data from a file.
	 * @param header the header file
	 */
	public void load(File header) {
		//TODO: Implement Loading
	}
	
	/** Serialize document index header data to the last file provided. */
	public void serialize() {
		this.serialize(this.header);
	}
	
	/**
	 * Serialize document index header data to a file.
	 * @param header file to serialize to
	 */
	public void serialize(File header) {
		//TODO: Implement Serialization
	}
	
	/**
	 * Compares the current file against the index data and determines if this 
	 * file needs to be reindexed.  (Because of modification)
	 */
	public boolean requiresIndex() {
		return this.file.lastModified() == this.last_modified;
	}
	
	/** Returns the file object this document is associated with. */
	public File getFile() {
		return this.file;
	}
	
	/**
	 * Returns true if the serialized form is inconsitent with the form in main memory. 
	 */
	public boolean isModified() {
		return this.modified;
	}
	
	@Override
	public String toString() {
		return this.file.getName();
	}
}
