
package com.tomasolo.sim.Algorithm.Main;

import com.tomasolo.sim.Algorithm.Instruction.Instruction;
import com.tomasolo.sim.Algorithm.MemoryAndBuffer.Memory;
import com.tomasolo.sim.Algorithm.MemoryAndBuffer.RegFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Rob implements Iterable<RobNode> {
	private static RobNode first;    // beginning of queue
	private static RobNode last;     // end of queue
	private static int n;  // number of elements on queue
	private static int last_index = -1;


	/**
	 * Initializes an empty queue.
	 */
	public Rob() {
		first = null;
		last = null;
		n = 0;
	}

	/**
	 * Returns true if this queue is empty.
	 *
	 * @return {@code true} if this queue is empty; {@code false} otherwise
	 */
	public boolean isEmpty() {
		return first == null;
	}

	/**
	 * Returns the number of items in this queue.
	 *
	 * @return the number of items in this queue
	 */
	public int size() {
		return n;
	}

	public boolean check() {
		return n <= 6;
	}

	/**
	 * Returns the item least recently added to this queue.
	 *
	 * @return the item least recently added to this queue
	 * @throws NoSuchElementException if this queue is empty
	 */
	public RobNode peek() {
		if (isEmpty()) throw new NoSuchElementException("Queue underflow");
		return first;
	}

	/**
	 * Adds the item to this queue.
	 */
	public int enqueue(Instruction inst) {
		if (inst.getName().equals(Instruction.JMP) || inst.getName().equals(Instruction.BEQ) || inst.getName().equals(Instruction.RET)) {
			RobNode oldlast = last;
			last = new RobNode();
			last.dest = 100;
			last.index = ++last_index;
			last.previous = oldlast;
			last.type = inst.getName();
			if (isEmpty()) first = last;
			else oldlast.next = last;
			n++; //increment size
			return last_index;
		} else if (inst.getName().equals(Instruction.JALR)) {
			RobNode oldlast = last;
			last = new RobNode();
			last.dest = inst.getRegA();
			last.index = ++last_index;
			last.previous = oldlast;
			last.type = inst.getName();
			if (isEmpty()) first = last;
			else oldlast.next = last;
			n++; //increment size
			return last_index;
		} else if (inst.getName().equals(Instruction.SW)) {
			RobNode oldlast = last;
			last = new RobNode();
			last.dest = 101;
			last.index = ++last_index;
			last.previous = oldlast;
			last.type = inst.getName();
			if (isEmpty()) first = last;
			else oldlast.next = last;
			n++; //increment size
			return last_index;
		} else {

			RobNode oldlast = last;
			last = new RobNode();
			last.dest = inst.getRegA();
			last.index = ++last_index;
			last.previous = oldlast;
			last.type = inst.getName();
			if (isEmpty()) first = last;
			else oldlast.next = last;
			n++; //increment size
			return last_index;

		}

	}

	/**
	 * Removes and returns the item on this queue that was least recently added.
	 *
	 * @return the item on this queue that was least recently added
	 * @throws NoSuchElementException if this queue is empty
	 */
	public RobNode dequeue() {
		if (isEmpty()) throw new NoSuchElementException("Queue underflow");
		RobNode item = first;
		first = first.next;
		n--;
		if (isEmpty()) last = null;   // to avoid loitering
		return item;
	}

	public int find_dest(int reg, int inst_indx) {
		//System.out.println("REG" + reg);
		RobNode current = first;
		while (current != null) {
			RobNode item = current;
			current = current.next;
			if (item.dest == reg && item.index != inst_indx) {
				return item.index;
			}
		}
		return -1;
	}

	public boolean is_ready(int indx) {
		RobNode current = first;
		while (current != null) {
			if (current.index == indx)
				return current.ready;
			current = current.next;
		}
		System.out.println("Rob Error");
		return false;

	}

	public Integer get_value(int indx) {
		RobNode current = first;
		while (current != null) {
			if (current.index == indx)
				return current.value;
			current = current.next;
		}
		System.out.println("Rob ERROR");
		return null;
	}

	public boolean set_value(int indx, Integer value, Integer jalrvalue) {

		RobNode current = first;
		while (current != null) {
			if (current.index == indx) {
				if (current.type.equals(Instruction.JALR) || current.type.equals(Instruction.SW))
					current.jalr_value2 = jalrvalue;


				current.value = value;
				current.ready = true;
				return true;
			}
			current = current.next;
		}
		return false;
	}

	/**
	 * Returns a string representation of this queue.
	 *
	 * @return the sequence of items in FIFO order, separated by spaces
	 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		RobNode pointer = first;
		while (pointer != null) {
			String item = pointer.type;
			s.append(item);
			item = String.valueOf(pointer.dest);
			s.append(item);
			s.append(' ');
			pointer = pointer.next;
		}

		return s.toString();
	}

	public Integer commit(Memory mem) {
		Integer PC = null;
		if (first.ready) {
			if (first.type.equals(Instruction.JMP) || first.type.equals(Instruction.RET))
				PC = first.value;
			else if (first.type.equals(Instruction.BEQ)) {
				if (first.value != null) {
					PC = first.value;
				}

			} else if (first.type.equals(Instruction.JALR)) {
				PC = first.jalr_value2;
				RegFile.write(first.dest, first.value);
			} else if (first.type.equals(Instruction.SW)) {
				try {
					mem.write(first.jalr_value2, first.value);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else
				RegFile.write(first.dest, first.value);


			first = first.next;
			n--;
		}
		return PC;
	}

	void flush() {
		first = last.next;
		last = first;


	}

	public ArrayList<RobNode> asList() {
		ArrayList<RobNode> list = new ArrayList<>();
		RobNode cur = first;
		while (cur != null && cur.next != null) {
			list.add(cur);
			cur = cur.next;
		}
		return list;
	}

	public Iterator<RobNode> iterator() {
		return new ListIterator(first);
		/*
		ِArrayList<RobNode> list;
		RobNode cur = first;
		while(cur.next != null) {
			list.add(cur);
			cur = cur.next;
		}
		return list;*/
	}
}