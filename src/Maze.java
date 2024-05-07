import javalib.impworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.Color;
import java.util.*;

/**
 * Represents a square on the Maze
 */
class Vertex {
  String name; // the _unique_ name of this square
  final boolean start; // if this square is the start
  final boolean end; // if this square is the exit
  final ArrayList<Edge> adjacentEdges; // edges from this node
  Color color; // the color of this Vertex
  Vertex prev; // the previous state of this Vertex
  int distFromStart; // this Vertex's distance from start
  int distFromEnd; // this Vertex's distance from end

  Vertex(Color color, boolean start, boolean end, ArrayList<Edge> adjacentEdges) {
    this.start = start;
    this.end = end;
    this.adjacentEdges = adjacentEdges;
    this.color = color;
    this.prev = this;
    this.distFromEnd = 0;
    this.distFromStart = 0;
  }

  Vertex(int height, int width, boolean start, boolean end) {
    this.start = start;
    this.end = end;
    this.adjacentEdges = new ArrayList<>();
    this.name = "H" + height + "W" + width;
    this.prev = this;
    if (start) {
      // The start is colored in Green
      this.color = new Color(1, 90, 32);
    }
    else if (end) {
      // The exit is colored in Purple
      this.color = new Color(100, 0, 128);
    }
    else {
      // Otherwise, the default color of a Vertex is White
      this.color = Color.WHITE;
    }
    this.distFromEnd = 0;
    this.distFromStart = 0;
  }

  /**
   * Creates adjacent Edges for this Vertex
   * EFFECT: Constructs adjacent Edges for this Vertex by randomly generating weights
   * @param v the Vertex to create adjacent Edges for
   * @param ifHoriz create Edges with a horizontal or vertical preference
   * @param bias determine which bias is preferred
   */
  public void makeEdge(Vertex v, boolean ifHoriz, int bias) {
    Random rand = new Random();
    int weight = 0;

    // No bias, so generate weights for the Edges randomly
    if (bias == 0) {
      weight = rand.nextInt();
    }

    // VERT BIAS!
    if (bias == 1) {
      // At vert bias, create vertical Edges with heavier weights, and horizontal Edges with
      // lighter weights
      if (ifHoriz) {
        // Honestly, a smaller bound will also work here, but we want to ensure the colorDisplay is
        // generated correctly when creating a large Maze
        weight = rand.nextInt(20000000);
      }
      else {
        weight = rand.nextInt(3);
      }
    }

    // HORIZ BIAS!
    if (bias == -1) {
      // At horiz bias, create horizontal Edges with heavier weights, and horizontal Edges with
      // lighter weights
      if (ifHoriz) {
        weight = rand.nextInt(3);
      }
      else {
        weight = rand.nextInt(20000000);
      }
    }

    this.adjacentEdges.add(new Edge(this, v, weight));
  }

  /**
   * Render this Vertex as an image
   * @param scale the configured size of the Vertex
   * @return the rendered Vertex as a WorldImage, scaled using param "scale"
   */
  public WorldImage draw(int scale) {
    return new RectangleImage(scale, scale, OutlineMode.SOLID, this.color);
  }

  /**
   * Determine if this Vertex and the other Object are equivalent
   * @param o the other Object to compare
   * @return if this Vertex and the other Object are equivalent
   */
  @Override
  public boolean equals(Object o) {
    // If the other Object is not a Vertex, then immediately return false
    if (!(o instanceof Vertex)) {
      return false;
    }

    // Otherwise, determine equivalency using the name, start and end fields
    Vertex that = (Vertex) o;
    return (this.name.equals(that.name)) && (this.start == that.start) && (this.end == that.end);
  }

  /**
   * Alter the hashCode since equivalency has been altered
   * @return the new hashCode of this Vertex, determined by its unique name field
   */
  @Override
  public int hashCode() {
    int hash = this.name.hashCode();
    if (this.start) {
      hash *= 999;
    }
    if (this.end) {
      hash *= 999;
    }
    return hash;
  }
}

/**
 * Represents a Wall on the Maze
 */
class Edge {
  final Vertex from; // the Vertex this Edge came from
  final Vertex to; // the Vertex this Edge is pointing towards
  final int weight; // the weight of this Edge

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  /**
   * Determine if this Edge and the other Object are equivalent
   * @param o the other Object to compare
   * @return if this Edge and the other Object are equivalent
   */
  @Override
  public boolean equals(Object o) {
    // If the other Object is not a Vertex, then immediately return false
    if (!(o instanceof Edge)) {
      return false;
    }

    Edge that = (Edge) o;
    // Otherwise, determine equivalency using the from and to fields. Moreover, since the fields
    // are symmetrical, this.from can equal that.to and this.to can equal that.from
    return (this.from.equals(that.from) && (this.to.equals(that.to)))
            || (this.from.equals(that.to) && (this.to.equals(that.from)));
  }

  /**
   * Alter the hashCode since equivalency has been altered
   * @return the new hashCode of this Edge, determined by its unique name field
   */
  @Override
  public int hashCode() {
    return this.from.hashCode() * this.to.hashCode();
  }

  /**
   * Render this Edge as an image
   * @param scale the configured size of the Edge
   * @param horizontal the horizontal of the Edge, since it can be vertical or horizontal
   * @return the rendered Edge as a WorldImage, scaled using param "scale"
   */
  public WorldImage draw(int scale, boolean horizontal) {
    if (horizontal) {
      return new RectangleImage(scale, 1, OutlineMode.SOLID, Color.BLACK);
    }
    return new RectangleImage(1, scale, OutlineMode.SOLID, Color.BLACK);
  }
}

/**
 * A Maze represented as a Graph
 */
class Graph {
  final ArrayList<ArrayList<Vertex>> allVertices; // all Vertices in this Graph
  final ArrayList<Edge> paths; // the MST of this Graph
  ICollection<Vertex> worklist; // the worklist utilized in Kruskal's algorithm
  ArrayList<Vertex> seen; // the Vertices that are already seen
  final ArrayList<Vertex> solution; // the solution to the maze (only _one_ can exist)
  boolean isBFS; // if the Graph is utilizing BFS or DFS traversal
  Posn location; // the current location of the user
  int optimalMoves; // the overall most optimal moves
  int movesTakenBFS; // the total moves for the most optimal path determined by BFS
  int movesTakenDFS; // the total moves for the most optimal path determined by DFS
  int movesTakenUser; // the total moves of the user
  boolean isUserDone; // if the User has successfully traversed the Graph
  final int bias; // the bias method (e.g. vert or horiz) utilized when traversing the Graph

