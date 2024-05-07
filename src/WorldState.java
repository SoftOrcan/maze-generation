import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;
import tester.*;

/**
 * Represents a WorldState
 */
class WorldState extends World {
  Graph maze;
  Graph currMaze;
  final int mazeWidth;
  final int mazeHeight;
  final int panelWidth;
  final int scale;
  boolean togglePath;
  boolean isSearching;
  boolean isUserMoving;
  int colorDisplayMode;
  int tickCount;

  WorldState(int mazeWidth, int mazeHeight) {
    this.maze = new Graph(mazeWidth, mazeHeight, 0);
    this.currMaze = new Graph(mazeWidth, mazeHeight);
    this.mazeWidth = mazeWidth;
    this.mazeHeight = mazeHeight;
    this.panelWidth = 200;
    this.scale = 10;
    this.togglePath = true;
    this.isSearching = false;
    this.isUserMoving = false;
    this.colorDisplayMode = 0;
    tickCount = 0;
  }

  WorldState(int mazeWidth, int mazeHeight, int bias) {
    this.maze = new Graph(mazeWidth, mazeHeight, bias);
    this.currMaze = new Graph(mazeWidth, mazeHeight);
    this.mazeWidth = mazeWidth;
    this.mazeHeight = mazeHeight;
    this.panelWidth = 200;
    this.scale = 10;
    this.togglePath = true;
    this.isSearching = false;
    this.isUserMoving = false;
    this.colorDisplayMode = 0; // 1 for vert, -1 for horiz, or 0 for no bias
    tickCount = 0;
  }

  /**
   * Draw this world as a scene
   */
  @Override
  public WorldScene makeScene() {
    return this.maze.draw(
            mazeWidth,
            mazeHeight,
            panelWidth,
            scale,
            colorDisplayMode,
            tickCount);
  }

  /**
   * Update the world on every tick
   * EFFECT: Update the world accordingly to the method of traversal utilized
   */
  @Override
  public void onTick() {
    if (tickCount <= mazeHeight * mazeHeight + 1) {
      tickCount += 1;
    }

    if (isSearching) {
      if (this.maze.isDone()) {
        // Once the Maze has been successfully traversed, then halt any automated traversal
        isSearching = false;
        return;
      }
      // Otherwise, continue traversal
      this.maze.searchOnce();
    }
  }

  /**
   * Restart the Maze and halts any automated traversal
   * EFFECT: Restart the Maze to its initial State
   */
  public void reset() {
    this.maze = new Graph(this.mazeWidth, this.mazeHeight);
    this.isSearching = false;
  }

