package org.jastadd.plugin.builder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;

import AST.*;

public class JastAddBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "org.jastadd.plugin.jastaddBuilder";

	private static final String MARKER_TYPE = "org.jastadd.plugin.xmlProblem";

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}
	
	private static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		char[] chars = new char[1024];
		while(reader.read(chars) > -1){
			sb.append(String.valueOf(chars));	
		}
		reader.close();
		return sb.toString();
	}


	private void addMarker(IFile file, String message, int lineNumber, int columnNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			DefaultLineTracker t = new DefaultLineTracker();
			t.set(readTextFile(file.getRawLocation().toOSString()));
			int offset = t.getLineOffset(lineNumber-1);
			offset += columnNumber - 1;
			marker.setAttribute(IMarker.CHAR_START, offset);
			marker.setAttribute(IMarker.CHAR_END, offset+1);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		} catch (IOException e) {
		} catch (BadLocationException e) {
		}
	}
	
	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkFile(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			
			Program program = new Program();
			program.initBytecodeReader(new bytecode.Parser());
			program.initJavaParser(
		        new JavaParser() {
			          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
			            return new parser.JavaParser().parse(is, fileName);
			          }
		        }
			);
			program.initPackageExtractor(new scanner.JavaScanner());
			program.initOptions();
			program.addKeyValueOption("-classpath");
			program.addOptions(new String[] { file.getRawLocation().toOSString() });
			Collection files = program.files();
		      try {
		          for(Iterator iter = files.iterator(); iter.hasNext(); ) {
		            String name = (String)iter.next();
		            try {
		              program.addSourceFile(name);
		            } catch(LexicalError e) {
		              throw new LexicalError(name + ": " + e.getMessage());
		            }
		          }

		          for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
		            CompilationUnit unit = (CompilationUnit)iter.next();
		            if(unit.fromSource()) {
		              Collection errors = new LinkedList();
		              Collection warnings = new LinkedList();
		              unit.errorCheck(errors, warnings);
		              if(!errors.isEmpty()) {
		            	  for(Iterator i2 = errors.iterator(); i2.hasNext(); ) {
		            		  String error = (String)i2.next();
		            		  int index1 = error.indexOf(':');
		            		  String fileName = error.substring(0, index1);
		            		  int index2 = error.indexOf(':', index1 + 1);
	            			  int lineNumber = Integer.parseInt(error.substring(index1+1, index2).trim());
	            			  int index3 = error.indexOf(':', index2 + 1);
	            			  // skip string *** Semantic Error
	            			  //int index4 = error.indexOf(':', index3 + 1);
	            			  String message = error.substring(index3+1, error.length());
	            			  JastAddBuilder.this.addMarker(file, message, lineNumber, IMarker.SEVERITY_ERROR);
		            	  }
		                //processErrors(errors, unit);
		              }
		              else {
		                //processWarnings(warnings, unit);
		            	unit.java2Transformation();
		           	    unit.generateClassfile();
		           	    //run();
		              }
		            }
		          }
		        } catch (ParseError e) {
		        	// FileName.java: line, row\n *** Syntactic error: reason
		        	String error = e.getMessage();
		        	int index1 = error.indexOf(':');
		        	int index2 = error.indexOf(',', index1+1);
		        	int index3 = error.indexOf('\n', index2+1);
		        	int index4 = error.indexOf(':', index3+1);
		        	String fileName = error.substring(0, index1);
		        	int line = Integer.parseInt(error.substring(index1+1, index2).trim());
		        	int column = Integer.parseInt(error.substring(index2+1, index3).trim());
		        	String message = error.substring(index4 + 1, error.length());
		        	JastAddBuilder.this.addMarker(file, message, line, column, IMarker.SEVERITY_ERROR);
		        	
		        } catch (LexicalError e) {
		          System.err.println(e.getMessage());
		        } catch (Exception e) {
		          System.err.println(e.getMessage());
		          e.printStackTrace();
		        }
			
		}
	}
	
	private void run() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.jastadd.plugin.launchConfigurationType");
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "SampleConfig");
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "Java");
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "Hello");
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
			ILaunchConfiguration config = wc.doSave();	
			config.launch(ILaunchManager.DEBUG_MODE, null);
			//config.launch(ILaunchManager.RUN_MODE, null);
		} catch(CoreException e) {

		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new ResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new DeltaVisitor());
	}

	private class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkFile(resource);
			//return true to continue visiting children.
			return true;
		}
	}
	
	private class DeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkFile(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkFile(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}
}
