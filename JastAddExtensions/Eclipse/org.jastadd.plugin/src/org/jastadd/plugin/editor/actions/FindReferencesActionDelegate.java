package org.jastadd.plugin.editor.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.search.JastAddSearchResult;
import org.jastadd.plugin.search.JastAddSearchResultPage;

import AST.ASTNode;
import AST.TypeDecl;

public class FindReferencesActionDelegate implements IEditorActionDelegate {

	private IEditorPart editorPart;
	private ASTNode selectedNode;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editorPart = targetEditor;
	}

	public void run(IAction action) {
		if (editorPart != null) {
			
			if (selectedNode != null) {
				/*
				Collection implementors = selectedNode.findImplementors();
				for(Iterator iter = implementors.iterator(); iter.hasNext(); ) {
					TypeDecl typeDecl = (TypeDecl)iter.next();
					System.out.println(typeDecl.typeName());
				}
				*/
				// Find the files and positions of referencing nodes
				/*
				ASTNode target = selectedNode.declaration();
				if(target != null) {
					EditorTools.openFile(target);
				}
				*/
				
				/*
				IWorkbenchPage activePage = SearchPlugin.getActivePage();
				if (activePage != null) {
					IViewPart viewPart = activePage.findView(JastAddSearchResultPage.SEARCH_ID);
					if (viewPart instanceof JastAddSearchResultPage) {
						JastAddSearchResultPage resultPage = (JastAddSearchResultPage)viewPart;
						try {
							activePage.showView(JastAddSearchResultPage.SEARCH_ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
				*/
				activateSearchView(true);
				
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			IEditorInput editorInput = editorPart.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
				IFile file = fileEditorInput.getFile();
				selectedNode = JastAddModel.getInstance().findNodeInDocument(file, textSelection.getOffset());
			}
		}
	}
	
	public ISearchResultViewPart activateSearchView(boolean useForNewSearch) {
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		
		String defaultPerspectiveId= NewSearchUI.getDefaultPerspectiveId();
		if (defaultPerspectiveId != null) {
			IWorkbenchWindow window= activePage.getWorkbenchWindow();
			if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
				try {
					activePage= PlatformUI.getWorkbench().showPerspective(defaultPerspectiveId, window);
				} catch (WorkbenchException ex) {
					// show view in current perspective
				}
			}
		}
		
		if (activePage != null) {
			try {
				
				ISearchResultViewPart viewPart= findLRUSearchResultView(activePage, useForNewSearch);
				/*
				String secondaryId= null;
				if (viewPart == null) {
					if (activePage.findViewReference(NewSearchUI.SEARCH_VIEW_ID) != null) {
						secondaryId= String.valueOf(++fViewCount); // avoid a secondary ID because of bug 125315
					}
				} else {
					secondaryId= viewPart.getViewSite().getSecondaryId();
				}
			    */
				return (ISearchResultViewPart) activePage.showView(NewSearchUI.SEARCH_VIEW_ID); //, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message); 
			}	
		}
		return null;
	}
	
	private ISearchResultViewPart findLRUSearchResultView(IWorkbenchPage page, boolean avoidPinnedViews) {
		/*
		boolean viewFoundInPage= false;
		for (Iterator iter= fLRUSearchViews.iterator(); iter.hasNext();) {
			SearchView view= (SearchView) iter.next();
			if (page.equals(view.getSite().getPage())) {
				if (!avoidPinnedViews || !view.isPinned()) {
					return view;
				}
				viewFoundInPage= true;
			}
		}
		if (!viewFoundInPage) {
		*/
			// find unresolved views
			IViewReference[] viewReferences= page.getViewReferences();
			for (int i= 0; i < viewReferences.length; i++) {
				IViewReference curr= viewReferences[i];
				if (NewSearchUI.SEARCH_VIEW_ID.equals(curr.getId()) && page.equals(curr.getPage())) {
					SearchView view= (SearchView) curr.getView(true);
					view.showSearchResult(new JastAddSearchResult());
					if (view != null && (!avoidPinnedViews || !view.isPinned())) {
						return view;
					}
					
				}
			}
		//}
		return null;
	}
}
