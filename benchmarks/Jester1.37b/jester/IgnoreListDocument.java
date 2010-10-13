package jester;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class IgnoreListDocument {
	public static final String FILE_NAME = "ignorelist.cfg";
	private IgnoreList ignoreList;
	private String source;
	private List ignoreRegions = new ArrayList();

	public IgnoreListDocument(String source, IgnoreList ignoreList) throws ConfigurationException {
		this.ignoreList = ignoreList;
		this.source = source;
		calculateIgnoreRegions();
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("IgnoreListDocument");
		if(ignoreRegions.size()>0){
			result.append(" with ignoreRegions:\n");
		}
		for (Iterator ignoreRegionIter = ignoreRegions.iterator(); ignoreRegionIter.hasNext();) {
			IgnoreRegion ignoreRegion = (IgnoreRegion) ignoreRegionIter.next();
			result.append(ignoreRegion.within(source)+"\n");
		}
		return result.toString();
	}
	
	public int length(){
		return source.length();
	}
	public char charAt(int index){
		char spaceWhenInsideIgnoreRegionToAvoidMisuse = ' ';
		return isInIgnoreRegion(index) ? spaceWhenInsideIgnoreRegionToAvoidMisuse : source.charAt(index);
	}
	//TODO used by report item - see if can be simplified
	public String substring(int start, int end){
		return source.substring(start, end);
	}
	
	public void writeOnto(Writer writer, int start, int end) throws IOException{
		writer.write(source, start, end);
	}

	private void calculateIgnoreRegions() throws ConfigurationException {	
		List ignorePairs = ignoreList.ignorePairs();
		for (Iterator iter = ignorePairs.iterator(); iter.hasNext();) {
			IgnorePair ignorePair = (IgnorePair) iter.next();
			calculateIgnoreRegions(ignorePair);
		}
	}

	private void calculateIgnoreRegions(IgnorePair ignorePair) {
		String start = ignorePair.getStart();
		String end = ignorePair.getEnd();
		
		int previousEndIndex = 0;
		while(true){
			int indexOfStart = source.indexOf(start, previousEndIndex);
			if (indexOfStart==-1){
				return;
			}
			int indexOfEnd = source.indexOf(end, indexOfStart);
			if (indexOfEnd==-1){
				return;
			}
			ignoreRegions.add(new IgnoreRegion(indexOfStart, indexOfEnd));
			previousEndIndex = indexOfEnd;
		}
	}

	public int indexOf(String string, int startIndex) {
		int index = source.indexOf(string, startIndex);
		if (index==-1){
			return index;
		}
		if (isInIgnoreRegion(index)){
			return indexOf(string, index+1);
		}else{
			return index;
		}
	}

	private boolean isInIgnoreRegion(int index) {
		for (Iterator iter = ignoreRegions.iterator(); iter.hasNext();) {
			IgnoreRegion ignoreRegion = (IgnoreRegion) iter.next();
			if (ignoreRegion.includes(index)){
				return true;
			}
		}
		return false;
	}
}
