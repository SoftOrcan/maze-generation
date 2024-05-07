import java.util.ArrayDeque;
import java.util.Deque;
import tester.*;

/**
 * Represents a mutable collection of items
 * @param <T> some item type
 */
interface ICollection<T> {

  /**
   * Is this collection empty?
   * @return boolean representing if the collection is empty
   */
  boolean isEmpty();

  /**
   * Add item to the list
   * EFFECT: adds the item to the collection
   * @param item to add to the list
   */
  void add(T item);

  /**
   * Remove the first item of the collection
   * EFFECT: removes that first item from the collection
   * @return T the first item in the collection
   */
  T remove();

  /**
   * Return the first item of the collection
   * @return T the first item in the collection
   */
  T nextUp();
}

/**
 * Represents a stack of items
 * @param <T> some item type
 */
class Stack<T> implements ICollection<T> {
  Deque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  /**
   * Is this collection empty?
   * @return boolean representing if the stack is empty
   */
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  /**
   * Remove the first item of the stack
   * EFFECT: removes that first item from the stack
   * @return T the first item in the stack
   */
  public T remove() {
    return this.contents.removeFirst();
  }

  /**
   * Add item to the list
   * EFFECT: adds the item to the stack
   * @param item to add to the list
   */
  public void add(T item) {
    this.contents.addFirst(item);
  }

  /**
   * Return the first item of the stack
   * @return T the first item in the stack
   */
  public T nextUp() {
    return this.contents.getFirst();
  }
}

/**
 * Represents a queue of items
 * @param <T> some item type
 */
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<>();
  }

  /**
   * Is this queue empty?
   * @return boolean representing if the queue is empty
   */
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  /**
   * Remove the first item of the queue
   * EFFECT: removes that first item from the queue
   * @return T the first item in the queue
   */
  public T remove() {
    return this.contents.removeFirst();
  }

  /**
   * Add item to the list
   * EFFECT: adds the item to the queue
   * @param item to add to the list
   */
  public void add(T item) {
    this.contents.addLast(item);
  }

  /**
   * Return the first item of the queue
   * @return T the first item in the queue
   */
  public T nextUp() {
    return this.contents.getFirst();
  }
}

class ExamplesCollection {

  boolean testIsEmpty(Tester t) {
    // This simultaneously tests the behavior of add as well
    // Though we also test to make sure it adds to the right place (front or back)
    ICollection<Integer> stack1 = new Stack<>();
    ICollection<Integer> stack2 = new Stack<>();
    ICollection<String> queue1 = new Queue<>();
    ICollection<String> queue2 = new Queue<>();
    stack1.add(2);
    queue1.add("A");
    return t.checkExpect(stack1.isEmpty(), false)
              && t.checkExpect(stack2.isEmpty(), true)
              && t.checkExpect(queue1.isEmpty(), false)
              && t.checkExpect(queue2.isEmpty(), true);
  }

  boolean testRemove(Tester t) {
    ICollection<Integer> stack1 = new Stack<>();
    ICollection<String> queue1 = new Queue<>();
    stack1.add(2);
    queue1.add("A");
    return t.checkExpect(stack1.remove(), 2)
            && t.checkExpect(stack1.isEmpty(), true)
            && t.checkExpect(queue1.remove(), "A")
            && t.checkExpect(queue1.isEmpty(), true);
  }

  boolean testNextUp(Tester t) {
    ICollection<Integer> stack1 = new Stack<>();
    ICollection<String> queue1 = new Queue<>();
    stack1.add(2);
    queue1.add("A");
    return t.checkExpect(stack1.nextUp(), 2)
            && t.checkExpect(stack1.isEmpty(), false)
            && t.checkExpect(queue1.nextUp(), "A")
            && t.checkExpect(queue1.isEmpty(), false);
  }

  boolean testAdd(Tester t) {
    // Since we tested nextUp, this is a valid test for add.
    // Ensure the position the data is being added to in the Deque is correct.
    ICollection<Integer> stack1 = new Stack<>();
    ICollection<String> queue1 = new Queue<>();
    stack1.add(2);
    stack1.add(3);
    queue1.add("A");
    queue1.add("B");
    return t.checkExpect(stack1.nextUp(), 3)
            && t.checkExpect(queue1.nextUp(), "A");
  }
}