  Graph() {
    this.allVertices = new ArrayList<>();
    this.paths = new ArrayList<>();
    this.solution = new ArrayList<>();
    this.bias = 0;
  }

  Graph(ArrayList<ArrayList<Vertex>> allVertices, ArrayList<Edge> paths,
        ICollection<Vertex> worklist, ArrayList<Vertex> seen, boolean isBFS, Posn location) {
    this.allVertices = allVertices;
    this.paths = paths;
    this.worklist = worklist;
    this.seen = seen;
    this.isBFS = isBFS;
    this.location = location;
    this.bias = 0;

    this.solution = new ArrayList<>();
    // Modify the Solution field by calculating this Graph's most optimal path
    this.calculateOptimalPath();

    this.isUserDone = false;
    // Calculate the Vertex's distances from the Start and End
    this.getVertexDistances();
  }

  Graph(int width, int height) {
    optimalMoves = 0;
    movesTakenBFS = 0;
    movesTakenDFS = 0;
    movesTakenUser = 0;
    this.solution = new ArrayList<>();

    // Construct the field allVertices by adding every Vertex in the Grid, row by row
    ArrayList<ArrayList<Vertex>> grid = new ArrayList<>();
    for (int heightIndex = 0; heightIndex < height; heightIndex += 1) {
      ArrayList<Vertex> row = new ArrayList<>();
      for (int widthIndex = 0; widthIndex < width; widthIndex += 1) {
        // The Start is located at (0, 0), and should be indicated as so
        if (widthIndex == 0 && heightIndex == 0) {
          row.add(new Vertex(heightIndex, widthIndex, true, false));
        }
        // The Exit is located at (width - 1, height - 1), and should be indicated as so
        else if (widthIndex == width - 1 && heightIndex == height - 1) {
          row.add(new Vertex(heightIndex, widthIndex, false, true));
        }
        else {
          row.add(new Vertex(heightIndex, widthIndex, false, false));
        }
      }
      grid.add(row);
    }
    this.allVertices = grid;

    // Create Edges for every Vertex in the field allVertices
    this.setEdges(this.allVertices);

    this.paths = this.kruskalMST();
    this.worklist = new Queue<>();
    this.seen = new ArrayList<>();
    this.isBFS = true;
    this.location = new Posn(0, 0);
    this.bias = 0;
    this.isUserDone = false;

    // Modify the Solution field by calculating this Graph's most optimal path
    this.calculateOptimalPath();

    // Calculate the Vertex's distances from the Start and End
    this.getVertexDistances();
  }

  Graph(int width, int height, int bias) {
    optimalMoves = 0;
    movesTakenBFS = 0;
    movesTakenDFS = 0;
    movesTakenUser = 0;
    this.solution = new ArrayList<>();
    this.isUserDone = false;

    // Construct the field allVertices by adding every Vertex in the Grid, row by row
    ArrayList<ArrayList<Vertex>> grid = new ArrayList<>();
    for (int heightIndex = 0; heightIndex < height; heightIndex += 1) {
      ArrayList<Vertex> row = new ArrayList<>();
      for (int widthIndex = 0; widthIndex < width; widthIndex += 1) {
        // The Start is located at (0, 0), and should be indicated as so
        if (widthIndex == 0 && heightIndex == 0) {
          row.add(new Vertex(heightIndex, widthIndex, true, false));
        }
        // The Exit is located at (width - 1, height - 1), and should be indicated as so
        else if (widthIndex == width - 1 && heightIndex == height - 1) {
          row.add(new Vertex(heightIndex, widthIndex, false, true));
        }
        else {
          row.add(new Vertex(heightIndex, widthIndex, false, false));
        }
      }
      grid.add(row);
    }
    this.allVertices = grid;

    // Create Edges for every Vertex in the field allVertices
    this.setEdges(this.allVertices);

    this.paths = this.kruskalMST();
    this.worklist = new Queue<>();
    this.seen = new ArrayList<>();
    this.isBFS = true;
    this.location = new Posn(0, 0);

    // Ensure that the bias is either -1, 0 or 1
    if (bias > 1 || bias < -1) {
      throw new IllegalArgumentException("Invalid bias!");
    }

    this.bias = bias;

    // Modify the Solution field by calculating this Graph's most optimal path
    this.calculateOptimalPath();
    // Calculate the Vertex's distances from the Start and End
    this.getVertexDistances();
  }

  /**
   * Creates the Edges given the Vertices in the Graph
   * EFFECT: Constructs the Edges for this Graph based on its Vertices
   * This is private because it should only be called by the constructor
   * @param grid all Vertices in the Graph, represented as a grid composed Arrays of Arrays
   */
  private void setEdges(ArrayList<ArrayList<Vertex>> grid) {
    // If the Grid is empty, or any row is empty, then the maze is considered to be empty
    if (grid.isEmpty() || grid.get(0).isEmpty()) {
      throw new IllegalArgumentException("Maze empty!");
    }

    int height = grid.size();
    int width = grid.get(0).size();

    // The maze width and height are not permitted to be less than 2, since that would not
    // provide enough space for a start _and_ an exit to be integrated
    if (height < 2 || width < 2) {
      throw new IllegalArgumentException("Maze too tiny!");
    }

    // Create Edges for all Vertices
    for (int heightIndex = 0; heightIndex < height; heightIndex += 1) {
      for (int widthIndex = 0; widthIndex < width; widthIndex += 1) {
        // Obtain the current Vertex
        Vertex cur = grid.get(heightIndex).get(widthIndex);

        // As long as the current width + 1 does not exceed the overall width...
        if (widthIndex + 1 != width) {
          // ... then, create a horizontal Edge
          cur.makeEdge(grid.get(heightIndex).get(widthIndex + 1), true, this.bias);
        }
        // As long as the current height + 1 does not exceed the overall height...
        if (heightIndex + 1 != height) {
          // ... then, create a vertical Edge
          cur.makeEdge(grid.get(heightIndex + 1).get(widthIndex), false, this.bias);
        }
        // *The weights of these two edges doesn't matter because sortedEdges ignores them.
        // As long as the current width does not equate 0...
        if (widthIndex != 0) {
          // ... then, create a vertical Edge
          cur.makeEdge(grid.get(heightIndex).get(widthIndex - 1), false, this.bias);
        }
        // As long as the current height does not equate 0...
        if (heightIndex != 0) {
          // ... then, create a horizontal Edge
          cur.makeEdge(grid.get(heightIndex - 1).get(widthIndex), true, this.bias);
        }
      }
    }
  }

