import javalib.worldimages.Posn;
import tester.Tester;

import java.util.HashMap;

/**
 * Represents Utils functions
 */
class Utils {

  /**
   * Return the X coordinate of this Posn
   * @param location the Posn
   * @return the X coordinate of this Posn
   */
  public int getX(Posn location) {
    return location.x;
  }

  /**
   * Return the Y coordinate of this Posn
   * @param location the Posn
   * @return the Y coordinate of this Posn
   */
  public int getY(Posn location) {
    return location.y;
  }

  // todo - test
  /**
   * Identify the representative of the parent
   * @param representatives the map of representatives
   * @param parent the parent whose representative we are looking for
   * @return the value of the representative
   */
  public String find(HashMap<String, String> representatives, String parent) {
    String val = representatives.get(parent);

    // If a node name maps to itself, then it is the representative; otherwise, “follow the links”
    // in the representatives map, and recursively look up the representative for the current
    // node’s parent.
    while (!parent.equals(val)) {
      parent = val;
      val = representatives.get(val);
    }

    return val;
  }

  // todo - test
  /**
   * Make rep1's representative's representative rep2's representative
   * @param representatives the map of representatives
   * @param name1 the first representative
   * @param name2 the second representative
   */
  public void union(HashMap<String, String> representatives, String name1, String name2) {
    representatives.replace(
            find(representatives, name1),
            find(representatives, name2));
  }
}

/**
 * Examples and tests for Utils file
 */
class ExamplesUtils {
  Utils u = new Utils();
  Posn posZero = new Posn(0, 0);
  Posn posNeg = new Posn(-3, -4);
  Posn posPos = new Posn(1, 8);

  boolean testGetX(Tester t) {
    return t.checkExpect(u.getX(posZero), 0)
            && t.checkExpect(u.getX(posNeg), -3)
            && t.checkExpect(u.getX(posPos), 1);
  }

  boolean testGetY(Tester t) {
    return t.checkExpect(u.getY(posZero), 0)
            && t.checkExpect(u.getY(posNeg), -4)
            && t.checkExpect(u.getY(posPos), 8);
  }

  boolean testFind(Tester t) {
    HashMap<String, String> map = new HashMap<>();
    map.put("key", "value");
    map.put("value", "value");
    map.put("key0", "key0");
    map.put("key1", "key2");
    map.put("key2", "key3");
    map.put("key3", "key4");
    map.put("key4", "key4");
    return t.checkExpect(u.find(map, "key"), "value")
            && t.checkExpect(u.find(map, "key0"), "key0")
            && t.checkExpect(u.find(map, "key1"), "key4");
  }

  boolean testUnion(Tester t) {
    HashMap<String, String> map = new HashMap<>();
    map.put("key", "value");
    map.put("value", "value");
    map.put("key0", "key0");
    map.put("key1", "key2");
    map.put("key2", "key2");
    map.put("key3", "key4");
    map.put("key4", "key4");
    u.union(map, "key", "key0");
    u.union(map, "key4", "key1");
    return t.checkExpect(u.find(map, "key"), "key0")
            && t.checkExpect(u.find(map, "value"), "key0")
            && t.checkExpect(u.find(map, "key0"), "key0")
            && t.checkExpect(u.find(map, "key3"), "key2")
            && t.checkExpect(u.find(map, "key4"), "key2");
  }
}