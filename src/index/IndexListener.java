package index;

/**
 * Interface for the listener that is updated as sizes of index items change
 * @author William Howard
 */
public interface IndexListener {
	public static enum szTypes {TERM_COLLECTION,DOCUMENT_COLLECTION,FILE_QUEUE};
	
	/** Occurs when the a size of something in the index has changed */
	public void sizeChanged(szTypes item, int new_size);
	
}
