/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.MethodLikeNode;
import net.sourceforge.pmd.lang.java.metrics.api.JavaOperationMetric;


/**
 * Base class for operation metrics. Can be applied on method and constructor declarations, and
 * lambda expressions.
 *
 * @author Clément Fournier
 */
public abstract class AbstractJavaOperationMetric extends net.sourceforge.pmd.lang.metrics.AbstractMetric<MethodLikeNode>
    implements JavaOperationMetric {

    /**
     * Returns true if the metric can be computed on this operation. By default, abstract operations are filtered out.
     *
     * @param node The operation
     *
     * @return True if the metric can be computed on this operation
     */
    @Override
    public boolean supports(MethodLikeNode node) {
        return !(node instanceof ASTMethodDeclaration && ((ASTMethodDeclaration) node).isAbstract());
    }


    /**
     * @see #supports(MethodLikeNode)
     * @deprecated Provided here for backwards binary compatibility with {@link #supports(MethodLikeNode)}.
     *             Please explicitly link your code to that method and recompile your code. Will be remove with 7.0.0
     */
    public boolean supports(ASTMethodOrConstructorDeclaration node) {
        return supports((MethodLikeNode) node);
    }


}
