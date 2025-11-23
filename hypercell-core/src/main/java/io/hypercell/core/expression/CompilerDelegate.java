package io.hypercell.core.expression;

import org.antlr.v4.runtime.tree.ParseTree;
import io.hypercell.api.Expression;

public interface CompilerDelegate {
    Expression compile(ParseTree tree, CompileContext cc);
}
