package skiplist;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class SkipList<E> implements List<E> {

	private SkipListEntry<E> head;
	private SkipListEntry<E> tail;

	private int numEntries;

	private int height;

	private Random coin;

	public SkipList() {

		head = new SkipListEntry<E>();
		tail = new SkipListEntry<E>();

		head.right = tail;
		tail.left = head;

		numEntries = 0;
		height = 1;

		coin = new Random();

	}

	/*
	 * Size method for the SkipList
	 * 
	 * @return Returns the number of elements in the list.
	 * 
	 */
	@Override
	public int size() {
		return numEntries;
	}

	/*
	 * Add method for the SkipList, it will append the given element to the end
	 * of the list.
	 * 
	 * @param E e -- The only parameter is a generic element to insert.
	 * 
	 * @return Returns true if the list changed as a result of the call.
	 * 
	 * @throws IndexOutOfBoundsException.
	 */
	@Override
	public boolean add(E e) {
		add(size(), e);
		return true;
	}

	/*
	 * Add method for the SkipList, it will add the given element at the
	 * specified index to the list.
	 * 
	 * @param int index -- First parameter is an integer representing the
	 * insertion index.
	 * 
	 * @param E e -- Second parameter is the generic element to insert.
	 * 
	 * @throws IndexOutOfBoundsExceptions.
	 */
	@Override
	public void add(int index, E e) {

		if (index < 0 || index > numEntries) {
			throw new IndexOutOfBoundsException("Index out of bounds");
		}

		SkipListEntry<E> nodeToAdd = new SkipListEntry<E>(e);
		int distance = 0;
		SkipListEntry<E> p = head;
		boolean notFound = true;

		while (notFound) {

			if ((p.right.spanWidth + distance) <= index) {
				p = p.right;

				distance += p.spanWidth;
			} else if (p.down != null) {
				p = p.down;

			} else {
				notFound = false;
			}
		}
		p.right.left = nodeToAdd;
		nodeToAdd.right = p.right;
		p.right = nodeToAdd;
		nodeToAdd.left = p;
		numEntries++;
		updateIndex(nodeToAdd);
		coinFlip(index, nodeToAdd);
	}

	
	/*
	 * Returns a string representation of the elements of the SkipList.
	 *
	 *@return Returns a String representation of the elements of the SkipList.
	 */
	@Override
	public String toString() {
		SkipListEntry<E> p = head;
		String output = "[";

		while (p.down != null)
			p = p.down;

		for (int i = 0; i < numEntries; i++) {
			p = p.right;
			output += p.data;
			if (numEntries - 1 == i)
				output += "]";
			else {
				output += ", ";
			}

		}
		
		if(numEntries == 0)
			output+="]";

		return output;

	}

	
	/*
	 * Get method for the SkipList, it will access the element at the index.
	 * 
	 * @param int index -- The parameter is an integer representing the
	 * access index.
	 * 
	 * @return Returns the generic element at the access index.
	 * 
	 * @throws IndexOutOfBoundsExceptions.
	 */
	@Override
	public E get(int index) {

		if (index < 0 || index >= numEntries) {
			throw new IndexOutOfBoundsException("Index out of bounds");
		}

		boolean notFound = true;
		int distance = 0;
		SkipListEntry<E> p = head;

		while (notFound) {

			if ((p.right.spanWidth + distance) <= index + 1) {
				p = p.right;

				distance += p.spanWidth;
			} else if (p.down != null) {
				p = p.down;

			} else {
				notFound = false;
			}
		}

		return p.data;
	}
	
	/*
	 * coinFlip method
	 * Helper method that takes a recently inserted node, runs a loop that has a 50% chance each pass to run again
	 * Each time the loop runs it will insert a copy of the node and a higher tier, checks height and dynamically adds new layers if necessary.
	 */
	private void coinFlip(int index, SkipListEntry<E> node) {
		int flips = 1;
		while (coin.nextBoolean()) {

			flips++;
			SkipListEntry<E> p = node;

			while (p.up != null) {
				p = p.up;
			}

			SkipListEntry<E> copy = new SkipListEntry<E>(node.data);

			if (height < flips) {
				addLayer(index);
				copy.spanWidth = numEntries + 1 - tail.spanWidth;
				copy.down = p;
				p.up = copy;
				copy.right = tail;
				copy.left = head;
				head.right = copy;
				tail.left = copy;
			}

			else {

				spliceNode(p, copy);

			}

		}
	}

	
	/*
	 * spliceNode helper method
	 * If there is a successful coinflip and there already exists a layer above the new node, this will splice it in 
	 */
	private void spliceNode(SkipListEntry<E> fromNode, SkipListEntry<E> copy) {
		SkipListEntry<E> p = fromNode;
		SkipListEntry<E> newNode = copy;
		boolean stationary = true;
		int numLeftJumps = 0;

		while (stationary) {
			if (p.up != null) {
				p = p.up;
				stationary = false;
			} else {
				numLeftJumps += p.spanWidth;
				p = p.left;

			}
		}
		newNode.spanWidth = numLeftJumps;
		p.right.spanWidth -= numLeftJumps;
		p.right.left = newNode;
		newNode.right = p.right;
		newNode.left = p;
		newNode.down = fromNode;
		fromNode.up = newNode;
		p.right = newNode;
	}

	/*updateIndex helper method
	 * When a new node is added, this will update the span widths of subsequence nodes in upper layers.
	 */
	private void updateIndex(SkipListEntry<E> node) {

		SkipListEntry<E> p = node;
		int currentHeight = 1;

		while (currentHeight < height) {

			if (p.up != null) {
				p = p.up;
				p.right.spanWidth++;
				currentHeight++;
			} else {
				p = p.left;
			}

		}

	}
	
	/*
	 * addLayer helper method
	 * On a successful coinFlip, this will add a new layer if one does not exist above the new node.
	 */
	private void addLayer(int index) {
		SkipListEntry<E> newTail = new SkipListEntry<E>();
		SkipListEntry<E> newHead = new SkipListEntry<E>();

		/* old head/tail points up a new head/tail */

		this.head.up = newHead;
		this.tail.up = newTail;

		newTail.spanWidth = numEntries - index;

		/* new head/tail points down at old head/tail */
		newHead.down = head;
		newTail.down = tail;

		/* new tail and head point at eachother */
		newHead.right = newTail;
		newTail.left = newHead;

		/* top layer becomes new head and tail */
		this.head = newHead;
		this.tail = newTail;

		this.height++;
	}
	
	

	private static class SkipListEntry<E> {

		private E data;

		private SkipListEntry<E> up = null;

		private SkipListEntry<E> down = null;

		private SkipListEntry<E> left = null;

		private SkipListEntry<E> right = null;

		int spanWidth = 1;

		private SkipListEntry() {

		}

		private SkipListEntry(E data) {
			this.data = data;

		}

	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();

	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();

	}
}