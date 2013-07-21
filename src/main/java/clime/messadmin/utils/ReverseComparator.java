package clime.messadmin.utils;

import java.util.Comparator;

/**
 * Comparator which reverse the sort order
 * @author C&eacute;drik LIME
 */
public class ReverseComparator<T> implements Comparator<T> {
	protected Comparator<T> comparator;

	/**
	 * 
	 */
	public ReverseComparator(Comparator<T> comparator) {
		super();
		this.comparator = comparator;
	}

	/** {@inheritDoc} */
	public int compare(T o1, T o2) {
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

	@Override
	public boolean equals(Object o) {
		return (o == this)
				|| (o instanceof ReverseComparator
						&& comparator.equals(((ReverseComparator) o).comparator));
	}

	@Override
	public int hashCode() {
		return comparator.hashCode() ^ 0x10000000;
	}
}
