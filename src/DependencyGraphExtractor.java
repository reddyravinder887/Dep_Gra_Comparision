import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
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
        CompilationUnit compilationUnit = JavaParser.parse(file);
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
                    for (NameExpr expr : n.getExtendedTypes()) {
                        String extendedType = expr.getNameAsString();
                        dependencies.add(new Pair<>(classFullName, currentPackage + "." + extendedType));
                    }
                }
                if (n.getImplementedTypes().size() > 0) {
                    for (NameExpr expr : n.getImplementedTypes()) {
                        String implementedType = expr.getNameAsString();
                        dependencies.add(new Pair<>(classFullName, currentPackage + "." + implementedType));
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
                for (NameExpr expr : n.getImplementedTypes()) {
                    String implementedType = expr.getNameAsString();
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
		        @Override
        public void visit(MethodDeclaration n, Void arg) {
            String returnType = n.getTypeAsString();
            if (n.getBody().isPresent()) {
                n.getBody().get().accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(NameExpr n, Void arg) {
                        Optional<String> qualifier = n.getQualifier();
                        if (qualifier.isPresent()) {
                            dependencies.add(new Pair<>(currentPackage + "." + returnType, currentPackage + "." + qualifier.get()));
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

}

public static void main(String[] args) {
    DependencyGraphExtractor extractor = new DependencyGraphExtractor("D:\\Master Study\\Thesis\\SSE\\Source_Files\\AboutBlock.java");
    try {
        List<Pair<String, String>> dependencies = extractor.extractDependencies();
        // do something with the dependencies
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
}






