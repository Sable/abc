package org.jastadd.plugin.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;
import org.jastadd.plugin.FileTools;
import org.jastadd.plugin.JastAddModel;
import org.jastadd.plugin.editor.actions.FindDeclarationActionDelegate;

import AST.ASTNode;
import AST.CompilationUnit;

public class ResourceNavigator extends org.eclipse.ui.views.navigator.ResourceNavigator {
    protected void initFilters(TreeViewer viewer) {
    	super.initFilters(viewer);
    	ResourcePatternFilter filter = new ResourcePatternFilter();
    	filter.setPatterns(new String[] { ".project", "*.java.dummy", "*.class" });
        viewer.addFilter(filter);
    }
    
    protected void initContentProvider(TreeViewer viewer) {
        viewer.setContentProvider(new ContentProvider());
    }
    
    protected void initLabelProvider(TreeViewer viewer) {
        viewer.setLabelProvider(new LabelProvider(
                new WorkbenchLabelProvider(), getPlugin().getWorkbench()
                        .getDecoratorManager().getLabelDecorator()));
    }
    
    protected void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        Object element = selection.getFirstElement();
        
        TreeViewer viewer = getTreeViewer();
        boolean expandable = viewer.isExpandable(element) && !(element instanceof IFile);
        if(element instanceof ASTNode) {
			ASTNode node = (ASTNode)element;
        	FileTools.openFile(node);
        }
        else if (expandable) {
            viewer.setExpandedState(element, !viewer.getExpandedState(element));
		} else if (selection.size() == 1 && (element instanceof IResource)
				&& ((IResource) element).getType() == IResource.PROJECT) {
			OpenResourceAction ora = new OpenResourceAction(getSite()
					.getShell());
			ora.selectionChanged((IStructuredSelection) viewer.getSelection());
			if (ora.isEnabled()) {
				ora.run();
			}
		}

    }

    
    static class ContentProvider extends WorkbenchContentProvider {
		public boolean hasChildren(Object element) {
			if(element instanceof IFile) {
				IFile file = (IFile)element;
				if(file.getFileExtension().equals("java"))
					return true;
			}
			else if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				return !node.outlineChildren().isEmpty();
			}
			return super.hasChildren(element);
		}

		public Object getParent(Object element) {
			if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				ASTNode parent = node.getParent();
				if (parent != null && parent.showInContentOutline())
					return parent;
				else getParent(parent);
			}
			return super.getParent(element);
		}

		public Object[] getChildren(Object element) {
			if(element instanceof IFile) {
				IFile file = (IFile)element;
				if(file.getFileExtension().equals("java")) {
					CompilationUnit unit = JastAddModel.getInstance().buildFile(file);
					return new Object[] { unit };
				}
			}
			else if(element instanceof ASTNode) {
				ASTNode node = (ASTNode)element;
				return node.outlineChildren().toArray();
			}
			return super.getChildren(element);
		}

    }
    
    static class LabelProvider extends DecoratingLabelProvider {
        public LabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
        	super(provider, decorator);
        }
		public String getText(Object element) {
			if (element instanceof ASTNode) {
				return ((ASTNode)element).contentOutlineLabel();
			} else if (element instanceof String) {
				return (String)element;
			}
			return super.getText(element);
		}
		public Image getImage(Object element) {
			if(element instanceof ASTNode) {
			  String imageKey = org.eclipse.ui.ide.IDE.SharedImages.IMG_OPEN_MARKER;
			  return imageKey == null ? null : PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
			return super.getImage(element);
		}
    }

}
