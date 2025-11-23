package io.hypercell.core.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hypercell.formula.HyperCellExpressionLexer;
import io.hypercell.formula.HyperCellExpressionParser;
import io.hypercell.core.grid.MemSheet;
import io.hypercell.api.FunctionRegistry;
import io.hypercell.api.Expression;

import java.util.List;

public class Compile {
    private static final Logger logger = LoggerFactory.getLogger(Compile.class);
    private final ParseTree tree;
    private Expression exp;
    private final CompileContext cc;
    private final FunctionRegistry registry;

    public Compile(String formula, MemSheet sheet, FunctionRegistry registry) {
        CharStream input = CharStreams.fromString(formula);
        io.hypercell.formula.HyperCellExpressionLexer lex = new HyperCellExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        io.hypercell.formula.HyperCellExpressionParser parser = new HyperCellExpressionParser(tokens);
        
        this.tree = parser.start();
        this.cc = new CompileContext(sheet, registry);
        this.registry = registry;
        compile();
    }

    public Compile(ParseTree tree, CompileContext cc, FunctionRegistry registry) {
        this.tree = tree;
        this.cc = cc;
        this.registry = registry;
        compile();
    }
    
    public Compile(ParseTree tree, CompileContext cc) {
        this.tree = tree;
        this.cc = cc;
        this.registry = cc.getRegistry();
        compile();
    }

    public Compile(String formula, CompileContext cc) {
        CharStream input = CharStreams.fromString(formula);
        io.hypercell.formula.HyperCellExpressionLexer lex = new HyperCellExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        io.hypercell.formula.HyperCellExpressionParser parser = new HyperCellExpressionParser(tokens);
        this.tree = parser.start();
        this.cc = cc;
        this.registry = cc.getRegistry();
        compile();
    }

    private void compile() {
        CompilerDelegate delegate = cc.getSheet().getWorkbook().getCompilerDelegate();
        if (delegate == null) {
            delegate = new StandardCompilerDelegate();
        }
        exp = delegate.compile(tree, cc);
    }

    public Expression getExpression() {
        return exp;
    }
    
    public List<Identifier> getIdentifiers() {
        return cc.getIdentifierList();
    }
    
    public List<Range> getRanges() {
        return cc.getRangeList();
    }
    
    public boolean isInformationalOnly() {
        return cc.isInformationalOnly();
    }
}