  /**
   * Create the MST of this graph following Kruskal's algorithm
   * This is private because it should only be called by the constructor
   * @return an ArrayList of Edges representing the MST
   */
  private ArrayList<Edge> kruskalMST() {
    Utils u = new Utils();
    HashMap<String, String> representatives = new HashMap<>();
    ArrayList<Edge> edgesInTree = new ArrayList<>();
    ArrayList<Edge> worklist = this.sortedEdges();

    // Initialize every node's representative to itself
    for (Vertex vertex : this.flatten()) {
      representatives.put(vertex.name, vertex.name);
    }

    // While there's more than one tree
    while (!worklist.isEmpty()) {
      Edge next = worklist.get(0);
      // Suppose find(representatives, X) equals find(representatives, Y)...
      if (u.find(representatives, next.to.name).equals(u.find(representatives, next.from.name))) {
        worklist.remove(0); // ... then discard this edge, since they're already connected
      }
      else {
        edgesInTree.add(next); // Otherwise, record this edge in edgesInTree
        u.union(representatives,
                next.to.name,
                next.from.name);
        // Do not need to remove from work-list here because next iteration should do so
        worklist.remove(0); // But we do for good form
      }
    }
    return edgesInTree;
  }

  /**
   * Sort the Array of Array of Edges by their _unique_ weights
   * * This is private because it should only be called by the constructor
   * @return sorted Array of Edges
   */
  private ArrayList<Edge> sortedEdges() {
    ArrayList<Edge> everyEdges = new ArrayList<>();
    Comparator<Edge> edgeComparator = (e1, e2) -> Integer.compare(e2.weight, e1.weight);

    int height = allVertices.size();
    int width = allVertices.get(0).size();

    // The maze width and height are not permitted to be less than 2, since that would not
    // provide enough space for a start and an exit to be integrated
    if (height < 2 || width < 2) {
      throw new IllegalArgumentException("Maze too tiny!");
    }

    // Create a list containing all the Edges in the row and column
    for (int heightIndex = 0; heightIndex < height; heightIndex += 1) {
      for (int widthIndex = 0; widthIndex < width; widthIndex += 1) {
        ArrayList<Edge> currEdges = allVertices.get(heightIndex).get(widthIndex).adjacentEdges;
        int curInd = 0; // the current Index
        // As long as the current width + 1 does not exceed the overall width...
        if (widthIndex + 1 != width) {
          // ... add the current Edge to the array
          everyEdges.add(currEdges.get(curInd));
          curInd += 1;
        }
        // As long as the current height + 1 does not exceed the overall height...
        if (heightIndex + 1 != height) {
          // ... add the current Edge to the array
          everyEdges.add(currEdges.get(curInd));
        }
        // *Do NOT include up/left (redundant, also weights are not the same)
      }
    }
    // Sort the array containing all Edges in the Grid
    everyEdges.sort(edgeComparator);
    return everyEdges;
  }

  /**
   * Flatten the Array of Array of Vertices (allVertices field) into a single Array
   * This is private because it should only be called by other methods
   * @return a single Array holding the rows of Vertices
   */
  private ArrayList<Vertex> flatten() {
    ArrayList<Vertex> flat = new ArrayList<>();

    for (ArrayList<Vertex> vertices : this.allVertices) {
      for (int widthIndex = 0; widthIndex < this.allVertices.get(0).size(); widthIndex += 1) {
        flat.add(vertices.get(widthIndex));
      }
    }

    return flat;
  }

  /**
   * Calculate the most optimal path provided this Graph
   * EFFECT: Modify the optimalMoves, movesTakenBFS, and movesTakenDFS based on the found most
   * optimal moves counts of each traversal mode
   * This is private because it should only be called by the constructor
   */
  private void calculateOptimalPath() {
    boolean isBFSDone = false;
    boolean isDFSDone = false;
    int count = 0;
    int countBFS = 0;
    int countDFS = 0;
    ICollection<Vertex> workBFS = new Queue<>();
    ICollection<Vertex> workDFS = new Stack<>();
    ArrayList<Vertex> seenBFS = new ArrayList<>();
    ArrayList<Vertex> seenDFS = new ArrayList<>();
    workBFS.add(this.allVertices.get(0).get(0));
    workDFS.add(this.allVertices.get(0).get(0));

    // While BFS traversal has not yet complete...
    while (!isBFSDone) {
      countBFS += 1;
      Vertex nextB = workBFS.remove();

      if (nextB.end) {
        isBFSDone = true;
        break;
      }

      // Add all the neighbors of next to the worklist for further processing
      for (Edge e : nextB.adjacentEdges) {
        if (this.paths.contains(e) && ! seenBFS.contains(e.to)) {
          workBFS.add(e.to);
          e.to.prev = nextB;
        }
      }
      // Add next to seen, since we're done with it
      seenBFS.add(nextB);
    }

    // While DFS traversal has not yet complete...
    while (!isDFSDone) {
      countDFS += 1;
      Vertex nextD = workDFS.remove();
      if (nextD.end) {
        isDFSDone = true;
        break;
      }
      // Add all the neighbors of next to the worklist for further processing
      for (Edge e : nextD.adjacentEdges) {
        if (this.paths.contains(e) && ! seenDFS.contains(e.to)) {
          workDFS.add(e.to);
        }
      }
      // Add next to seen, since we're done with it
      seenDFS.add(nextD);
    }

    int height = this.allVertices.size();
    int width = this.allVertices.get(0).size();
    Vertex last = this.allVertices.get(height - 1).get(width - 1);

    while (!last.equals(this.allVertices.get(0).get(0))) {
      this.solution.add(last);
      last = last.prev;
      count += 1;
    }

    this.optimalMoves = count;
    this.movesTakenBFS = countBFS;
    this.movesTakenDFS = countDFS;
  }

