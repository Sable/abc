package org.jastadd.plugin.jastaddj.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.IShowInTarget;
import org.jastadd.plugin.Activator;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.util.BuildUtil;
import org.jastadd.plugin.jastaddj.util.EditorUtil;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.registry.IASTRegistryListener;
import org.jastadd.plugin.ui.view.AbstractBaseExplorer;
import org.jastadd.plugin.ui.view.JastAddContentProvider;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;


@SuppressWarnings("restriction")
public class JastAddJExplorer extends AbstractBaseExplorer implements IShowInTarget {
	
	public static final String VIEW_ID = "org.jastadd.plugin.explore.JastAddJExplorer";

	
	private ITreeContentProvider contentProvider = new JastAddJContentProvider();
	
	private ILabelProvider labelProvider = new DecoratingLabelProvider(
			new DecoratingLabelProvider(new MyLabelProvider(), getPlugin()
					.getWorkbench().getDecoratorManager().getLabelDecorator()),
			new MyProblemLabelDecorator());

	private Map<IPath, IContainer> sourceRootMap;
	
	protected void initContentProvider(TreeViewer viewer) {
		viewer.setContentProvider(contentProvider);
	}

	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(labelProvider);
	}

	public IContainer findSourceRoot(IResource resource) {
		IPath resourcePath = resource.getFullPath();
		while (!resourcePath.isEmpty() && !resourcePath.isRoot()
				&& !sourceRootMap.containsKey(resourcePath)) {
			resourcePath = resourcePath.removeLastSegments(1);
		}
		return sourceRootMap.get(resourcePath);
	}
	

	public class JastAddJContentProvider extends BaseContentProvider implements IASTRegistryListener { 
		public JastAddJContentProvider() {
			super(new BaseWorkbenchContentProvider(),
					new JastAddContentProvider());
			Activator.getASTRegistry().addListener(this);
		}

		private void loadModelInfo() {
			sourceRootMap = new HashMap<IPath, IContainer>();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {				
				BuildUtil.addSourceRoots(project, sourceRootMap);
			}
		}
		
		@Override
		public void childASTChanged(IProject project, String key) {
			//updateIfExpanded(project, key);
			doViewerRefresh();
		}

		@Override
		public void projectASTChanged(IProject project) {
			//updateIfExpanded(project, null);
			doViewerRefresh();
		}
		
		/**
		 * Updates AST which have been expanded
		 * @param project The project which changed
		 * @param key The key of the concerned AST
		 */
		/*
		protected void updateIfExpanded(IProject project, String key) {
			
			TreeViewer viewer = getTreeViewer();
			
			// Locate project among expanded paths
			TreePath[] path = viewer.getExpandedTreePaths();
			for (int i = 0; i < path.length; i++) {
				
				// Walk through path
				for (int j = 0; j < path[i].getSegmentCount(); j++) {
					Object obj = path[i].getSegment(j);
					// Check project
					if (obj instanceof IProject) {
						IProject p = (IProject)obj;
						// If this is an other project skip this path
						if (!p.equals(project)) {
							break;
						}
					}
					// For the first node of type IASTNode 
					if (obj instanceof IASTNode) {
						IASTNode node = (IASTNode)obj;
						// If the node as a lookup. Update if there's 
						// a key match or if there's no key
						if (node.hasLookupKey() && (key == null
								|| node.lookupKey().equals(key))) {
							Object parent = i > 0 ? path[i].getSegment(j-1) :  node;
							// Refresh the viewer from this root and down wards
							viewer.refresh(parent);
							break;
						}
					}
				}
			}
		}
		*/
		
		


		private void releaseModelInfo() {				
			sourceRootMap = null;
		}

		private void reloadModelInfo() {
			releaseModelInfo();
			loadModelInfo();
		}

		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
			super.inputChanged(viewer, oldInput, newInput);
			reloadModelInfo();
		}

		public void dispose() {
			releaseModelInfo();
			super.dispose();
		}
	}

	private class MyLabelProvider extends BaseLabelProvider {

		public MyLabelProvider() {
			super(new WorkbenchLabelProvider(), new JastAddLabelProvider());
		}

		protected Image getSourceRootItemImage(IContainer sourceRoot, IResource resource) {
			if (resource instanceof IContainer && !sourceRoot.equals(resource)) {
				return JavaPluginImages
						.get(((ITreeContentProvider) getViewer()
								.getContentProvider()).hasChildren(resource) ? JavaPluginImages.IMG_OBJS_PACKAGE
								: JavaPluginImages.IMG_OBJS_EMPTY_PACKAGE);
			} else
				return super.getSourceRootItemImage(sourceRoot, resource);
		}
	}

	private class MyProblemLabelDecorator extends BaseProblemLabelDecorator {
		@Override
		public Image decorateImage(Image image, Object element) {
			if (element instanceof Integer)
				return decorateImage(image, (int)(Integer) element);
			return super.decorateImage(image, element);
		}

		protected Image decorateImage(Image image, int severity) {
			int adornmentFlags;
			if (severity == IMarker.SEVERITY_ERROR)
				adornmentFlags = JavaElementImageDescriptor.ERROR;
			else if (severity == IMarker.SEVERITY_WARNING)
				adornmentFlags = JavaElementImageDescriptor.WARNING;
			else
				return image;

			ImageDescriptor imageDescriptor = new ImageImageDescriptor(image);
			Rectangle bounds = image.getBounds();
			return JavaPlugin.getImageDescriptorRegistry().get(
					new JavaElementImageDescriptor(imageDescriptor,
							adornmentFlags, new Point(bounds.width,
									bounds.height)));
		}
	}

	private String[] extension = new String[] { "*.class" };
	
	@Override
	protected boolean filterInView(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile)resource;
			for (int i = 0; i < extension.length; i++) {
				if (file.getFileExtension().equals(extension[i])) {
					return true;
				}
			}
			// Files starting with a dot
			if (file.getFullPath().lastSegment().startsWith("."))
				return true;
		} else if (resource instanceof IFolder) {
			IFolder folder = (IFolder)resource;
			if (folder.getName().equals("bin")) {
				return true;	
			}
		}
		return false;
	}

	@Override
	protected IResource getParentResourceForNode(IJastAddNode node) {
		return FileUtil.getFile(node);
	}

	@Override
	protected void openEditorForNode(IJastAddNode node) {
		FileUtil.openFile(node);
	}

	@Override
	protected void openFile(IFile file) {
		TreeViewer viewer = getTreeViewer();
		IEditorDescriptor descriptor = EditorUtil
				.getEditorDescription(file);
		OpenFileAction action = new OpenFileAction(getSite().getPage(),
				descriptor);
		action.selectionChanged((IStructuredSelection) viewer
				.getSelection());
		if (action.isEnabled())
			action.run();
	}
}
