package index;

import generic.Tuple2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * The inverted index much more efficiently stores the indexed data for later
 * retrieval.  This forms the centre of the project and should be the only class
 * that needs to be instantiated.  All useful functions can be accessed through this
 * class.
 * @author William Howard
 */
public class InvertedIndex {	
	/** Contains the global data for an indexed term */
	private Map<String,TermData> index = new HashMap<String,TermData>();
	
	/** Contains information about documents in a table form */
	private List<Document> docTab = new Vector<Document>();
	
	/** A queue for the files to be indexed */
	private Queue<File> indexQueue = new LinkedList<File>();
	
	/** The threads for indexing */
	private IndexThread[] threads = null;
	private int firstIdle = 0;
	
	/** The event listeners */
	private List<IndexListener> listeners = new Vector<IndexListener>();
	
	/** Constructs the inverted index with 10 threads. */
	public InvertedIndex() {
		this(10);
	}
	
	/** Constructs the inverted index with a specified number of threads */
	public InvertedIndex(int threads) {
		if(threads < 1)
			throw new IllegalArgumentException("Threads must not be < 1");
		
		this.threads = new IndexThread[threads];
		for(int i = 0; i < threads; i++)
			this.threads[i] = new IndexThread(this,i);
	}
	
	/**
	 * Query for a list of relevant files.  If a term appears in a query but not in
	 * the document collection it will be ignored.
	 * @param s the query string
	 * @param max_results the maximum number of results to return
	 * @return a list of files matching the query
	 */
	public SortedSet<QueryResult> query(String s, int max_results) {
		//Since the IndexThread converts every term to lower case so shall we
		s = s.toLowerCase();
		
		//Create a list of files to return
		SortedSet<QueryResult> retval = new TreeSet<QueryResult>();
			
		//Split the terms of the query by the non-word regular expression class
		String[] split = s.split("\\W+");
		
		//This implementation considers the Query document vector a binary vector
		//in other words - duplicates are not allowed
		Set<String> terms = new HashSet<String>();
		for(String str : split)
			terms.add(str);
		
		//For every document in the collection calculate the similarity coefficient
		for(int i = 0; i < this.docTab.size(); i++) {
			double sc = 0.0;
			for(String term : terms) {
				TermData td = index.get(term);
				
				if(td != null)
					sc += td.getIDF() * td.getIDF() * td.getFreq(i);
			}
			
			File f = this.docTab.get(i).getFile();
			if(sc > 0)
				retval.add(new QueryResult(sc,f));
			
			if(retval.size() > max_results)
				retval.remove(retval.last());
		}
		
		return retval;
	}
	
	/**
	 * Forces the index to scan all of its indexed files.  If some files have been
	 * modified or removed then adjust the index appropriately.
	 */
	public void forceUpdate() {
		//TODO: Implement
	}

	/**
 	 * Adds a file to the queue for indexing
	 * @param f the file to be indexed
	 * @throws IllegalArgumentException the file is not readable
	 * @throws FileNotFoundException the file is not found
	 */
	public void index(File f) throws IllegalArgumentException, FileNotFoundException {
		//Make sure this file is not part of the collection already
		for(Document d : this.docTab) {
			if(d.getFile().equals(f))
				return;
		}
		
		//If there is an idle thread assign it this file
		if(this.firstIdle < this.threads.length) {
			//Set the first idle thread to index the file
			this.threads[firstIdle].index(f);
			
			//Find the next idle thread and set the pointer to it
			for(int i = firstIdle; i <= threads.length; i++, firstIdle++)
				if(i != threads.length && threads[i].isIdle())
					break;
		} else {	//If no idle thread could be found add it to the queue
			this.indexQueue.add(f);
			this.fireQueueChangeEvent();
		}
	}

	/**
	 * Adds a collection of files to the queue for indexing.  If an exception occurs
	 * there is no guarantee of the operation being atomic, in other words some 
	 * files will be added and some will not.
	 * @param f the files to index
	 * @throws IllegalArgumentException if a file was not readable
	 * @throws FileNotFoundException if a file could not be found
	 */
	public void indexAll(Collection<File> f) throws IllegalArgumentException, FileNotFoundException {
		for(File doc : f)
			this.index(doc);
	}
	
