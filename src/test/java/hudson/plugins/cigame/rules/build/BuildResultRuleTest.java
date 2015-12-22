package hudson.plugins.cigame.rules.build;

import hudson.model.Build;
import hudson.model.Result;
import hudson.model.User;
import hudson.plugins.cigame.model.RuleResult;
import hudson.plugins.cigame.rules.build.BuildResultRule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BuildResultRuleTest {

    @Mock
    private Build build;

    @Test
    public void testFirstBuildSuccess() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.SUCCESS, null);
        assertThat("Successful build should give 100 results",  results.getPoints(), is((double) 100));
    }

    @Test
    public void testFirstBuildFailed() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.FAILURE, null);
        assertThat("Failed build should give -100 results", results.getPoints(), is((double) -100));
    }

    @Test
    public void testFirstBuildWasUnstable() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.UNSTABLE, null);
        assertNull("Unstable build should return null", results);
    }

    @Test
    public void testLastBuildWasUnstable() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.SUCCESS, Result.UNSTABLE);
        assertThat("Fixed build should give 100 results", results.getPoints(), is((double)100));
    }

    @Test
    public void testContinuedBuildFailure() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.FAILURE, Result.FAILURE);
        assertNull("No change in failure result should return null", results);
    }

    @Test
    public void testContinuedUnstableBuild() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.UNSTABLE, Result.UNSTABLE);
        assertNull("No change in usntable result should return null", results);
    }

    @Test
    public void testLastBuildWasAborted() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.FAILURE, Result.ABORTED);
        assertNull("Previous aborted build should return null", results);
    }

    @Test
    public void testContinuedBuildSuccess() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.SUCCESS, Result.SUCCESS);
        assertThat("No change in result should give 100 results", results.getPoints(), is((double)100));
    }

    @Test
    public void testCurrentBuildWasUnstable() {
        BuildResultRule rule = new BuildResultRule(100, -100);
        RuleResult results = rule.evaluate(Result.UNSTABLE, Result.SUCCESS);
        assertNull("Unstable builds should return null", results);
    }

    @Test
    public void testMultiUserBuildFailure(){
        BuildResultRule rule = new BuildResultRule(100, -100);
        when(build.getResult()).thenReturn(Result.FAILURE);
        Set<User> mockSet = new AbstractSet<User>() {
            @Override public Iterator<User> iterator() {
                return null;
            }
            @Override public int size() {
                return 2;
            }
        };
        when(build.getCulprits()).thenReturn(mockSet);
        assertEquals("MultiUser build that fails should return the message",Messages.BuildRuleSet_BuildFailedMultiUser(),rule.evaluate(build).getDescription());
    }
}
