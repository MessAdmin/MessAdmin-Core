package clime.messadmin.utils;

import java.util.Comparator;

/**
 * Comparator which reverse the sort order
 * @author C&eacute;drik LIME
 */
public class ReverseComparator implements Comparator {
	protected Comparator comparator;

	/**
	 * 
	 */
	public ReverseComparator(Comparator comparator) {
		super();
		this.comparator = comparator;
	}

	/** {@inheritDoc}
	 */
	public int compare(Object o1, Object o2) {
		if (comparator != null) {
			return comparator.compare(o2, o1);
		} else if (o2 instanceof Comparable) {
			return ((Comparable)o2).compareTo(o1);
		} else if (o1 instanceof Comparable) {
			return - ((Comparable)o1).compareTo(o2);
		} else {
			throw new ClassCastException();
		}
	}
}