  /**
   * Calculate each vertex's distance from the start and end based on the walls of the
   * maze
   * EFFECT: Update each vertex's distFromStart and distFromEnd field
   * This is private because it should only be called by the constructor
   */
  private void getVertexDistances() {
    boolean isBFSDone = false;
    int countBFS = 0;
    ICollection<Vertex> workBFS = new Queue<>();
    ArrayList<Vertex> seenBFS = new ArrayList<>();
    workBFS.add(this.allVertices.get(0).get(0));

    // While BFS traversal has not yet been completed...
    while (!isBFSDone) {
      countBFS += 1;
      Vertex nextB = workBFS.remove();
      // Add all the neighbors of next to the worklist for further processing
      for (Edge e : nextB.adjacentEdges) {
        if (this.paths.contains(e) && ! seenBFS.contains(e.to)) {
          workBFS.add(e.to);
        }
      }
      nextB.distFromStart = countBFS;
      if (workBFS.isEmpty()) {
        isBFSDone = true;
        break;
      }
      // Add next to seen, since we're done with it
      seenBFS.add(nextB);
    }

    isBFSDone = false;
    countBFS = 0;
    workBFS = new Queue<>();
    seenBFS.clear();
    workBFS.add(this.allVertices
            .get(this.allVertices.size() - 1)
            .get(this.allVertices.get(0).size() - 1));

    // While BFS traversal has not yet complete...
    while (!isBFSDone) {
      countBFS += 1;
      Vertex nextB = workBFS.remove();
      // Add all the neighbors of next to the worklist for further processing
      for (Edge e : nextB.adjacentEdges) {
        if (this.paths.contains(e) && ! seenBFS.contains(e.to)) {
          workBFS.add(e.to);
        }
      }

      nextB.distFromEnd = countBFS;
      seenBFS.add(nextB);
      if (workBFS.isEmpty()) {
        isBFSDone = true;
        break;
      }
    }
  }

  /**
   * Render this Graph as an image (e.g. Maze)
   * @param width the width of the Maze
   * @param height the height of the Maze
   * @param panelWidth the width of the panel next to the Maze
   * @param scale the configured size of the Graph
   * @param colorDisplay the colorDisplay mode to display
   * @param tickCount the current tick count
   * @return the rendered Graph as a WorldImage, scaled using param "scale"
   */
  public WorldScene draw(int width, int height, int panelWidth, int scale, int colorDisplay,
                         int tickCount) {
    // Ensure that the tickCount is never greater this.paths.size - 1
    if (tickCount >= this.paths.size() - 1) {
      tickCount = this.paths.size() - 1;
    }

    List<Edge> availablePaths = this.paths.subList(0, tickCount);

    WorldScene scene = new WorldScene(width * scale, height * scale);

    // If colorDisplay is selected, display the Maze utilizing the particular colorDisplay's style
    if (colorDisplay == 1) {
      this.changeColorsFromStart();
    }
    if (colorDisplay == 2) {
      this.changeColorsFromEnd();
    }

    // Otherwise, render the Graph as a Maze Vertex by Vertex
    for (int heightIndex = 0; heightIndex < height; heightIndex += 1) {
      for (int widthIndex = 0; widthIndex < width; widthIndex += 1) {
        // Get the current Vertex
        Vertex currVertex = this.allVertices.get(heightIndex).get(widthIndex);
        // Get the Edges adjacent to the current Vertex in order to construct walls
        ArrayList<Edge> currEdges = currVertex.adjacentEdges;
        // Base image to add on
        WorldImage img = currVertex.draw(scale);
        int currIndex = 0;

        // As long as widthIndex + 1 does not equal the width...
        if (widthIndex + 1 != width) {
          if (!availablePaths.contains(currEdges.get(currIndex))) {
            // ... we are permitted to head to the right and draw
            img = new OverlayOffsetImage(
                    currEdges.get(currIndex).draw(scale, false),
                    (double) - scale / 2, 0,
                    img);
          }
          currIndex += 1;
        }
        // As long as heightIndex + 1 does not equal the height...
        if (heightIndex + 1 != height) {
          // ... we are permitted to head down and draw
          if (!availablePaths.contains(currEdges.get(currIndex))) {
            img = new OverlayOffsetImage(
                  currEdges.get(currIndex).draw(scale, true),
                  0, (double) - scale / 2,
                  img);
          }
        }
        // Place the image on the scene, shifted by 0.5 scale
        scene.placeImageXY(
                img,
                widthIndex * scale + (int) (scale * 0.5),
                heightIndex * scale + (int) (scale * 0.5));
      }
    }

    // Display the scores for BFS and DFS, as well as the total moves in the optimal path and
    // the current move count of the User
    scene.placeImageXY(
            new AboveImage(
                    new TextImage("BFS Count: " + movesTakenBFS, panelWidth / 10, Color.RED),
                    new TextImage("DFS Count: " + movesTakenDFS, panelWidth / 10, Color.BLUE),
                    new TextImage("Optimal Moves: " + optimalMoves, panelWidth / 10, Color.GREEN),
                    new TextImage("User Count: " + movesTakenUser, panelWidth / 10, Color.BLACK)),
            (width * scale) + (panelWidth / 2), height * scale / 2);

    return scene;
  }

  /**
   * Initiate the search, style dependent on whether it is a BFS or DFS search
   * EFFECT: Initiate the worklist between a new Queue or Stack, and begin BFS or DFS search
   * @param isBFS determine if the search style is a BFS or DFS
   */
  public void startSearch(boolean isBFS) {
    this.isBFS = isBFS;

    if (isBFS) {
      worklist = new Queue<>();
    }
    else {
      worklist = new Stack<>();
    }

    // Add the top left-most vertex to the worklist
    worklist.add(allVertices.get(0).get(0));

    // At the start of the search, we have not seen anything yet. Therefore, set the "seen" field
    // to an empty Array
    this.seen = new ArrayList<>();
  }

