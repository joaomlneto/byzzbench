package byzzbench.simulator.protocols.pbft;

import java.io.Serializable;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * A set of type T. Type T must have a "String id()" method which is used
 * as the set's key. Furthermore, the returned identifiers must be within the range
 * 0 <= i < sz (where sz is the maximum number of elements in the set).
 *
 * @param <T> the type of the elements in the set
 */
public class IdentifiableObjectsSet<T extends IdentifiableObject> implements Serializable {
    /**
     * The maximum number of elements in the set
     */
    private final SortedSet<String> validIds;

    /**
     * The elements in the set
     */
    private final SortedMap<String, T> elems = new TreeMap<>();

    /**
     * Creates a new set that can hold up to "sz" elements.
     *
     * @param validIds the set of valid identifiers
     */
    public IdentifiableObjectsSet(SortedSet<String> validIds) {
        this.validIds = validIds;
    }

    /**
     * Adds "e" to the set (if it has a valid id not in the set) and returns true.
     * Otherwise, returns false and does nothing.
     *
     * @param e the element to add
     * @return true if the element was added, false otherwise
     */
    public boolean store(T e) {
        String id = e.id();
        if (elems.containsKey(id) || !validIds.contains(id)) {
            return false;
        }
        elems.put(id, e);
        return true;
    }

    /**
     * Returns the element in the set with identifier "id", or null if no such element exists.
     *
     * @param id the identifier of the element to fetch
     * @return the element with identifier "id", or empty if no such element exists
     */
    public Optional<T> fetch(String id) {
        return Optional.ofNullable(elems.get(id));
    }

    /**
     * Removes the element with identifier "id" from the set and returns it.
     *
     * @param id the identifier of the element to remove
     * @return the element with identifier "id", or null if no such element exists
     */
    public Optional<T> remove(String id) {
        return Optional.ofNullable(elems.remove(id));
    }

    /**
     * Removes all elements from the set and deletes them.
     */
    public void clear() {
        elems.clear();
    }

    /**
     * Returns the number of elements in the set.
     *
     * @return the number of elements in the set
     */
    int size() {
        return elems.size();
    }
}
