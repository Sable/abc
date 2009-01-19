package org.jastadd.plugin.jastaddj.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.IShowInTarget;
import org.jastadd.plugin.compiler.ast.IJastAddNode;
import org.jastadd.plugin.jastaddj.util.BuildUtil;
import org.jastadd.plugin.jastaddj.util.EditorUtil;
import org.jastadd.plugin.jastaddj.util.FileUtil;
import org.jastadd.plugin.ui.view.AbstractBaseExplorer;
import org.jastadd.plugin.ui.view.JastAddContentProvider;
import org.jastadd.plugin.ui.view.JastAddLabelProvider;

public class JastAddJExplorer extends AbstractBaseExplorer implements
		IShowInTarget {
	public static final String VIEW_ID = "org.jastadd.plugin.explore.JastAddJExplorer";

	private ITreeContentProvider contentProvider = new MyContentProvider();
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

	public class MyContentProvider extends BaseContentProvider { //implements JastAddModelListener {
		public MyContentProvider() {
			super(new BaseWorkbenchContentProvider(),
					new JastAddContentProvider());
		}

		private void loadModelInfo() {
			sourceRootMap = new HashMap<IPath, IContainer>();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
					.getProjects();
			for (IProject project : projects) {
				
				/*
				JastAddJModel model = JastAddModelProvider.getModel(project,
						JastAddJModel.class);
				if (model == null)
					continue;
				model.addListener(this);
				*/
				
				BuildUtil.addSourceRoots(project, sourceRootMap);
			}
		}

		private void releaseModelInfo() {
			/*
			for (JastAddModel model : JastAddModelProvider.getModels())
					model.removeListener(this);
			*/
				
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

		public void modelChangedEvent() {
			reloadModelInfo();
			doViewerRefresh();
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
	}

	private String[] extension = new String[] { ".project", "*.java.dummy", "*.class" };
	
	@Override
	protected boolean filterInView(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile)resource;
			for (int i = 0; i < extension.length; i++) {
				if (file.getFileExtension().equals(extension[i])) {
					return true;
				}
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
