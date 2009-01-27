package org.jastadd.plugin.util;

import java.util.Arrays;

public class JastAddPair<First, Second> {
	public First first;
	public Second second;
	
	public JastAddPair(First first, Second second) {
		this.first = first;
		this.second = second;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object object) {
		if (!(object instanceof JastAddPair))
			return false;
		JastAddPair<First, Second> pair = (JastAddPair<First, Second>) object;
		return (this.first == null ? this.first == pair.first : this.first
				.equals(pair.first))
				&& (this.second == null ? this.second == pair.second
						: this.second.equals(pair.second));
	}
	
	public int hashCode() {
		return Arrays.hashCode(new Object[] {first, second});
	}	
}
