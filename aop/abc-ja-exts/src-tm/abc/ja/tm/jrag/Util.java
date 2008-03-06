package abc.ja.tm.jrag;

import java.util.*;

/**
 * Implements a persistent collection backed by a normal collection.
 * In the simplest case, subclasses only need to specify the backing collection
 * type and provide some methods for obtaining new instances (empty(), copy() etc),
 * as well as override all methods returning PersistentCollection<T> to return
 * the subclass type.
 * 
 * @author Pavel Avgustinov
 *
 * @param <T> The type of object stored in this collection
 */
abstract class PersistentCollection<T> implements Iterable<T>, Cloneable {
	public PersistentCollection<T> add(T o) {
		if(this.contains(o)) {
			return this;
		} else {
			PersistentCollection<T> result = this.copy();
			result.unsafeAdd(o);
			return result;
		}
	}
	
	public PersistentCollection<T> addAll(Iterable<T> o) {
		PersistentCollection<T> result = this.copy();
		result.unsafeAddAll(o);
		return result;
	}
	// methods with reasonable defaults
	public Object clone() {
		try {
			return super.clone();
		} catch(CloneNotSupportedException e) {
			throw new RuntimeException("Called clone() on unsupporting class..", e);
		}
	}
	// no clear()
	public boolean contains(Object o) {
		return getData().contains(o);
	}
	
	public boolean containsAll(Iterable<?> c) {
		for(Object o : c)
			if(!this.contains(o)) return false;
		return true;
	}
	
	public abstract PersistentCollection<T> copy();
	
	/**
	 * Method to construct an empty PersistentCollection of the dynamic receiver type.
	 * @return a new instance with no elements (NB: *not* a representative emptySet).
	 */
	public abstract PersistentCollection<T> empty();
	
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof PersistentCollection)) return false;
		PersistentCollection<T> c = (PersistentCollection<T>)o;
		return this.equals(c);
	}

	public int hashCode() {
		return this.getData().hashCode();
	}
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	protected abstract Collection<T> getData();
	
	public Iterator<T> iterator() {
		return new PersistentIterator<T>(getData().iterator());
	}
	
	public PersistentCollection<T> remove(Object o) {
		if(this.contains(o)) {
			PersistentCollection<T> result = this.copy();
			this.unsafeRemove(o);
			return result;
		} else {
			return this;
		}
	}

	public PersistentCollection<T> removeAll(Iterable<?> c) {
		PersistentCollection<T> result = this.copy();
		result.unsafeRemoveAll(c);
		return result;
	}
	
	public PersistentCollection<T> retainAll(Iterable<?> c) {
		PersistentCollection<T> result = this.empty();
		for(Object o : c)
			if(this.contains(o))
				result.unsafeAdd((T)o);
		return result;
	}

	public int size() {
		return getData().size();
	}

	public Object[] toArray() {
		Object[] result = new Object[size()];
		int i = 0;
		for(T o : this)
			result[i++] = o;
		return result;
	}

	public abstract Collection<T> toCollection();
	
	// UNSAFE methods
	protected boolean unsafeAdd(T o) {
		return getData().add(o);
	}
	
	protected boolean unsafeAddAll(Iterable<T> c) {
		boolean result = true;
		for(T o : c)
			result &= unsafeAdd(o);
		return result;
	}

	protected boolean unsafeRemove(Object o) {
		return getData().remove(o);
	}

	protected boolean unsafeRemoveAll(Iterable<?> c) {
		boolean result = true;
		for(Object o : c)
			result &= unsafeRemove(o);
		return result;
	}
	
	static class PersistentIterator<T> implements Iterator<T> {
		private Iterator<T> it;
		
		public PersistentIterator(Iterator<T> it) {
			this.it = it;
		}
		
		public boolean hasNext() {
			return it.hasNext();
		}

		public T next() {
			return it.next();
		}

		public void remove() {
			throw new RuntimeException("Attempt to remove PersistentCollection");
		}
		
	}
}

class PersistentSet<T> extends PersistentCollection<T> {
	private HashSet<T> data;
	
	public PersistentSet() {
		data = new HashSet<T>();
	}
	
	public PersistentSet(Iterable<T> c) {
		this();
		unsafeAddAll(c);
	}
	
	protected PersistentSet(Iterable<T> c, T o) {
		this(c);
		unsafeAdd(o);
	}
	
	private PersistentSet(Collection<T> c) {
		data = new HashSet<T>(c);
	}
	
	public PersistentSet<T> add(T o) {
		return (PersistentSet<T>)super.add(o);
	}

	@Override
	public PersistentSet<T> addAll(Iterable<T> o) {
		return (PersistentSet<T>)super.addAll(o);
	}

	@Override
	public PersistentSet<T> copy() {
		return new PersistentSet<T>(data);
	}

	public PersistentSet<T> difference(PersistentSet<? extends T> c) {
		return this.removeAll(c);
	}
	
	@Override
	public PersistentSet<T> empty() {
		return new PersistentSet<T>();
	}

	@Override
	protected Collection<T> getData() {
		return data;
	}

	public PersistentSet<T> intersect(PersistentSet<? extends T> c) {
		return this.retainAll(c);
	}
	
	public PersistentSet<T> remove(Object o) {
		return (PersistentSet<T>)super.remove(o);
	}

	@Override
	public PersistentSet<T> removeAll(Iterable<?> c) {
		return (PersistentSet<T>)super.removeAll(c);
	}

	@Override
	public PersistentSet<T> retainAll(Iterable<?> c) {
		return (PersistentSet<T>)super.retainAll(c);
	}

	public Collection<T> toCollection() {
		return new HashSet<T>(data);
	}
	
	public PersistentSet<T> union(PersistentSet<? extends T> c) {
		return this.addAll((PersistentSet<T>)c);
	}
}

class PersistentList<T> extends PersistentCollection<T> {
	private ArrayList<T> data;
	
	public PersistentList() {
		data = new ArrayList<T>();
	}
	
	public PersistentList(Iterable<T> c) {
		this();
		unsafeAddAll(c);
	}
	
	public PersistentList(Collection<T> c) {
		data = new ArrayList<T>(c);
	}
	
	@Override
	public PersistentList<T> copy() {
		return new PersistentList<T>(data);
	}

	@Override
	public PersistentList<T> empty() {
		return new PersistentList<T>();
	}

	@Override
	protected Collection<T> getData() {
		return data;
	}

	@Override
	public Collection<T> toCollection() {
		return new ArrayList<T>(data);
	}

	@Override
	public PersistentList<T> add(T o) {
		return (PersistentList<T>)super.add(o);
	}

	@Override
	public PersistentList<T> addAll(Iterable<T> o) {
		return (PersistentList<T>)super.addAll(o);
	}

	@Override
	public PersistentList<T> remove(Object o) {
		return (PersistentList<T>)super.remove(o);
	}

	@Override
	public PersistentList<T> removeAll(Iterable<?> c) {
		return (PersistentList<T>)super.removeAll(c);
	}

	@Override
	public PersistentList<T> retainAll(Iterable<?> c) {
		return (PersistentList<T>)super.retainAll(c);
	}
	
}
