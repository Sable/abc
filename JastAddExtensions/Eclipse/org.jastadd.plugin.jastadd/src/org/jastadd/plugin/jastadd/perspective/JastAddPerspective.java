package org.jastadd.plugin.jastadd.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;
import org.jastadd.plugin.jastadd.explorer.JastAddExplorer;
import org.jastadd.plugin.jastaddj.search.JastAddJSearchResultPage;

public class JastAddPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
 		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float)0.75, editorArea);
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addShowViewShortcut(JastAddJSearchResultPage.SEARCH_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
 		
		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder.addView(JastAddExplorer.VIEW_ID);
		folder.addPlaceholder("org.jastadd.plugin.explore.*");
		folder.addPlaceholder(IPageLayout.ID_RES_NAV);
				
		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		outputfolder.addPlaceholder("org.jastadd.plugin.output.*");
		//outputfolder.addView(JastAddSearchResultPage.SEARCH_ID);
		
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

	}

}
