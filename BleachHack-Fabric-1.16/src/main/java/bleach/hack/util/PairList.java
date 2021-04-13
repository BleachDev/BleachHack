package bleach.hack.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

public class PairList<L, R> extends ArrayList<MutablePair<L, R>> {

	private static final long serialVersionUID = 7053699555145167731L;

	/**
	 * @return List of all keys in this PairList.
	 */
	public List<L> getEntries() {
		List<L> list = new ArrayList<>();
		for (MutablePair<L, R> pair : this) {
			list.add(pair.getLeft());
		}

		return list;
	}

	/**
	 * @return List of all values in this PairList.
	 */
	public List<R> getValues() {
		List<R> list = new ArrayList<>();
		for (MutablePair<L, R> pair : this) {
			list.add(pair.getRight());
		}

		return list;
	}

	/**
	 * lookup for a value for a specified entry.
	 *
	 * @param entry key to lookup
	 * @return the value related to the specified entry, or null if does not exist.
	 */
	public R getValue(L entry) {
		for (MutablePair<L, R> pair : this) {
			if (entry.equals(pair.getLeft())) {
				return pair.getRight();
			}
		}

		return null;
	}

	/**
	 * Gets the first pair with the specified key.
	 *
	 * @param entry key to lookup
	 * @return the pair related to the specified entry, or null if does not exist.
	 */
	public MutablePair<L, R> getPair(L entry) {
		for (MutablePair<L, R> pair : this) {
			if (entry.equals(pair.getLeft())) {
				return pair;
			}
		}

		return null;
	}

	/**
	 * @param entry key to lookup
	 * @return if any pairs contains this key.
	 */
	public boolean containsKey(L entry) {
		if (entry == null) {
			return false;
		}

		for (MutablePair<L, R> pair : this) {
			if (entry.equals(pair.getLeft())) {
				return true;
			}
		}

		return false;
	}

}
