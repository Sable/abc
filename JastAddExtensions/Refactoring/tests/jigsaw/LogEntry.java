package tests.jigsaw;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import AST.Problem;
import AST.RefactoringException;

public class LogEntry {
	
	private String refactoring;
	private Map<String, String> parameters = new HashMap<String, String>();
	volatile private long start;
	volatile private long end;
	volatile private RefactoringException rfe = null;
	volatile private Throwable throwable;
	volatile private Collection<Problem> errors = Collections.emptyList();
	volatile private boolean timeout;
	volatile private int typeAccess;
	volatile private int methodAccess;
	volatile private int variableAccess;
	volatile private int insertedCasts;
	volatile private int accessibilityIssues;
	volatile private int typeIssues;
	
	public LogEntry(String refactoring) {
		this.refactoring = refactoring;
	}
	
	public void startsNow() {
		start = System.currentTimeMillis();
	}
		
	public long duration() {
		if(end == 0)
			return -1;
		return end-start;
	}
	
	public void addParameter(String parameter, String value) {
		parameters.put(parameter, value);
	}
	
	public void finished() {
		end = System.currentTimeMillis();
	}
	
	public void finished(RefactoringException rfe) {
		end = System.currentTimeMillis();
		this.rfe = rfe;
	}
	
	public void finished(Throwable throwable) {
		end = System.currentTimeMillis();
		this.throwable = throwable;
	}
	
	public void logTimeout() {
		end = System.currentTimeMillis();
		timeout = true;
	}
	
	public boolean timeout() {
		return timeout;
	}

	public void logErrors(Collection<Problem> errors) {
		this.errors = errors;
	}
	
	public void print(OutputStream out) {
		System.out.println(toString());
	}
	
	public void logTypeIssue() {
		this.typeIssues++;
	}

	public void logAdjustedTypeAccess() {
		typeAccess++;
	}

	public void logAdjustedMethodAccess() {
		methodAccess++;
	}

	public void logAdjustedVariableAccess() {
		variableAccess++;
	}
	
	public void logCastInserted() {
		insertedCasts++;
	}

	public void logAccessibilityIssues(int i) {
		accessibilityIssues = i;
	}

	public boolean wasSuccess() {
		return rfe == null && !timeout();
	}

	public boolean hasErrors() {
		return errors != null && errors.size()>0;
	}

	public boolean hasException() {
		return throwable != null;
	}

	public String errorMessage() {
		return (rfe == null ? "" : rfe.getMessage());
	}

	public String toString(){
		StringBuilder res = new StringBuilder();
		res.append(refactoring).append("; ");
		for(Map.Entry<String, String> parameter : parameters.entrySet())
			res.append(parameter.getKey()).append(":").append(parameter.getValue()).append(", ");
		res.append("; ")
		   .append(wasSuccess()).append("; ")
		   .append(rfe == null ? "" : (rfe.getMessage().contains("cannot satisfy") ? "cannot satisfy..." : rfe.getMessage())).append("; ");
		res.append(hasException() ? "Exception: " + throwable.getMessage().replaceAll(";", ",") : "").append("; ")
		   .append(timeout()).append("; ")
		   .append(hasErrors()).append("; ");
		for(Problem problem : errors)
			res.append(problem.toString().replaceAll(";",",").replaceAll(System.getProperty("line.separator"), "  ")).append(", ");
		res.append("; ")
		   .append(duration()).append("; ")
		   .append(typeAccess).append("; ")
		   .append(methodAccess).append("; ")
		   .append(variableAccess).append("; ")
		   .append(insertedCasts).append("; ")
		   .append(accessibilityIssues).append("; ")
		   .append(typeIssues).append("; ");
		return res.toString();
	}

	public static String header() {
		StringBuilder res = new StringBuilder();
		res.append("refactoring;")
		   .append("parameters;")
		   .append("success;")
		   .append("refactoring message;")
		   .append("exception message;")
		   .append("timeout;")
		   .append("errors;")
		   .append("error messages;")
		   .append("duration;")
		   .append("adjusted type accesses;")
		   .append("adjusted method accesses;")
		   .append("adjusted variable accesses;")
		   .append("casts inserted;")
		   .append("accessibility adjustments;")
		   .append("type adjustments;");
		return res.toString();
	}

}
