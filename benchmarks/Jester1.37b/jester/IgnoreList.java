package jester;

import java.util.ArrayList;
import java.util.List;

public class IgnoreList {
	private static final String specialEndOfLineMarker = "\\n";
	private String contents;

	public IgnoreList(String ignoreFileContents) {
		contents = ignoreFileContents;
	}

	public List ignorePairs() throws ConfigurationException {
		List result = new ArrayList();
		String[] lines = contents.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.length()==0){
				continue;
			}
			char delimiter = line.charAt(0);
			String[] parts = line.split(""+delimiter);
			if (parts.length != 3){
				throw new ConfigurationException("could not parse ignore list line "+line);
			}
			String start = parts[1];
			String end = parts[2];
			if(end.equals(specialEndOfLineMarker)){
				end = "\n";
			}
			result.add(new IgnorePair(start, end));
		}
		return result;
	}
	
	public String toString() {
		try{
			return "IgnoreList ["+ignorePairs()+"]";
		}catch(ConfigurationException ex){
			return "Malformed IgnoreList";
		}
	}
}