	/**
	 * <p>For internal use, notifies completion of indexing.</p>
	 * 
	 * <p>Must be synchronized as several threads may access this
	 * function concurrently.  Since Java collections objects should
	 * not be accessed concurrently synchronized behaviour is required.</p> 
	 * @param t the thread that has completed
	 */
	public synchronized void notifyCompletion(IndexThread t) {
		//Retrieve the values of this thread
		File f = t.getFile();
		Map<String,Integer> freq = t.getTermFrequency();
		
		//Add the document to the document table
		try {
			this.docTab.add(new Document(f));
		} catch(Exception e) {
			System.err.println("File:'" + t.getFile().getName() + "' - " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//Update the collection size so that idf can be calculated correctly
		for(TermData td : this.index.values())
			td.setDocumentCollectionSize(this.docTab.size());
		
		//Fire off collection size listeners
		this.fireCollectionChangeEvent();
		
		//Retrieve the document identifier
		int docId = this.docTab.size() - 1;
		
		//Merge the Terms into the posting list
		for(Map.Entry<String, Integer> e : freq.entrySet()) {
			//Get the term from the index map
			TermData td = this.index.get(e.getKey());
			
			//If the term was not found create and add it to the index
			if(td == null) {
				td = new TermData(this.docTab.size());
				this.index.put(e.getKey(), td);
				this.fireTermChangeEvent();
			}
			
			//Add the frequency to the posting list
			td.addFrequency(e.getValue(), docId);
		}			
		
		//Set this thread to idle
		t.setIdle();
		
		//If the queue is not empty poll it and start indexing again.
		//Otherwise, check the firstIdle pointer and set the thread to idle
		if(this.indexQueue.size() > 0) {
			try {
				t.index( indexQueue.poll() );
				this.fireQueueChangeEvent();
			} catch(Exception e) {
				System.err.println("File:'" + t.getFile().getName() + "' - " + e.getMessage());
				e.printStackTrace();
				return;
			}
		} else {
			if(this.firstIdle > t.getID())
				this.firstIdle = t.getID();			
		}
	}
	
	/** Loads the index from a file */
	public void load(File f) {
		//TODO: Implement loading
	}
	
	/** Writes the index to a file */
	public void serialize(File f) {
		//TODO: Implement serialization
	}
	
	/** Adds an index listener */
	public void addIndexListener(IndexListener l) {
		this.listeners.add(l);
	}
	
	/** Removes an index listener */
	public void removeIndexListener(IndexListener l) {
		this.listeners.remove(l);
	}
	
	/** Convenience function for firing off collection size change events */
	private void fireCollectionChangeEvent() {
		for(IndexListener l : this.listeners)
			l.sizeChanged(IndexListener.szTypes.DOCUMENT_COLLECTION,this.size());
	}
	
	/** Convenience function for firing off queue size change events */
	private void fireQueueChangeEvent() {
		for(IndexListener l : this.listeners)
			l.sizeChanged(IndexListener.szTypes.FILE_QUEUE,this.indexQueue.size());		
	}
	
	/** Convenience function for firing off term size change events */
	private void fireTermChangeEvent() {
		for(IndexListener l : this.listeners)
			l.sizeChanged(IndexListener.szTypes.TERM_COLLECTION,this.index.size());		
	}	
	
	/** Returns the number of files indexed */
	public int size() {
		return docTab.size();
	}
	
	/** Returns the number of terms globally in the collection */
	public int termSize() {
		return this.index.size();
	}
	
	/** Returns the number of indexing threads this inverted index uses */
	public int getNumberThreads() {
		return this.threads.length;
	}
	
	/** Returns the current progress of each thread and the file they are indexing as a map. */
	public Map<Integer,Tuple2<Float,File>> getThreadMap() {
		Map<Integer,Tuple2<Float,File>> result = new HashMap<Integer,Tuple2<Float,File>>();
		
		for(int i = 0; i < this.threads.length; i++) {
			Tuple2<Float,File> p = new Tuple2<Float,File>();
			p.first = threads[i].progress();
			p.second = threads[i].getFile();
			
			result.put(i, p);
		}
		
		return result;
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Index:\n");
		for(Map.Entry<String, TermData> e : this.index.entrySet()) {
			b.append(e.getKey() + " - " + e.getValue().toString() + "\n");
		}
		
		b.append("\nDocument Table:\n------------------\n");
		for(int i = 0; i < this.docTab.size(); i++)
			b.append(i + "\t| " + this.docTab.get(i).toString() + "\n");
		
		return b.toString();
	}
}