  /**
   * Search the Graph exactly once in either a BFS or DFS search style
   * EFFECT: Search the Graph exactly once, and modify the traversed path accordingly to the
   * traversal method's respective color
   */
  public void searchOnce() {
    // Obtain the next Vertex in the worklist (which is the item at the end)
    Vertex next = worklist.remove();

    if (this.isBFS) {
      // BFS is displayed by the color PINK
      next.color = Color.PINK;
    }
    else {
      // DFS is displayed by the color CYAN
      next.color = Color.CYAN;
    }

    if (next.end) {
      // If the next Vertex is the exit, then restart the worklist, since the search is completed
      worklist = new Queue<>();
    }
    else {
      // Otherwise, add all the neighbors of next to the worklist for further processing
      for (Edge e : next.adjacentEdges) {
        if (this.paths.contains(e) && ! this.seen.contains(e.to)) {
          worklist.add(e.to);
          e.to.prev = next;
        }
      }
      // Add next to the "seen" field, since we're done with it
      this.seen.add(next);
    }

    // Repaint the Edges to clear this Graph. Specifically, the colors of the start and exit at
    // the edge should be maintained.
    this.repaintStartAndExit();
  }

  /**
   * Move the user towards a specified direction
   * EFFECT: Update the location of the User, and modify the rendering of the Maze to reflect the
   * path that the User has traversed through
   * @param direction the direction the user is moving towards (left, right, down, up)
   * @param width the width of the Maze
   * @param height the height of the Maze
   */
  public void moveTowards(String direction, int width, int height) {
    // Do not repaint the User's manual maze initially
    Utils u = new Utils();

    boolean repaint = false;
    Posn prevLocation = this.location;
    Vertex currVertex;
    Vertex nextVertex;

    switch (direction) {
      case "left":
        // If the user is moving left, decrease the x coordinate of the current posn by 1
        int moveLeft = u.getX(this.location) - 1;
        if ((moveLeft < 0)) {
          // if moveLeft is less than 0 (out of bounds for the Maze), return
          return;
        }

        // Obtain the current Vertex
        currVertex = this.allVertices.get(u.getY(this.location)).get(u.getX(this.location));
        // Obtain the next Vertex after moving left
        nextVertex = this.allVertices.get(u.getY(this.location)).get(moveLeft);

        // If there is an Edge between the curr and next Vertices, then return. This is due to
        // the fact that a user may not travel past walls.
        if (edgeBetween(currVertex, nextVertex)) {
          return;
        }

        // Otherwise, if the new location is valid, then move the player by modifying the
        // player's location
        this.location = new Posn(u.getX(this.location) - 1, u.getY(this.location));
        this.movesTakenUser += 1;
        repaint = true; // Allow the Maze to repaint to indicate the user's curr and prev locations
        break;
      case "up":
        // If the user is moving up, decrease the y coordinate of the current posn by 1
        int moveUp = u.getY(this.location) - 1;

        if (moveUp < 0) {
          // if moveUp is less than 0 (out of bounds for the Maze), return
          return;
        }

        // Obtain the current Vertex
        currVertex = this.allVertices.get(u.getY(this.location)).get(u.getX(this.location));
        // Obtain the next Vertex after moving up
        nextVertex = this.allVertices.get(moveUp).get(u.getX(this.location));

        // If there is an Edge between the curr and next Vertices, then return. This is due to
        // the fact that a user may not travel past walls.
        if (edgeBetween(currVertex, nextVertex)) {
          return;
        }

        // Otherwise, if the new location is valid, then move the player by modifying the
        // player's location
        this.location = new Posn(u.getX(this.location), u.getY(this.location) - 1);
        this.movesTakenUser += 1;
        repaint = true; // Allow the Maze to repaint to indicate the user's curr and prev locations
        break;
      case "down":
        // If the user is moving down, increase the y coordinate of the current posn by 1
        int moveDown = u.getY(this.location) + 1;

        if (moveDown == height) {
          // if moveDown equals the Maze's height (out of bounds for the Maze), return
          return;
        }

        // Obtain the current Vertex
        currVertex = this.allVertices.get(u.getY(this.location)).get(u.getX(this.location));
        // Obtain the next Vertex after moving down
        nextVertex = this.allVertices.get(moveDown).get(u.getX(this.location));

        // If there is an Edge between the curr and next Vertices, then return. This is due to
        // the fact that a user may not travel past walls.
        if (edgeBetween(currVertex, nextVertex)) {
          return;
        }

        // Otherwise, if the new location is valid, then move the player by modifying the
        // player's location
        this.location = new Posn(u.getX(this.location), u.getY(this.location) + 1);
        this.movesTakenUser += 1;
        repaint = true; // Allow the Maze to repaint to indicate the user's curr and prev locations
        break;
      case "right":
        // If the user is moving to the right, increase the x coordinate of the current posn by 1
        int moveRight = u.getX(this.location) + 1;

        if (moveRight == width) {
          // if moveRight equals the Maze's width (out of bounds for the Maze), return
          return;
        }

        // Obtain the current Vertex
        currVertex = this.allVertices.get(u.getY(this.location)).get(u.getX(this.location));
        // Obtain the next Vertex after moving down
        nextVertex = this.allVertices.get(u.getY(this.location)).get(moveRight);

        // If there is an Edge between the curr and next Vertices, then return. This is due to
        // the fact that a user may not travel past walls.
        if (edgeBetween(currVertex, nextVertex)) {
          return;
        }

        // Otherwise, if the new location is valid, then move the player by modifying the
        // player's location
        this.location = new Posn(u.getX(this.location) + 1, u.getY(this.location));
        this.movesTakenUser += 1;
        repaint = true; // Allow the Maze to repaint to indicate the user's curr and prev locations
        break;
      default:
        break;
    }
    if (repaint) {
      // If the player is changing location, then modify the previous location's color to CYAN and
      // update the current location to BLUE
      this.colorLocation(prevLocation, this.location);
    }
    if (u.getX(this.location) == width - 1 && u.getY(this.location) == height - 1) {
      // Once the player has reached the exit, display the optimal path
      this.paintOptimalPath(true);
    }
    else if (this.isUserDone) {
      // If the user has completed traversal of the maze, then set isUserDone to false
      this.isUserDone = false;
      // Repaint the maze...
      this.repaint();
      // ... and reset the player's location
      this.location = new Posn(0, 0);
    }
  }