  /**
   * Handle a key event
   * EFFECT: Perform a different event, modifying the Maze accordingly, based on the key
   * interaction
   * @param key the key pressed and released
   */
  @Override
  public void onKeyReleased(String key) {
    // If the colorDisplayMode has not yet been activated, then return the world as-is
    if (colorDisplayMode != 0 && (!key.equals("h"))) {
      return;
    }

    switch (key) {
      case "h": // Render the Maze accordingly to its colorDisplay
        isSearching = false;
        // Repaint the Maze according to its colorDisplay
        this.maze.repaint();
        // Restart the User's location to the Start
        this.maze.location = new Posn(0, 0);
        if (colorDisplayMode == 0) {
          colorDisplayMode = 1;
        }
        else if (colorDisplayMode == 1) {
          colorDisplayMode = 2;
        }
        else {
          colorDisplayMode = 0;
        }
        break;
      case "s":
        this.tickCount = this.mazeWidth * this.mazeHeight + 1;
        break;
      case " ": // Pause the world
        this.isSearching = !this.maze.worklist.isEmpty() && !this.isSearching;
        break;
      case "b": // Perform BFS traversal
        isUserMoving = false;
        isSearching = false;
        this.maze.repaint();
        this.maze.startSearch(true);
        isSearching = true;
        break;
      case "d": // Perform DFS traversal
        isUserMoving = false;
        isSearching = false;
        this.maze.repaint();
        this.maze.startSearch(false);
        isSearching = true;
        break;
      case "c": // Clear the Maze
        isSearching = false;
        this.maze.repaint();
        // Restart the location and moves count of the User
        this.maze.location = new Posn(0, 0);
        this.maze.movesTakenUser = 0;
        //this.tickCount = 0;
        break;
      case "t": // Toggle between the initial or current state of the Maze
        if (togglePath) {
          this.currMaze.copyMaze(this.maze);
          // Create a temporary holder of the current Maze
          Graph temp = currMaze;
          // Save the current Maze
          currMaze = maze;
          maze = temp;
          this.maze.repaint();
          this.togglePath = false;
        }
        else {
          // Create a temporary holder of the current Maze
          Graph temp = currMaze;
          // Save the current Maze
          currMaze = maze;
          maze = temp;
          this.togglePath = true;
        }
        break;
      case "n": // Generate a new Maze without restarting the world
        this.reset();
        break;
      case "left": // Move the User to the left
        if (!isUserMoving) {
          this.maze.location = new Posn(0, 0);
          isUserMoving = true;
          isSearching = false;
          this.maze.repaint();
        }
        this.maze.moveTowards("left", this.mazeWidth, this.mazeHeight);
        break;
      case "up": // Move the User up
        if (!isUserMoving) {
          this.maze.location = new Posn(0, 0);
          isUserMoving = true;
          isSearching = false;
          this.maze.repaint();
        }
        this.maze.moveTowards("up", this.mazeWidth, this.mazeHeight);
        break;
      case "down": // Move the User down
        if (!isUserMoving) {
          this.maze.location = new Posn(0, 0);
          isUserMoving = true;
          isSearching = false;
          this.maze.repaint();
        }
        this.maze.moveTowards("down", this.mazeWidth, this.mazeHeight);
        break;
      case "right": // Move the User to the right
        if (!isUserMoving) {
          this.maze.location = new Posn(0, 0);
          isUserMoving = true;
          isSearching = false;
          this.maze.repaint();
        }
        this.maze.moveTowards("right", this.mazeWidth, this.mazeHeight);
        break;
      default:
        break;
    }
  }
}

/**
 * Examples and tests for WorldState file
 */
class ExamplesWorldState {
  int width;
  int height;
  WorldState world;

  void setUp() {
    width = 30;
    height = 30;
    world = new WorldState(width, height, 0);
  }

  public void testBigBangNoBias(Tester t) {
    int width = 5;
    int height = 5;
    WorldState world = new WorldState(width, height, 0);
    world.bigBang(
            width * world.scale + world.panelWidth,
            height * world.scale,
            0.01);
  }

  public void testBigBangVertBias(Tester t) {
    int width = 30;
    int height = 30;
    WorldState world = new WorldState(width, height, 1);
    world.bigBang(
            width * world.scale + world.panelWidth,
            height * world.scale,
            0.01);
  }

  public void testBigBangHorizBias(Tester t) {
    int width = 30;
    int height = 30;
    WorldState world = new WorldState(width, height, -1);
    world.bigBang(
            width * world.scale + world.panelWidth,
            height * world.scale,
            0.01);
  }

  public void testOnKeyReleasedPaused(Tester t) {
    setUp();
    world.onKeyReleased(" ");
    world.makeScene().saveImage("OnKeyReleasedPaused.png");
  }

  public void testOnKeyReleasedNewMaze(Tester t) {
    setUp();
    world.makeScene().saveImage("OnKeyReleasedOriginalMaze.png");
    world.onKeyReleased("n");
    world.makeScene().saveImage("OnKeyReleasedNewMaze.png");
  }

  public void testOnKeyReleasedDFS(Tester t) {
    setUp();
    world.onKeyReleased("d");
    // Unfortunately, this would not render since DFS can only be demonstrated with onTick() as
    // the bigBang progresses
    world.makeScene().saveImage("OnKeyReleasedDFS.png");
  }

