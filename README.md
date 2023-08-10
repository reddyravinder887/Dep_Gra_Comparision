# **Dependency Graph Comparision**
 
 
 
 ## **Main Goal:**

 
1.	Build a dependency graph from the Java import statements
2.	Derive the Dependency graph using the ground truth tools like Jdeps
3.	Evaluate the Build and Derived dependency graphs.



 ## **Things want to implement :**
 
1. Build a Dependency Graph from the Java import statements
      - Here, I need to develop a Java code, that should extract the dependency graph from the Java import statements from Java projects
      
2.  Derive the Dependency graph using the ground truth tools like Jdeps
      - Here, Using the tool already existing like Jdeps which we run on the same large Java projects to derive the dependency graphs.
 
  
## **Things want to Achieve :**
  
1. Evaluate the Build and Derived dependency graphs.
        - Once we got built and derived dependency graphs, now evaluate both dependency graphs with the help of a concept from graph theory called Graph Edit Distance (GED). 
        - GED will take two graphs and compare them using the methodologies and processes included in it. Once the comparison is done it will show the similarity in the form of a number. 

 ## Conclusion:
 
  By this similarity number, we will conclude in our research that whether the graphs are underestimated( the similarity number is too low) or overestimated (( the similarity number is too high)


## Reference:

  1. Dependency Graph [https://en.wikipedia.org/wiki/Dependency_graph]
  2. Jdeps [https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html]
  3. Graph Edit Distance [https://en.wikipedia.org/wiki/Graph_edit_distance]
