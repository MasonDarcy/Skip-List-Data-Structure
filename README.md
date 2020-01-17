# SkipListProject

Implementation of a Skiplist data structure with add and search functions. A data structure that supports O(log(n)) search and add
functionality.

## What is a Skip List?

A skip list is very similar to a linked list. However, when adding a node to a skip list, the program flips a coin to decide whether or 
not to add a new "highway" above the current linked list. This distinguishes the skip list as a probabilistic data structure.
These "lanes" can be used when searching the list to more quickly traverse the structure and find a desired node.

The lookup value of a regular skip list is O(n), but this implementation is an "indexable" version. Essentially, for every link, the 
skip list stores the "width" of the link. The width is defined as the number of bottom layer links being traversed by each of the higher
express links. This allows each layer to the be same total width, and the algorithm can intelligently take the shortest path to a desired
location in the structure.