  /**
   * Reset the color display of the Maze by altering Vertices (which are not the exit and start)
   * to WHITE
   * EFFECT: Render all Vertices as WHITE, except for the Start and Exit Vertices, which must
   * remain GREEN and PURPLE, respectively
   */
  public void repaint() {
    int widthIndex = -1;
    int heightIndex = -1;

    for (ArrayList<Vertex> vertexRow : this.allVertices) {
      heightIndex += 1;
      for (Vertex vert : vertexRow) {
        widthIndex += 1;
        // The Start is located at (0, 0), so ensure the Color of this Vertex remains GREEN
        if (widthIndex == 0 && heightIndex == 0) {
          vert.color = new Color(1, 90, 32);
        }
        // The Exit is located at (widthIndex - 1, heightIndex - 1), so ensure the Color of this
        // Vertex remains PURPLE
        else if (widthIndex == this.allVertices.get(0).size() - 1
                && heightIndex == this.allVertices.size() - 1) {
          vert.color = new Color(100, 0, 128);
        }
        // Otherwise, modify every Vertex's Color to WHITE
        else {
          vert.color = Color.WHITE;
        }
      }
    }

    // Modify the Start and Exit's Color fields to ensure they are maintained even if it is
    // cleared, toggled, etc.
    this.repaintStartAndExit();
  }

  /**
   * Modify the Color fields of the Start and Exit vertices to GREEN and PURPLE, respectively
   * EFFECT: Modify the Color fields of the Start and Exit vertices to GREEN or PURPLE,
   * respectively
   * This is private because it should only be called by other methods after they modify colors
   */
  private void repaintStartAndExit() {
    int height = this.allVertices.size();
    int width = this.allVertices.get(0).size();

    // Modify Color field of the Vertex at the Start to GREEN
    this.allVertices.get(0).get(0).color = new Color(1, 90, 32);
    // Modify Color field of the Vertex at the Exit to PURPLE
    this.allVertices.get(height - 1).get(width - 1).color = new Color(100, 0, 128);
  }

  /**
   * Modify the previous and current locations to indicate the current location of the User
   * EFFECT: Modify the rendering of previous and current locations to indicate the current location
   * of the User. Must ensure that the Start and Exit vertices remain GREEN and Purple,
   * respectively.
   * This is private because it should only be called by methods that update the current location
   * @param prevLocation the previous Posn
   * @param currLocation the current Posn
   */
  private void colorLocation(Posn prevLocation, Posn currLocation) {
    // We feel a field of field access is justified here as the point of the Posns is to store
    // positional information for us to use
    // The previous location's Color field should be altered to CYAN
    this.allVertices.get(prevLocation.y).get(prevLocation.x).color = Color.CYAN;
    // The current location's Color field should be altered to BLUE
    this.allVertices.get(currLocation.y).get(currLocation.x).color = Color.BLUE;
    // Repaint to the Start and Exit to GREEN and PURPLE, respectively
    this.repaintStartAndExit();
  }

