import java.util.*;

public class PatternMatcher {

    public boolean matchesClass() {
        return false;
    }

    private class AIMethodPattern
      {
        public boolean matchesExecution() {
            LinkedList ftypes = null;

            int skip_last = 0;

            while (skip_last-- > 0) ftypes.removeLast();

            return matchesClass();
        }

    }

    public static void main(String[] args) {
        PatternMatcher p = new PatternMatcher();
        p.go();
    }

    public void go (){
        AIMethodPattern a = new AIMethodPattern();
        a.matchesExecution();
    }


}
