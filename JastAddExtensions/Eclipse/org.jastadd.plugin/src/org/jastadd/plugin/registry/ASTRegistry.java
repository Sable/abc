package org.jastadd.plugin.registry;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;

import org.jastadd.plugin.compiler.ast.IASTNode;

/**
 * The ASTRegistry maintains JastAdd ASTs.
 * 
 * @author emma
 *
 */
public class ASTRegistry {
	
	/**
	 * Each project ast is connected to a lock object which is used when the 
	 * ast is updated or a child ast is checked out.
	 */
	private class ASTEntry {
		private Object lock;
		private IASTNode ast;
		public ASTEntry(IASTNode ast) {
			this.ast = ast;
			lock = new Object();
		}
		public Object lock() {
			return lock;
		}
		public IASTNode ast() {
			return ast;
		}
		public void setRootAST(IASTNode ast) {
			this.ast = ast;
		}
		public IASTNode getChildAST(String key) {
			return ast.lookupChildAST(key);
		}
		public void updateChildAST(IASTNode node, String key) {
			IASTNode child = ast.lookupChildAST(key);
			if (child != null) {
				//synchronized(ast.treeLockObject()) {
					ast.flushAttributes();
					child.replaceWith(node);
				//}
			}
		}
	}

	// IProject as key?
	// One ASTEntry/project means one IRootAST per project which is a problem
	// when there are more than one compiler active for a project. Both compilers
	// will update the register with a root ast project pair. One way is to solve this
	// is to create a key object containing an IProject and a nature key indicating
	// which nature this IRootAST object refers to. This would create a connection between
	// compilers and natures. This might be a reasonable limitation.
	private HashMap<IProject,ASTEntry> astMap = new HashMap<IProject,ASTEntry>();