  /**
   * Determine if an Edge exists between two Vertices
   * This is private because it should only be called by other methods
   * @param a one of the Vertices
   * @param b one of the Vertices
   * @return if an Edge exists between two Vertices
   */
  private boolean edgeBetween(Vertex a, Vertex b) {
    for (Edge e : this.paths) {
      // If an Edge's "to" equate to Vertex a and "from" equate to Vertex b, or vice versa, then no
      // walls are blocking either Vertex
      if ((e.to.equals(a) && e.from.equals(b)) || (e.to.equals(b) && e.from.equals(a))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determine if a Maze has been successfully traversed
   * @return if a Maze has been successfully traversed
   */
  public boolean isDone() {
    if (this.worklist.isEmpty()) {
      // Display the most optimal path to the user
      this.paintOptimalPath(false);
      return true;
    }
    return false;
  }

  /**
   * Paint the most optimal path GREY after the User has successfully traversed the Maze, or
   * either RED or BLUE if BFS or DFS (respectively) has completed traversal
   * This is private because it should only be called by other methods when the maze has been solved
   * EFFECT: Render every Vertex in the most optimal path as GREY, RED or BLUE, depending on the
   * traversal method utilized
   * @param isUserNavigating if the User is navigating the maze (versus BFS or DFS algorithms)
   */
  private void paintOptimalPath(boolean isUserNavigating) {
    // If the user is navigating the Maze, then paint the solution LIGHT_GRAY
    if (isUserNavigating) {
      this.isUserDone = true;
      for (Vertex v : this.solution) {
        // For every Vertex in the Solution, paint it LIGHT_GRAY
        v.color = Color.LIGHT_GRAY;
      }
    }
    // Otherwise, either the DFS or BFS algorithm has completed traversal, so reveal the solution
    // accordingly to the algorithm
    else {
      for (Vertex v : this.solution) {
        if (isBFS) {
          // For every Vertex in the Solution, paint it RED
          v.color = Color.RED;
        }
        else {
          // For every Vertex in the Solution, paint it BLUE
          v.color = Color.BLUE;
        }
      }
    }

    // Ensure the Colors of the Start and Exit are maintained (GREEN and PURPLE, respectively)
    this.repaintStartAndExit();
  }

  /**
   * Alter this Graph to be a duplication of the other Graph
   * EFFECT: Modify the fields of this Graph to be the exact duplication of the other Maze's fields
   * @param maze the other Graph to copy from
   */
  public void copyMaze(Graph maze) {
    // Clear any information contained in this Graph's allVertices field
    this.allVertices.clear();
    // Copy the other Graph's allVertices field, one by one Vertex
    for (ArrayList<Vertex> row : maze.allVertices) {
      ArrayList<Vertex> newRow = new ArrayList<>();
      for (Vertex vertex : row) {
        // Create a new Vertex that is the duplication of the other Graph's Vertex to create a
        // deeper copy
        Vertex newVertex =
                new Vertex(
                        vertex.color,
                        vertex.start,
                        vertex.end,
                        vertex.adjacentEdges);
        newRow.add(newVertex);
      }
      this.allVertices.add(newRow);
    }

    // Clear any information contained in this Graph's paths field
    this.paths.clear();
    for (Edge edge : maze.paths) {
      // Create a new Edge that is the duplication of the other Graph's Edge to create a deeper copy
      Edge newEdge =
              new Edge(
                      edge.from,
                      edge.to,
                      edge.weight
              );
      this.paths.add(newEdge);
    }

    this.worklist = maze.worklist;
    this.seen = maze.seen;
    this.isBFS = maze.isBFS;
    this.location = maze.location;
  }

  /**
   * Change the color of each vertex to reflect its distance from the start of the maze
   * EFFECT: Change each vertex's color field
   * This is private because it should only be called by other methods when the user wants to use
   * this feature
   */
  private void changeColorsFromStart() {
    int max = Integer.MIN_VALUE;

    for (Vertex v : this.flatten()) {
      int dist = v.distFromStart;
      if (dist > max) {
        max = dist;
      }
    }

    int dif = max - 1;

    // For every vertex, alter its color based on its distance from the Start
    for (Vertex v : this.flatten()) {
      double percentRed = (v.distFromStart - 1.0) / dif;
      if (percentRed > 0.5) {
        percentRed = percentRed * 2;
        if (percentRed >= 1.0) {
          percentRed = 1.0;
        }
        int red = (int) (255 * percentRed);
        if (red > 255) {
          red = 255;
        }
        if (red < 0) {
          red = 0;
        }
        v.color = new Color(red, 0, 0);
      }
      else {
        percentRed = percentRed * 2;
        if (percentRed <= 0.0) {
          percentRed = 0.0;
        }
        int blue = (int) (255 * percentRed);
        if (blue > 255) {
          blue = 255;
        }
        if (blue < 0) {
          blue = 0;
        }
        v.color = new Color(0, 0, blue);
      }
    }
    this.repaintStartAndExit();
  }

  /**
   * Change the color of each vertex to reflect its distance from the end of the maze
   * EFFECT: Change each vertex's color field
   * This is private because it should only be called by other methods when the user wants to use
   * this feature
   */
  private void changeColorsFromEnd() {
    int max = Integer.MIN_VALUE;

    for (Vertex v : this.flatten()) {
      int dist = v.distFromEnd;
      if (dist > max) {
        max = dist;
      }
    }

    int dif = max - 1;

    // For every vertex, alter its color based on its distance from the Exit
    for (Vertex v : this.flatten()) {
      double percentRed = (v.distFromEnd - 1.0) / dif;
      if (percentRed > 0.5) {
        percentRed = percentRed * 2;
        if (percentRed >= 1.0) {
          percentRed = 1.0;
        }
        int red = (int) (255 * percentRed);
        if (red > 255) {
          red = 255;
        }
        if (red < 0) {
          red = 0;
        }
        v.color = new Color(red, 0, 0);
      } else {
        percentRed = percentRed * 2;
        if (percentRed <= 0.0) {
          percentRed = 0.0;
        }
        int blue = (int) (255 * percentRed);
        if (blue > 255) {
          blue = 255;
        }
        if (blue < 0) {
          blue = 0;
        }
        v.color = new Color(0, 0, blue);
      }
    }
    this.repaintStartAndExit();
  }
}

/**
 * Examples and tests for Maze file
 */
class ExamplesMaze {
  Vertex start;
  Vertex exit;
  Vertex middle;
  Edge edge1;
  Edge edge2;
  Edge edgeArbitrary;
  Graph maze;


  void vertexSetUp() {
    start = new Vertex(Color.RED, true, false, new ArrayList<>());
    exit = new Vertex(Color.GREEN, false, true, new ArrayList<>());
    middle = new Vertex(Color.YELLOW, false, false, new ArrayList<>());
  }

  void edgeSetUp() {
    Vertex v = new Vertex(Color.RED, false, false, new ArrayList<>());
    v.name = "A";
    Vertex v1 = new Vertex(Color.GREEN, false, false, new ArrayList<>());
    v1.name = "B";
    Vertex v2 = new Vertex(Color.BLACK, true, true, new ArrayList<>());
    v2.name = "C";
    edge1 = new Edge(v, v1, 10);
    edge2 = new Edge(v1, v, 10);
    edgeArbitrary = new Edge(v, v2, 5000);
  }

  void graphSetUp() {
    maze = new Graph(2, 2);
  }

  void graphSetUpLarge() {
    maze = new Graph(3, 3);
  }

  boolean testMakeEdge(Tester t) {
    vertexSetUp();
    start.makeEdge(middle, true, 0);
    return t.checkExpect(start.adjacentEdges.size(), 1)
            && t.checkExpect(start.adjacentEdges.get(0).to, middle)
            && t.checkExpect(start.adjacentEdges.get(0).from, start)
            && t.checkExpect(middle.adjacentEdges.size(), 0);
  }

  void testDrawVertex(Tester t) {
    vertexSetUp();
    start.draw(10).saveImage("VertexRed.png");
    middle.draw(50).saveImage("VertexYellow.png");
  }

  boolean testVertexSameness(Tester t) {
    Vertex v = new Vertex(Color.RED, false, false, new ArrayList<>());
    v.name = "Same";
    Vertex v2 = new Vertex(Color.GREEN, false, false, new ArrayList<>());
    v2.name = "Same";
    Vertex v3 = new Vertex(Color.GREEN, false, false, new ArrayList<>());
    v3.name = "Different";
    return t.checkExpect(v.equals(v2), true)
              && t.checkExpect(v2.equals(v3), false)
              && t.checkExpect(v3.equals(v2), false)
              && t.checkExpect(v.equals(new Edge(v, v2, 1)), false);
  }

  boolean testVertexHashcode(Tester t) {
    Vertex v = new Vertex(Color.RED, false, false, new ArrayList<>());
    v.name = "Same";
    Vertex v2 = new Vertex(Color.GREEN, false, false, new ArrayList<>());
    v2.name = "Same";
    Vertex v3 = new Vertex(Color.GREEN, false, false, new ArrayList<>());
    v3.name = "Different";
    return t.checkExpect(v.hashCode(), v2.hashCode())
            && t.checkExpect(v2.hashCode() == v3.hashCode(), false)
            && t.checkExpect(v3.hashCode() == v2.hashCode(), false);
  }

  boolean testEdgeEquals(Tester t) {
    edgeSetUp();
    return t.checkExpect(edge1.equals(edge2), true)
            && t.checkExpect(edge2.equals(edge1), true)
            && t.checkExpect(edge1.equals(edgeArbitrary), false);
  }

  boolean testEdgeHashCode(Tester t) {
    edgeSetUp();
    return t.checkExpect(edge1.hashCode() == edge2.hashCode(), true)
            && t.checkExpect(edge2.hashCode() == edge1.hashCode(), true)
            && t.checkExpect(edge1.hashCode() == edgeArbitrary.hashCode(), false);
  }

  void testEdgeDraw(Tester t) {
    edgeSetUp();
    edge1.draw(10, true).saveImage("HorizEdge.png");
    edge2.draw(20, false).saveImage("VertEdge.png");
  }

  void testGraphDraw(Tester t) {
    graphSetUp();
    maze.draw(2, 2, 10, 2, 2, 0).saveImage("GraphDraw.png");
    // Otherwise, see the bigBang and ensure that drawings appear accurate
  }

  boolean testCopyMaze(Tester t) {
    graphSetUp();
    Graph maze2 = new Graph();
    maze.copyMaze(maze2);
    return t.checkExpect(maze.allVertices, maze2.allVertices)
            && t.checkExpect(maze.bias, maze2.bias)
            && t.checkExpect(maze.worklist, maze2.worklist)
            && t.checkExpect(maze.paths, maze2.paths)
            && t.checkExpect(maze.isBFS, maze2.isBFS)
            && t.checkExpect(maze.location, maze2.location);
  }

  boolean testRepaint(Tester t) {
    graphSetUp();
    Vertex start = maze.allVertices.get(0).get(0);
    Vertex exit = maze.allVertices.get(1).get(1);
    Vertex middle = maze.allVertices.get(0).get(1);
    start.color = Color.PINK;
    exit.color = Color.PINK;
    middle.color = Color.PINK;
    maze.repaint();
    return t.checkExpect(start.color, new Color(1, 90, 32))
            && t.checkExpect(exit.color, new Color(100, 0, 128))
            && t.checkExpect(middle.color, Color.WHITE);
  }

  boolean testSearchOnceBFS(Tester t) {
    graphSetUp();
    maze.startSearch(true);
    maze.searchOnce();
    maze.searchOnce();
    Vertex start = maze.allVertices.get(0).get(0);
    Vertex exit = maze.allVertices.get(1).get(1);
    Vertex middle = maze.allVertices.get(0).get(1);
    maze.draw(2, 2, 1, 10, 0, 0)
            .saveImage("SearchOnceBFS.png");
    return t.checkExpect(start.color, new Color(1, 90, 32))
            && t.checkExpect(exit.color, new Color(100, 0, 128));
  }

  boolean testSearchOnceDFS(Tester t) {
    graphSetUp();
    maze.startSearch(false);
    maze.searchOnce();
    maze.searchOnce();
    Vertex start = maze.allVertices.get(0).get(0);
    Vertex exit = maze.allVertices.get(1).get(1);
    // This image indicates the maze has been DFS traversed
    maze.draw(2, 2, 1, 10, 0, 0)
            .saveImage("SearchOnceDFS.png");
    return t.checkExpect(start.color, new Color(1, 90, 32))
            && t.checkExpect(exit.color, new Color(100, 0, 128));
  }

  boolean testStartSearchQueue(Tester t) {
    graphSetUp();
    maze.startSearch(true);
    ICollection<Vertex> q = new Queue<>();
    q.add(maze.allVertices.get(0).get(0));
    return t.checkExpect(maze.worklist, q);
  }

  boolean testStartSearchStack(Tester t) {
    graphSetUp();
    maze.startSearch(false);
    ICollection<Vertex> s = new Stack<>();
    s.add(maze.allVertices.get(0).get(0));
    return t.checkExpect(maze.worklist, s);
  }

  boolean testIsDone(Tester t) {
    graphSetUp();
    maze.moveTowards("right", 2, 2);
    maze.moveTowards("down", 2, 2);
    maze.isDone();
    maze.draw(2, 2, 1, 10, 0, 0)
            .saveImage("MazeIsDone.png");
    return t.checkExpect(maze.worklist.isEmpty(), true);
  }

  void testMoveTowardsRight(Tester t) {
    graphSetUpLarge();
    maze.moveTowards("right", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsRight.png");
    maze.moveTowards("right", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsRightRight.png");
    // Traveling out of bounds
    maze.moveTowards("right", 3, 3);
    maze.moveTowards("right", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsRightOutOfBounds.png");
  }

  void testMoveTowardsLeft(Tester t) {
    graphSetUpLarge();
    // Traveling out of bounds
    maze.moveTowards("left", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsLeftOutOfBounds.png");
    maze.moveTowards("right", 3, 3);
    maze.moveTowards("right", 3, 3);
    maze.moveTowards("left", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsLeft.png");
    maze.moveTowards("left", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsLeftLeft.png");
  }

  void testMoveTowardsUp(Tester t) {
    graphSetUpLarge();
    // Traveling out of bounds
    maze.moveTowards("up", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsUpOutOfBounds.png");
    maze.moveTowards("down", 3, 3);
    maze.moveTowards("down", 3, 3);
    maze.moveTowards("up", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsUp.png");
    maze.moveTowards("up", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsUpUp.png");
  }

  void testMoveTowardsDown(Tester t) {
    graphSetUpLarge();
    // Traveling out of bounds
    maze.moveTowards("down", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsDown.png");
    maze.moveTowards("down", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsDownDown.png");
    maze.moveTowards("down", 3, 3);
    maze.moveTowards("down", 3, 3);
    maze.draw(3, 3, 1, 10, 0, 0)
            .saveImage("MoveTowardsDownOutOfBounds.png");
  }

  void testMoveTowardsCompleted(Tester t) {
    graphSetUp();
    maze.moveTowards("right", 3, 3);
    maze.moveTowards("down", 3, 3);
    maze.draw(2, 2, 1, 10, 0, 0)
            .saveImage("MoveTowardsCompletedTraversal.png");
  }
}