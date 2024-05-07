# Additional Features
Provided are the additional features we were able to implement into our Maze Generation project:

**Whistles:**

- [x] Provide an option to toggle the viewing of the visited paths.

- [x] Allow the user the ability to start a new maze without restarting the program.

- [x] Keep the score of wrong moves — for either the automatic solutions or manual ones — and 
  maybe keep statistics on which one of the two algorithms had fewer steps for each maze.

**Bells:**

- [x] In addition to animating the solution of the maze, also animate the construction of the 
  maze: on each tick, show a single wall being knocked down.

- [x] Color every square with a gradient of colors indicating how far it is from the start of 
  the maze. E.g. red means very close to the start, and blue means very far.

- [x] Color every square with a gradient of colors indicating how far it is from the exit of the 
  maze. E.g. red means very close to the exit, and blue means very far. (Are these colors 
  exactly the opposite of the previous ones, or is this a different color pattern altogether?)

- [x] (Tricky) Construct mazes with a bias in a particular direction — a preference for 
  horizontal or vertical corridors. (Hint: you might wish to play tricks with the edge weights 
  here.)

--- 

# Design Decisions

Below enumerates the methods and fields which we made public, as well as an explanation as to 
why the data and methods were not able to be made private or final.

## Maze.java
### class `Vertex`

Fields:

- `String name`: A public field, since it is reassigned in the constructor as 
`this.name = "H" + height + "W" + width;`. This reflects appropriately, since the height and 
  width of a `Vertex` may differ graph to graph.
- `Color color`: A public field, since the color may be modified depending on if a Vertex has 
  been traversed, cleared, toggled, with gradient, etc.
- `Vertex prev`: A public field, since the prev state of this Vertex is reassigned in the 
  method `calculateOptimalPath()` and `searchOnce()` in an effort to follow the worklist 
  during BFS or DFS algorithm traversal.
- `int distFromStart`: A public field, since the next Vertex's `distFromStart` is modified in the 
  method `getVertexDistance()` in an effort to calculate the Vertex's distance from the starting 
  Vertex. It initially begins at 0 in the constructor, but during calculation, the actual 
  distance will be calculated.
- `int distFromEnd`: A public field, since the next Vertex's `distFromEnd` is modified in the 
  method `getVertexDistance()` in an effort to calculate the Vertex's distance from the ending 
  Vertex. It initially begins at 0 in the constructor, but during calculation, the actual 
  distance will be calculated.

Methods:

- `void makeEdge()`: A public method, since it is utilized in `setEdge()` when Edges must be 
  created for every Vertex in a Graph.
- `WorldImage draw()`: A public method, since it is utilized in the method `draw()` in Graph in 
  an effort to render this Vertex as a square in the Maze. 
- `boolean equals()` and `int hashCode()`: These are mandated to be public methods, since they 
  are simply overriding the public `equals()` and `hashCode()` methods from java.lang.Object.

### class `Edge`

Methods:

- `WorldImage draw()`: A public method, since it is utilized in the method `draw()` in Graph in an 
  effort to render this Edge as a wall in the Maze.
- `boolean equals()` and `int hashCode()`: These are mandated to be public methods, since they 
  are simply overriding the public `equals()` and `hashCode()` methods from java.lang.Object.

### class `Graph`

Fields:

- `ICollection<Vertex> worklist`: A public field, since it is reassigned in `startSearch()`, 
  depending on which algorithm used. Specifically, BFS should utilize a Queue, while DFS 
  should make use of a Stack. Furthermore, it is emptied when the Maze has been successfully 
  traversed in `searchOnce()`.
- `ArrayList<Vertex> seen`: A public field, since it is reassigned in `copyMaze()` to `maze.
  seen` in an effort to duplicate the contents of the other Maze.
- `boolean isBFS`: A public field, since it is reassigned in `copyMaze()` to `maze.isBFS` in an 
  effort to duplicate the contents of the other Maze.
- `Posn location`: A public field, since it is reassigned in `copyMaze()` to `maze.location` in 
  an effort to duplicate the contents of the other Maze. This field is also reassigned in 
  `moveTowards()` when the current location of the User is updated when a valid move is made in 
  the Maze.
- `int optimalMoves`: A public field, since it is reassigned in `calculateOptimalPath()` to 
  represent the length of the most optimal path from the Start to Exit.
