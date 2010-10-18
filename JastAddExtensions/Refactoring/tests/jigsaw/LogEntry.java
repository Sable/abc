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
	private long start;
	private long end;
	private Map<String, String> parameters = new HashMap<String, String>();
	private RefactoringException rfe = null;
	private Throwable throwable;
	private Collection<Problem> errors = Collections.emptyList();
	private boolean timeout;
	private boolean accessibilityIssues;
	private boolean nameIssues;
	private boolean typeIssues;
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
	
	public void typeIssues(boolean typeIssues) {
		this.typeIssues = typeIssues;
	}

	public void accessibilityIssues(boolean accessibilityIssues) {
		this.accessibilityIssues = accessibilityIssues;
	}

	public void nameIssues(boolean nameIssues) {
		this.nameIssues = nameIssues;
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

	public String toString(){
		StringBuilder res = new StringBuilder();
		res.append(refactoring).append("; ");
		for(Map.Entry<String, String> parameter : parameters.entrySet())
			res.append(parameter.getKey()).append(":").append(parameter.getValue()).append(", ");
		res.append("; ")
		   .append(wasSuccess()).append("; ")
		   .append(rfe == null ? "" : rfe.getMessage()).append("; ")
		   .append(hasException() ? throwable.getMessage() : "").append("; ")
		   .append(timeout()).append("; ")
		   .append(hasErrors()).append("; ");
		for(Problem problem : errors)
			res.append(problem.toString()).append(", ");
		res.append("; ")
		   .append(duration()).append("; ")
		   .append(accessibilityIssues).append("; ")
		   .append(nameIssues).append("; ")
		   .append(typeIssues).append("; ");
		return res.toString();
	}

	public static String header() {
		StringBuilder res = new StringBuilder();
		res.append("refactoring; ")
		   .append("parameters; ")
		   .append("success; ")
		   .append("refactoring message; ")
		   .append("exception message; ")
		   .append("timeout; ")
		   .append("errors; ")
		   .append("error messages; ")
		   .append("duration ;")
		   .append("accessibility issues ;")
		   .append("naming issues ;")
		   .append("type issues ;");
		return res.toString();
	}
}
