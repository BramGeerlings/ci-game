package hudson.plugins.cigame.rules.build;

import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.User;
import hudson.plugins.cigame.GameDescriptor;
import hudson.plugins.cigame.model.Rule;
import hudson.plugins.cigame.model.RuleResult;

import java.util.Set;

/**
 * Rule that gives points on the result of the build.
 */
public class BuildResultRule implements Rule {

    private int failurePoints;
    private int successPoints;

    public BuildResultRule() {
        this(1, -10);
    }

    public BuildResultRule(int successPoints, int failurePoints) {
        this.successPoints = successPoints;
        this.failurePoints = failurePoints;
    }

    public String getName() {
        return Messages.BuildRuleSet_BuildResult(); //$NON-NLS-1$
    }

    public RuleResult evaluate(AbstractBuild<?, ?> build) {
        Result result = build.getResult();
        Result lastResult = null;
        if(!Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getAllowMultiAuthorBreak() && //check the plug-in settings
                build.getCulprits().size() > 1 &&   //check if there's more than one person involved with the build
                build.getResult() == Result.FAILURE){   //check if the build is a failure
                    RuleResult ruleResult = new RuleResult(0.0,Messages.BuildRuleSet_BuildFailedMultiUser());
                    return ruleResult;
        }
        if (build.getPreviousBuild() != null) {
            lastResult = build.getPreviousBuild().getResult();
        }
        return evaluate(result, lastResult);
    }

    RuleResult evaluate(Result result, Result lastResult) {
        if (result == Result.SUCCESS) {
            return new RuleResult( successPoints, Messages.BuildRuleSet_BuildSuccess()); //$NON-NLS-1$
        }
        if (result == Result.FAILURE) {
            if ((lastResult == null)
                    || (lastResult.isBetterThan(Result.FAILURE))) {
                return new RuleResult(failurePoints, Messages.BuildRuleSet_BuildFailed()); //$NON-NLS-1$
            }
        }
        return null;
    }
}
