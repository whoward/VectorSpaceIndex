package index;

/**
 * Structure for containing data about a term.
 * @author William Howard
 */
public class TermData {
	/** the number of documents in a collection, used to calculate idf **/
	private int doc_sz = 0;
	
	/** the inverse document frequency is used to quickly determine the weight of a query term **/
	private float idf = 0.0f;
	
	/** Contains a mapping of documents to frequency for this term, may not always be in primary memory **/
	private PostingList list = new PostingList();
	
	/**
	 * Constructs the term data with a specified collection size
	 * @param collection_sz
	 */
	public TermData(int collection_sz) {
		this.doc_sz = collection_sz;
	}
		
	/**
	 * Sets the size of the document collection, changes to this will require recalculation of the idf.
	 * @param size the new size
	 */
	public void setDocumentCollectionSize(int size) {
		this.doc_sz = size;
		this.calculateIDF();
	}

	/**
	 * Adds a value to the frequency of a document for this term.  
	 * The frequency will never fall lower than one.
	 * @param val the value to add
	 * @param doc_id the id of the document to add to
	 */
	public void addFrequency(int val, int doc_id) {
		this.list.addFrequency(val,doc_id);		
		this.calculateIDF();
	}
	
	/** Returns the inverse document frequency of this term. */
	public float getIDF() {
		return this.idf;
	}
	
	public int getFreq(int docid) {
		Integer i = this.list.freq.get(docid);
		return (i==null)?0:i;
	}
	
	/** Returns the posting list for this term. **/
	public PostingList getPostingList() {
		return this.list;
	}
	
	/** Calculates the inverse document frequency based on the known values. */
	private void calculateIDF() {
		this.idf = (float)Math.log10( ((float)this.doc_sz)/((float)this.list.size()) );
	}
	
	@Override
	public String toString() {
		return "idf: " + this.idf + ", " + this.list.toString();
	}
	
}
