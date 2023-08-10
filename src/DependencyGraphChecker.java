import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DependencyGraphChecker {
    public static void main(String[] args) {
        String filePath = "D:\\Master Study\\Thesis\\SSE\\EightQueens.java"; // Replace with the path to your Java file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String className = "";
            Map<String, Set<String>> dependencies = new HashMap<>();
            Set<String> importedClasses = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("import")) {
                    // Extract the imported class or package
                    String importedItem = extractImportedItem(line);
                    if (!importedItem.isEmpty()) {
                        importedClasses.addAll(resolveImportedClasses(importedItem, filePath));
                    }
                } else if (line.contains("class")) {
                    // Extract the class name from the class definition
                    className = extractClassName(line);
                }
            }

            // Traverse the imported classes to find their dependencies
            for (String importedClass : importedClasses) {
                Set<String> classDependencies = findClassDependencies(importedClass, filePath);
                dependencies.put(importedClass, classDependencies);
            }

            // Print the dependency graph
            System.out.println("Class: " + className);
            System.out.println("Dependencies:");
            for (String importedClass : dependencies.keySet()) {
                Set<String> classDependencies = dependencies.get(importedClass);
                if (!classDependencies.isEmpty()) {
                    System.out.println(importedClass);
                    for (String dependency : classDependencies) {
                        System.out.println("    " + dependency);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractImportedItem(String importStatement) {
        // Extract the imported class or package from the import statement
        String[] tokens = importStatement.split("\\s+");
        if (tokens.length > 1) {
            return tokens[1].replace(";", "");
        }
        return "";
    }

    private static String extractClassName(String classDefinition) {
        // Extract the class name from the class definition line using a regular expression
        String[] tokens = classDefinition.split("\\s+");
        if (tokens.length > 1) {
            return tokens[1];
        }
        return "";
    }

    private static Set<String> findClassDependencies(String className, String filePath) throws IOException {
        Set<String> dependencies = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentClassName = "";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("import")) {
                    // Extract the imported class or package
                    String importedItem = extractImportedItem(line);
                    if (!importedItem.isEmpty()) {
                        Set<String> importedClasses = resolveImportedClasses(importedItem, filePath);
                        if (importedClasses.contains(className)) {
                            dependencies.add(currentClassName);
                        }
                    }
                } else if (line.contains("class")) {
                    // Extract the class name from the class definition
                    currentClassName = extractClassName(line);
                }
            }
        }

        return dependencies;
    }

    private static Set<String> resolveImportedClasses(String importedItem, String filePath) throws IOException {
        Set<String> importedClasses = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("class")) {
                    // Extract the class name from the class definition
                    String className = extractClassName(line);
                    if (!className.isEmpty() && isClassInPackage(className, importedItem)) {
                        importedClasses.add(className);
                    }
                }
            }
        }

        return importedClasses;
    }

    private static boolean isClassInPackage(String className, String packageName) {
        return className.startsWith(packageName + ".") || className.equals(packageName);
    }
}
