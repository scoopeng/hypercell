package io.hypercell.core.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import io.hypercell.formula.HyperCellExpressionLexer;
import io.hypercell.formula.HyperCellExpressionParser;
import io.hypercell.core.grid.MemSheet;
import io.hypercell.api.EvaluationContext;
import io.hypercell.api.FunctionRegistry;
import io.hypercell.api.Function;
import io.hypercell.api.Expression;

import io.hypercell.formula.HyperCellExpressionParser.*;

public class Compile {
    private static final Logger logger = LoggerFactory.getLogger(Compile.class);
    private final ParseTree tree;
    private Expression exp;
    private final CompileContext cc;
    private final FunctionRegistry registry;

    public Compile(String formula, MemSheet sheet, FunctionRegistry registry) {
        CharStream input = CharStreams.fromString(formula);
        HyperCellExpressionLexer lex = new HyperCellExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        HyperCellExpressionParser parser = new HyperCellExpressionParser(tokens);
        
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

    private void compile() {
        if (tree instanceof StartContext) {
            Compile c = new Compile(tree.getChild(0), cc, registry);
            exp = c.getExpression();
        } else if (tree instanceof PARENContext) {
            Compile c = new Compile(tree.getChild(1), cc, registry);
            exp = c.getExpression();
        } else if (tree instanceof UMINUSContext) {
            exp = new UnaryOperator(tree.getChild(0), tree.getChild(1), cc);
        } else if (tree instanceof ADDOPContext || tree instanceof MULOPContext || tree instanceof COMPOPPContext
                || tree instanceof POWERContext || tree instanceof CONCATOPPContext) {
            exp = new BinaryOperator(tree.getChild(0), tree.getChild(1), tree.getChild(2), cc);
        } else if (tree instanceof NUMBERContext) {
            ParseTree child = tree.getChild(0);
            if (child instanceof INTEGERVALContext || child instanceof DECIMALVALContext) {
                try {
                    exp = new SheetNumber(tree.getChild(0));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        } else if (tree instanceof STRINGContext) {
            exp = new SheetString(tree.getChild(0));
        } else if (tree instanceof CONSTANTContext) {
            exp = new SheetConstant(tree);
        } else if (tree instanceof REFContext) {
            Compile c = new Compile(tree.getChild(0), cc, registry);
            exp = c.getExpression();
        } else if (tree instanceof CELLContext) {
            Identifier id = new Identifier(tree.getChild(0), cc.getSheet());
            cc.addIdentifier(id);
            exp = id;
        } else if (tree instanceof ItemContext) {
            Identifier id = new Identifier(tree, cc.getSheet());
            cc.addIdentifier(id);
            exp = id;
        } else if (tree instanceof RangeContext) {
            exp = new Range(cc.getSheet(), tree);
            cc.addRange((Range) exp);
        } else if (tree instanceof RangeorreferenceContext) {
            Compile c = new Compile(tree.getChild(0), cc, registry);
            exp = c.getExpression();
        } else if (tree instanceof MATHContext || tree instanceof LOGICALContext || tree instanceof STATISTICALContext || tree instanceof TEXTUALContext) {
            ParseTree child = tree.getChild(0);
            handleFunction(child);
        } else if (tree instanceof GENERIC_FUNCTIONContext) {
            handleFunction(tree);
        }
    }
    
    private void handleFunction(ParseTree funcTree) {
        String funcName = "";
        if (funcTree.getChildCount() > 0) {
             ParseTree first = funcTree.getChild(0);
             if (first instanceof TerminalNodeImpl) {
                 funcName = first.getText().toUpperCase();
                 // Clean up name if needed (remove parens if they are part of token?)
                 // Lexer defines SUMTOKEN as 'SUM'.
                 // Generic function: IDENTIFIER '(' ...
                 if (funcTree instanceof GENERIC_FUNCTIONContext) {
                     // funcName is IDENTIFIER
                 }
             }
        }
        
        List<Expression> args = new ArrayList<>();
        for (int i = 0; i < funcTree.getChildCount(); i++) {
            ParseTree child = funcTree.getChild(i);
            if (child instanceof TerminalNodeImpl) continue; // Skip tokens
            // Skip if child is not an expression type we know?
            // We can recursively compile it.
            Compile c = new Compile(child, cc, registry);
            if (c.getExpression() != null) {
                args.add(c.getExpression());
            }
        }
        
        if (registry != null && registry.hasFunction(funcName)) {
            // We need Function interface to have execute
            // FunctionCallExpression takes Function, args, context
            // But FunctionRegistry.getFunction(name) returns Function.
            // Wait, my Registry interface had 'hasFunction' and 'execute'.
            // It should have 'getFunction'.
            // I defined FunctionRegistry earlier:
            // interface FunctionRegistry { Function getFunction(String name); void register(...); }
            // So I can use it.
            Function func = registry.getFunction(funcName);
            exp = new FunctionCallExpression(func, args, cc.getSheet()); // MemSheet implements EvaluationContext (need to ensure)
        } else {
            // Unknown function
            // Create Error expression?
        }
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
