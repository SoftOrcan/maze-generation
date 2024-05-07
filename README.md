# Maze Generation
Generate mazes using Kruskal’s algorithm, and solving them using either breadth- or depth-first searches.
Includes additional features, such as manual movement, heatmaps and maze construction animation, at the discretion of the user. 

Please refer to [Design Decisions.md](https://github.com/HarmonicOrcan/maze-generation/blob/main/Design%20Decisions.md)
for more detailed information. 

Contributions are made by Joe Portas ([@JoePortas05](https://github.com/JoePortas05) on GitHub).

## Running
Requires Java 11 - later versions _will not_ run the tester library correctly.

The two provided JARs (`javalib.jar` and `tester.jar`) should be added to the classpath when 
these are being run.

For run configurations, run the `tester.Main` class with one CLI argument: 
the name of the class with tests to run (starting with `Examples`,
and contain methods beginning with `test` that return boolean).

## Key Controls
Provided are the keys associated with maze navigation, generation and style:

**Miscellaneous:**
- Space Bar: Pause the bigBang
- Key `n`: Generate a new Maze without restarting the bigBang

**Automated Traversal:**
- Key `d`: Perform Depth-First Search traversal
- Key `b`: Perform Breath-First Search traversal

**Manual Traversal:**
- Left Arrow: Move to the left
- Right Arrow: Move to the right
- Down Arrow: Move down
- Up Arrow: Move up

**Maze Rendering Toggle:**
- Key `h`: Turn on the distance gradient; can alternate between distance from the Start or from the End
- Key `c`: Clear the Maze
- Key `s`: Skip the construction animation
- Key `t`: View the initial state of the Maze without clearing it; can alternate between the
  current travelled path or the aforementioned initial state.