  public void testOnKeyReleasedBFS(Tester t) {
    setUp();
    world.onKeyReleased("b");
    // Unfortunately, this would not render since BFS can only be demonstrated with onTick() as
    // the bigBang progresses
    world.makeScene().saveImage("OnKeyReleasedBFS.png");
  }

  // *For the moves images below, we are unable to demonstrate that User is not permitted to move
  // past walls. This is due to the face that the Maze is not rendered until the bigBang is
  // initiated. Hence, you can test this behavior in the bigBang, but it cannot be demonstrated
  // in a simple image.
  public void testOnKeyReleasedMoveRight(Tester t) {
    setUp();
    world.onKeyReleased("right");
    world.makeScene().saveImage("OnKeyReleasedMoveRight.png");
    world.onKeyReleased("right");
    world.makeScene().saveImage("OnKeyReleasedMoveRightRight.png");
  }

  public void testOnKeyReleasedMoveLeft(Tester t) {
    setUp();
    world.onKeyReleased("left");
    world.makeScene().saveImage("OnKeyReleasedMoveLeft.png");
    world.onKeyReleased("left");
    world.makeScene().saveImage("OnKeyReleasedMoveLeftLeft.png");
  }

  public void testOnKeyReleasedMoveDown(Tester t) {
    setUp();
    world.onKeyReleased("down");
    world.makeScene().saveImage("OnKeyReleasedMoveDown.png");
    world.onKeyReleased("down");
    world.makeScene().saveImage("OnKeyReleasedMoveDownDown.png");
  }

  public void testOnKeyReleasedMoveUp(Tester t) {
    setUp();
    world.onKeyReleased("up");
    world.makeScene().saveImage("OnKeyReleasedMoveUp.png");
    world.onKeyReleased("up");
    world.makeScene().saveImage("OnKeyReleasedMoveUpUp.png");
  }

  public void testOnKeyReleasedcolorDisplay(Tester t) {
    setUp();
    world.onKeyReleased("h");
    world.makeScene().saveImage("OnKeyReleasedcolorDisplayFromStart.png");
    world.onKeyReleased("h");
    world.makeScene().saveImage("OnKeyReleasedcolorDisplayFromEnd.png");
    // On the third "h" key released, the world should be reset to its original state
    world.onKeyReleased("h");
    world.makeScene().saveImage("OnKeyReleasedcolorDisplayDisable.png");
  }

  public void testOnKeyReleasedClear(Tester t) {
    setUp();
    world.onKeyReleased("right");
    world.onKeyReleased("right");
    // Original maze after User moves
    world.makeScene().saveImage("OnKeyReleasedClearMoved.png");
    world.onKeyReleased("c");
    // Altered maze after it is cleared
    world.makeScene().saveImage("OnKeyReleasedClearMoves.png");
  }

  public void testOnKeyReleasedToggle(Tester t) {
    setUp();
    world.onKeyReleased("right");
    world.onKeyReleased("right");
    // Original maze after User moves
    world.makeScene().saveImage("OnKeyReleasedToggleMoved.png");
    world.onKeyReleased("t");
    // Altered maze after it is toggled
    world.makeScene().saveImage("OnKeyReleasedTogglePaths.png");
  }

  public void testReset(Tester t) {
    setUp();
    world.onKeyReleased("down");
    world.onKeyReleased("down");
    // Original maze after User moves
    world.makeScene().saveImage("ResetMoved.png");
    world.reset();
    // Altered maze after it is reset
    world.makeScene().saveImage("Reset.png");
  }

  public boolean testSkip(Tester t) {
    setUp();
    world.onTick();
    world.onTick();
    int countHere = world.tickCount;
    world.onKeyReleased("s");
    return t.checkExpect(world.tickCount, 901)
            && t.checkExpect(countHere, 2);
  }
}