- `int movesTakenBFS`: A public field, since it is reassigned in `calculateOptimalPath()` to 
  represent the length of the most optimal BFS path from the Start to Exit.
- `int movesTakenDFS`: A public field, since it is reassigned in `calculateOptimalPath()` to
  represent the length of the most optimal DFS path from the Start to Exit.
- `int movesTakenUser`: A public field, since it is modified in `moveTowards()` to reflect the 
  User's total move count. 
- `boolean isUserDone`: A public field, since it is modified in `moveTowards()` to reflect when 
  the User has successfully traversed the Maze, or if otherwise.

Methods:
- `WorldScene draw()`: A public method, since it is utilized in the WorldState class (specifically,
 in the method `makeScene()`) to render the Maze as an interactive bigBang. 
- `void startSearch()`: A public method, since it is utilized in `onKeyReleased()` to indicate 
  when BFS or DFS traversal should initiate (e.g. following particular key releases).
- `void searchOnce()`: A public method, since it is utilized in `onTick()` to initiate BFS or 
  DFS traversal until the Exit has been reached. 
- `void moveTowards()`: A public method, since it is utilized in `onKeyReleased()` to update the 
  User's current position based on the key interactions. 
- `void repaint()`: A public method, since it is utilized in `onKeyReleased()` to render the 
  Maze to its initial state. This is additionally used in `moveTowards()` during the User's manual 
  Maze traversal to reflect the areas in which the User has travelled past. 
- `void repaintStartAndExit():` A public method, since it is widely utilized in `repaint()`, 
  `paintOptimalPath()`, `changeColorsFromStart()` and `changeColorsFromExit()`. This is due to 
  the fact that even though other Vertices may be repainted to its initial state white, or some 
  other color (for example, distant-gradient colors), the colors of the Start and Exit must remain 
  consistent. Such that, the starting Vertex must be rendered as GREEN, while the ending 
  Vertex must be PURPLE. 
- `boolean isDone()`: A public method, since it is utilized in `onTick()` to reflect when the 
  Maze has successfully completed its traversal and reached the Exit. 
- `void copyMaze()`: A public method, since it is utilized in `onKeyReleased()` to create a 
  duplication of the Maze before it is cleared or toggled to its initial state.

## WorldState.java
### class `WorldState`

Fields:

- `Graph maze`: A public field, since it is utilized in methods `reset()` and `onKeyReleased()`. 
  Specifically, `reset()` requires the Maze to be reset to its original form, before it has been 
  traversed. `onKeyReleased()` requires the Maze to be modified when the path is erased.
- `Graph currMaze`: A public field, since it is utilized in `onKeyReleased()`. Specifically, it 
  gets reassigned to the current Maze before the current Maze gets erased, in an effort to 
  remember the current Maze if toggled back to it.
- `boolean togglePath`: A public field, since it is utilized in `onKeyReleased()` when the 
  user toggles between seeing paths and removing the paths traversed thus far. 
- `boolean isSearching`: A public field, since it is utilized in `onKeyReleased()` and `onTick()
  `. Specifically, `onTick()` switches this field off when the BFS or DFS algorithm has finished 
  traversal. In `onKeyReleased()`, the field gets turned off when distance-gradient mode is on, or when 
  the user is manually traversing the Maze. 
- `boolean isUserMoving`: A public field, since it is modified when BFS or DFS algorithm is 
  utilized in `onKeyReleased()` over manual user traversal.
- `int colorDisplayMode`: A public field, since it is modified when the color display modes are switched between 
  in `onKeyReleased()`. 
- `int count`: A public field, since it is modified in `onTick()` whenever a tick is made in the 
  `WorldState`.

Methods:

All methods in `WorldState` are public, since they are inheriting from `World`.

## Utils.java
The only major design decision we believe should be mentioned is the implementation of `getX` and 
`getY`. However, we deem this as necessary, since we may need to modify the Posn, but only at one 
value at a time.

## Miscellaneous
One important note to mention is that we decided to calculate the quantity of steps from BFS and DFS  
before the initial rendering of the maze. This made the code much cleaner without excess boolean flags,
but it means larger mazes take slightly longer to render as the maze approaches the maximum 
dimensions.