	/** 
	 * Lookups up an AST with in the given project with a matching key in the root node
	 * @param key The key of the root node in the AST
	 * @param project The project in which the corresponding file resides
	 * @return The matching AST or null if no AST was found
	 */
	public IASTNode lookupAST(String key, IProject project) {
		IASTNode node = null;
		if (astMap.containsKey(project)) {
			node = getAST(key, project);
		} else {
			try {
				project.build(IncrementalProjectBuilder.FULL_BUILD , org.jastadd.plugin.Builder.BUILDER_ID, null, null);
				if (astMap.containsKey(project))
					node = getAST(key, project);
				//return lookupAST(key, project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return node;
	}
	
	/** 
	 * Looks up an AST matching a key and project
	 * @param key The key to match
	 * @param project The project to match
	 * @return The AST found, if no match null
	 */
	private IASTNode getAST(String key, IProject project) {
		IASTNode node;
		ASTEntry entry = astMap.get(project);
		if (key == null) {
			node = entry.ast;
		} else {
			synchronized (entry.lock()) {
				node = entry.getChildAST(key);
			}
		}
		return node;
	}

	/**
	 * Updates the root AST for a project
	 * @param ast The new root AST for the project
	 * @param project The project for which a new AST has been constructed
	 */
	public void updateProjectAST(IASTNode ast, IProject project) {
		if (!ast.isProjectAST()) {
			// TODO Do something here
			return;
		}
		if (ast != null && astMap.containsKey(project)) {
			// We have an entry for this project
			ASTEntry entry = astMap.get(project);
			// We need to synchronize while we update the root AST 
			// to prevent look ups while we're updating
			synchronized (entry.lock()) {
				entry.setRootAST(ast);
			}
		} else {
			// We need to add a new entry for this project
			ASTEntry entry = new ASTEntry(ast);
			astMap.put(project, entry);
		}
		// Notify all listeners interested in this project
		notifyListeners(project);
	}

	/** 
	 * Updates a sub-AST in the root AST in the given project if there's a matching key.
	 * This is currently something we probably cannot support because of problems with attribute evaluation?
	 * 
	 * @param node The sub-AST to insert
	 * @param key The key to use when finding where to insert the new sub-AST
	 * @param file The project corresponding to the root AST
	 */
	public void updateAST(IASTNode node, String key, IFile file) {
		IProject project = file.getProject();
		if (node != null && astMap.containsKey(project)) {
			// We have an entry for this project
			ASTEntry entry = astMap.get(project);
			synchronized (entry.lock()) {
				entry.updateChildAST(node, key);
			}
			notifyListeners(project, key);
		} else {
			// We don't have an entry for this project
			// so we do nothing
		}
	}
	
	/**
	 * Discard all ASTs for a given project and notify listeners
	 * @param project The project to discard
	 */
	public void discardAST(IProject project) {
		if (astMap.containsKey(project)) {
			astMap.remove(project);
		}
		notifyListeners(project);
	}


	// Listeners
	
	private class ListenerEntry {
		private IProject project;
		private String key;
		private IASTRegistryListener listener;
		public ListenerEntry(IASTRegistryListener listener, IProject project, String key) {
			this.listener = listener;
			this.project = project;
			this.key = key;
		}
		public ListenerEntry(IASTRegistryListener listener) {
			this.listener = listener;
		}
		public IASTRegistryListener listener() {
			return listener;
		}
		public IProject project() {
			return project;
		}
		public String key() {
			return key;
		}
	}

	private ArrayList<ListenerEntry> listenerList = new ArrayList<ListenerEntry>();

	/**
	 * Registers the given listener.
	 * @param listener The listener class implementing IASTRegistryListener
	 * @param project If not null, only listen to events related to this project
	 * @param key If not null, only listen to events related to this project and key
	 */
	public void addListener(IASTRegistryListener listener, IProject project, String key) {
		ListenerEntry entry = new ListenerEntry(listener, project, key);
		synchronized (ASTRegistry.class) {
			listenerList.add(entry);
		}
	}
	
	/**
	 * Registers the given listener.
	 * @param listener The listener class implementing IASTRegistryListener
	 */
	public void addListener(IASTRegistryListener listener) {
		ListenerEntry entry = new ListenerEntry(listener);
		synchronized (ASTRegistry.class) {
			listenerList.add(entry);
		}
	}

	/**
	 * Removes the given listener.
	 * @param listener The listener to remove
	 */
	public void removeListener(IASTRegistryListener listener) {
		ListenerEntry[] l = new ListenerEntry[listenerList.size()];
		synchronized (ASTRegistry.class) {
			listenerList.toArray(l);
		}
		for (int i = 0; i < l.length; i++) {
			if (l[i].listener().equals(listener)) {
				synchronized (ASTRegistry.class) {
					listenerList.remove(l[i]);
				}
			}
		}
	}
	
	/**
	 * Notify listeners interested in changes related to this project.
	 * This might update SWT components which means we need to notify
	 * using Display.syncExec(Runnable). 
	 * @param project The project which has been updated
	 */
	private void notifyListeners(final IProject project) {
		final ListenerEntry[] l = new ListenerEntry[listenerList.size()];
		synchronized (ASTRegistry.class) {
			listenerList.toArray(l);
		}
		for (int i = 0; i < l.length; i++) {
			if (l[i].project() == null || l[i].project().equals(project)) {
					final int j = i;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							l[j].listener().projectASTChanged(project);
						}
					});
			}
		}
	}

	/**
	 * Notify listeners interested in changes related to this project and key.
	 * This might update SWT components which means we need to notify
	 * using Display.syncExec(Runnable). 
	 * @param project The project which has been updated
	 * @param key The key which has been updated.
	 */
	private void notifyListeners(final IProject project, final String key) {
		final ListenerEntry[] l = new ListenerEntry[listenerList.size()];
		synchronized (ASTRegistry.class) {
			listenerList.toArray(l);
		}
		for (int i = 0; i < l.length; i++) {
			if (l[i].project() == null || l[i].project().equals(project)) {
				if (l[i].key() == null || l[i].key().equals(key)) {
					final int j = i;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							l[j].listener().childASTChanged(project, key);
						}
					});
				}
			}
		}
	}
}