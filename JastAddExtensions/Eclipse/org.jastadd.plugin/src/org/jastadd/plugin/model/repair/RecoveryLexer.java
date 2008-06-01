package org.jastadd.plugin.model.repair;

import java.util.*;

public interface RecoveryLexer {
	public SOF parse(StringBuffer buf);
}
