# Maze Generation
Working with graphs and graph algorithms by designing mazes using Kruskal’s algorithm, and 
solving them using either breadth- or depth-first searches.

Contributions are made by Joe Portas ([@JoePortas05](https://github.com/JoePortas05) on GitHub).

## Running
Requires Java 11 - later versions _will not_ run the tester library correctly.

The two provided JARs (`javalib.jar` and `tester.jar`) should be added to the classpath when 
these are being run.

For run configurations, run the `tester.Main` class with one CLI argument: 
the name of the class with tests to run (starting with `Examples`,
and contain methods beginning with `test` that return boolean).

## Key Controls
Provided are the design decisions we have made in completion of our
Maze Generation project. Specifically, the keys associated with maze navigation,
generation and style:

**Miscellaneous:**
- Space Bar: pause the bigBang
- Key `n`: generate a new Maze without restarting the bigBang

**Automated Traversal:**
- Key `d`: perform DFS algorithm traversal
- Key `b`: perform BFS algorithm traversal

**Manual Traversal:**
- Left Arrow: move to the left
- Right Arrow: move to the right
- Down Arrow: move down
- Up Arrow: move up

**Maze Rendering Toggle:**
- Key `h`: turn on the distance gradient; can alternate between distance from the Start or from the End
- Key `c`: clear the Maze
- Key `s`: skip the construction animation
- Key `t`: view the initial state of the Maze without clearing it; can alternate between the
  current travelled path or the aforementioned initial state.
