package hudson.plugins.cigame.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;

import java.io.PrintStream;
import java.util.*;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.User;
import hudson.plugins.cigame.rules.build.Messages;
import org.junit.Test;

public class ScoreCardTest {

    @Test(expected=IllegalStateException.class)
    public void testIllegalStateThrownInGetScores() {
        ScoreCard sc = new ScoreCard();
        sc.getScores();
    }

    @Test
    public void assertThatEmptyRuleResultIsNotUsed() {
        Rule rule = mock(Rule.class);
        when(rule.evaluate(isA(AbstractBuild.class))).thenReturn(RuleResult.EMPTY_RESULT);
        ScoreCard card = new ScoreCard();
        card.record(mock(AbstractBuild.class), new RuleSet("test", Arrays.asList(new Rule[]{rule})), null);
        assertThat(card.getScores().size(), is(0));
    }
    
    @Test
    public void assertRuleNull(){
    	List<Rule> liste = new ArrayList<Rule>();
    	liste.add(null);
    	ScoreCard card = new ScoreCard();
    	card.record(mock(AbstractBuild.class), new RuleSet("test", liste), null);
    }
    
    @Test
    public void assertEmptyRuleBookDoesNotThrowIllegalException() {
        ScoreCard scoreCard = new ScoreCard();
        scoreCard.record(mock(AbstractBuild.class), new RuleBook(), null);
        assertThat(scoreCard.getTotalPoints(), is(0d));
    }

    @Test
    public void assertNonDescriptRulesAreCaught(){
        ScoreCard scoreCard = new ScoreCard();
        AbstractBuild build = (mock(AbstractBuild.class));
        BuildListener listener = (mock(BuildListener.class));
        RuleResult result = new RuleResult(0,"test");
        Rule rule = mock(Rule.class);
        PrintStream ps = (mock(PrintStream.class));
        when(listener.getLogger()).thenReturn(ps);
        when(rule.evaluate(isA(AbstractBuild.class))).thenReturn(result);
        Set<User> mockSet = new AbstractSet<User>() {
            @Override public Iterator<User> iterator() {
                return null;
            }
            @Override public int size() {
                return 2;
            }
        };
        when(build.getCulprits()).thenReturn(mockSet);
        when(build.getResult()).thenReturn(Result.FAILURE);
        scoreCard.record(build,new RuleSet("test", Arrays.asList(new Rule[]{rule})),listener);
    }
}
