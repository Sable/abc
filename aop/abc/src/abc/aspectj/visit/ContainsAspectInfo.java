package abc.aspectj.visit;

import abc.weaving.aspectinfo.*;

public interface ContainsAspectInfo {
    public void update(GlobalAspectInfo gai, Aspect current_aspect);
}
