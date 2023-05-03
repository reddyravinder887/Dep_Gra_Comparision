import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DependencyGraphExtractor {

    private String filePath;

    public DependencyGraphExtractor(String filePath) {
        this.filePath = filePath;
    }

    public List<Pair<String, String>> extractDependencies() throws FileNotFoundException {
        File file = new File(filePath);
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
         CompilationUnit compilationUnit = parseResult.getResult().orElse(null);
        DependencyVisitor visitor = new DependencyVisitor();
        visitor.visit(compilationUnit, null);
        return visitor.getDependencies();
    }

    private static class DependencyVisitor extends VoidVisitorAdapter<Void> {

        private List<Pair<String, String>> dependencies = new ArrayList<>();
        private String currentPackage;

        public List<Pair<String, String>> getDependencies() {
            return dependencies;
        }

        @Override
        public void visit(PackageDeclaration n, Void arg) {
            currentPackage = n.getName().toString();
            super.visit(n, arg);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (!n.isInterface()) {
                String className = n.getNameAsString();
                String classFullName = currentPackage + "." + className;
                if (n.getExtendedTypes().size() > 0) {
                    for (ClassOrInterfaceType extendedType : n.getExtendedTypes()) {
                        String typeName = extendedType.getNameAsString();
                        dependencies.add(new Pair<>(classFullName, currentPackage + "." + typeName));
                    }
                }
                if (n.getImplementedTypes().size() > 0) {
                    for (ClassOrInterfaceType implementedType : n.getImplementedTypes()) {
                        String typeName = implementedType.getNameAsString();
                        dependencies.add(new Pair<>(classFullName, currentPackage + "." + typeName));
                    }
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(EnumDeclaration n, Void arg) {
            String enumName = n.getNameAsString();
            String enumFullName = currentPackage + "." + enumName;
            if (n.getImplementedTypes().size() > 0) {
                for (ClassOrInterfaceType implementedType : n.getImplementedTypes()) {
                    String typeName = implementedType.getNameAsString();
                    dependencies.add(new Pair<>(enumFullName, currentPackage + "." + implementedType));
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(FieldDeclaration n, Void arg) {
            String type = n.getElementType().toString();
            if (n.getVariables().size() > 0) {
                for (VariableDeclarator var : n.getVariables()) {
                    if (var.getInitializer().isPresent() && var.getInitializer().get() instanceof ObjectCreationExpr) {
                        String createdType = ((ObjectCreationExpr) var.getInitializer().get()).getType().getName().toString();
                        dependencies.add(new Pair<>(currentPackage + "." + type, currentPackage + "." + createdType));
                    }
                }
            }
            super.visit(n, arg);
        }
		        @Override
        public void visit(MethodDeclaration n, Void arg) {
            String returnType = n.getTypeAsString();
            if (n.getBody().isPresent()) {
                n.getBody().get().accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(NameExpr n, Void arg) {
                        Optional<Node> parentNode = n.getParentNode();
                if (parentNode.isPresent() && parentNode.get() instanceof MethodCallExpr) {
                    MethodCallExpr methodCallExpr = (MethodCallExpr) parentNode.get();
                    if (methodCallExpr.getScope().isPresent()) {
                        String qualifier = methodCallExpr.getScope().get().toString();
                        dependencies.add(new Pair<>(currentPackage + "." + returnType, currentPackage + "." + qualifier));
                    }
                }
                        super.visit(n, arg);
                    }
                }, null);
                n.getBody().get().accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ObjectCreationExpr n, Void arg) {
                        String createdType = n.getType().getName().toString();
                        dependencies.add(new Pair<>(currentPackage + "." + returnType, currentPackage + "." + createdType));
                        super.visit(n, arg);
                    }
                }, null);
            }
            super.visit(n, arg);
        }
    }



public static void main(String[] args) {
    DependencyGraphExtractor extractor = new DependencyGraphExtractor("D:\\Master Study\\Thesis\\SSE\\Source_Files\\AbstractContractCreateTest.java");
    try {
        List<Pair<String, String>> dependencies = extractor.extractDependencies();
        for (Pair<String, String> dependency : dependencies) {
            System.out.println(dependency.getKey() + "  dependes on -> " + dependency.getValue());
        }
        // do something with the dependencies
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
}
}






