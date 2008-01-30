import java.util.logging.*;

public aspect DetectLogUsage {
    declare warning : call(void Logger.log(..))
	: "Consider Logger.logp() instead";
}
