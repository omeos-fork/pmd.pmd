/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchBranch;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchFallthroughBranch;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchLike;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.rule.internal.DataflowPass;
import net.sourceforge.pmd.lang.java.rule.internal.DataflowPass.DataflowResult;
import net.sourceforge.pmd.reporting.RuleContext;
import net.sourceforge.pmd.util.OptionalBool;

public class ImplicitSwitchFallThroughRule extends AbstractJavaRulechainRule {

    private static final Pattern IGNORED_COMMENT = Pattern.compile("/[/*].*\\bfalls?[ -]?thr(ough|u)\\b.*",
                                                                   Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public ImplicitSwitchFallThroughRule() {
        super(ASTSwitchStatement.class, ASTSwitchExpression.class);
    }

    @Override
    public Object visit(ASTSwitchStatement node, Object data) {
        checkSwitchLike(node, asCtx(data));
        return null;
    }

    @Override
    public Object visit(ASTSwitchExpression node, Object data) {
        checkSwitchLike(node, asCtx(data));
        return null;
    }

    private void checkSwitchLike(ASTSwitchLike node, RuleContext ruleContext) {
        DataflowResult dataflow = DataflowPass.getDataflowResult(node.getRoot());

        for (ASTSwitchBranch branch : node.getBranches()) {
            if (branch instanceof ASTSwitchFallthroughBranch && branch != node.getLastChild()) {
                ASTSwitchFallthroughBranch fallthrough = (ASTSwitchFallthroughBranch) branch;
                OptionalBool bool = dataflow.switchBranchFallsThrough(branch);
                if (bool != OptionalBool.NO
                        && fallthrough.getStatements().nonEmpty()
                        && !nextBranchHasComment(branch)) {
                    ruleContext.addViolation(branch.getNextBranch().getLabel());
                }
            } else {
                return;
            }
        }
    }

    boolean nextBranchHasComment(ASTSwitchBranch branch) {
        JavaNode nextBranch = branch.getNextBranch();
        if (nextBranch == null) {
            return false;
        }
        for (JavaccToken special : GenericToken.previousSpecials(nextBranch.getFirstToken())) {
            if (JavaAstUtils.isComment(special)
                && IGNORED_COMMENT.matcher(special.getImageCs()).find()) {
                return true;
            }
        }
        return false;
    }
}
