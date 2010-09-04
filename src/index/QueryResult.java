package index;

import java.io.File;

/**
 * A comparable class containing a pairing of the similarity of a result to the value (file) of a result
 * @author William Howard
 */
public class QueryResult implements Comparable<QueryResult> {

	public double similarity = 0.0;
	public File file = null;
	
	public QueryResult(double sim, File file) {
		this.similarity = sim;
		this.file = file;
	}
	
	public int compareTo(QueryResult rhs) {		
		return -Double.compare(this.similarity, rhs.similarity);
	}

}
