package org.jastadd.plugin.jastaddj.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.IShowInTarget;
import org.jastadd.plugin.explorer.JastAddBaseExplorer;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;
import org.jastadd.plugin.model.JastAddModelProvider;
import org.jastadd.plugin.providers.JastAddContentProvider;
import org.jastadd.plugin.providers.JastAddLabelProvider;

public class JastAddJExplorer extends JastAddBaseExplorer implements
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

	public class MyContentProvider extends BaseContentProvider implements JastAddModelListener {
		public MyContentProvider() {
			super(new BaseWorkbenchContentProvider(),
					new JastAddContentProvider());
		}

		private void loadModelInfo() {
			sourceRootMap = new HashMap<IPath, IContainer>();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
					.getProjects();
			for (IProject project : projects) {
				JastAddJModel model = JastAddModelProvider.getModel(project,
						JastAddJModel.class);
				if (model == null)
					continue;
				model.addListener(this);
				model.addSourceRoots(project, sourceRootMap);
			}
		}

		private void releaseModelInfo() {
			for (JastAddModel model : JastAddModelProvider.getModels())
					model.removeListener(this);
				
			sourceRootMap = null;
		}

		private void reloadModelInfo() {
			releaseModelInfo();
			loadModelInfo();
		}

		public /*synchronized*/ void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
			super.inputChanged(viewer, oldInput, newInput);
			reloadModelInfo();
		}

		public /*synchronized*/ void dispose() {
			releaseModelInfo();
			super.dispose();
		}

		public /*synchronized*/ void modelChangedEvent() {
			reloadModelInfo();
			doViewerRefresh();
		}
	}

	private class MyLabelProvider extends BaseLabelProvider {

		public MyLabelProvider() {
			super(new WorkbenchLabelProvider(), new JastAddLabelProvider());
		}

		protected Image getSourceRootItemImage(IContainer sourceRoot,
				IResource resource) {
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
}
