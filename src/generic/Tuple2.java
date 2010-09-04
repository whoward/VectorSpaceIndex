package generic;

/**
 * A simple class to store two grouped variables.
 * @author William Howard
 *
 * @param <T1> the type of the first component of this tuple.
 * @param <T2> the type of the second component of this tuple.
 */
public class Tuple2<T1, T2> {
	public T1 first;
	public T2 second;
	
	@Override
	public String toString() {
		return "(" + this.first + "," + this.second + ")";
	}
}