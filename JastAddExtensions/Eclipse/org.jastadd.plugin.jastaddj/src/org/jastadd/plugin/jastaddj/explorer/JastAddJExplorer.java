package org.jastadd.plugin.jastaddj.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.jastadd.plugin.AST.IJastAddNode;
import org.jastadd.plugin.AST.IOutlineNode;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration;
import org.jastadd.plugin.jastaddj.builder.JastAddJBuildConfiguration.SourcePathEntry;
import org.jastadd.plugin.jastaddj.model.JastAddJModel;
import org.jastadd.plugin.model.JastAddModel;
import org.jastadd.plugin.model.JastAddModelListener;
import org.jastadd.plugin.model.JastAddModelProvider;

public class JastAddJExplorer extends ResourceNavigator {
	public static final String VIEW_ID = "org.jastadd.plugin.explore.JastAddJExplorer";
	
	protected void initContentProvider(TreeViewer viewer) {
		viewer.setContentProvider(new MyContentProvider());
	}

	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new MyLabelProvider());
	}

	private class ProjectInfo {
		JastAddJModel model;
		JastAddJBuildConfiguration buildConfiguration;
	}

	private class SourceContainerInfo {
		JastAddJModel model;
		IProject project;
		IContainer sourceContainer;
		SourcePathEntry sourcePathEntry;
	}

	private Map<IProject, ProjectInfo> projectInfoMap;
	private Map<IPath, SourceContainerInfo> sourceContainerInfoMap;

	public IPath checkSourceResource(IResource resource) {
		List<String> segments = new ArrayList<String>();
		IPath path = resource.getFullPath();
		while (!path.isEmpty() && !path.isRoot()
				&& !sourceContainerInfoMap.containsKey(path)) {
			String segment = path.lastSegment();
			segments.add(0, segment);
			path = path.removeLastSegments(1);
		}
		if (!sourceContainerInfoMap.containsKey(path))
			return null;
		IPath result = new Path("");
		for (String segment : segments)
			result = result.append(segment);
		return result;
	}

	public class MyContentProvider implements ITreeContentProvider,
			IResourceChangeListener, JastAddModelListener {
		private Viewer viewer;

		public MyContentProvider() {
			super();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;
			IWorkspace oldWorkspace = null;
			IWorkspace newWorkspace = null;

			if (oldInput instanceof IWorkspace) {
				oldWorkspace = (IWorkspace) oldInput;
			} else if (oldInput instanceof IContainer) {
				oldWorkspace = ((IContainer) oldInput).getWorkspace();
			}

			if (newInput instanceof IWorkspace) {
				newWorkspace = (IWorkspace) newInput;
			} else if (newInput instanceof IContainer) {
				newWorkspace = ((IContainer) newInput).getWorkspace();
			}

			if (oldWorkspace != newWorkspace) {
				if (oldWorkspace != null) {
					oldWorkspace.removeResourceChangeListener(this);
				}
				if (newWorkspace != null) {
					newWorkspace.addResourceChangeListener(this,
							IResourceChangeEvent.POST_CHANGE);
				}
			}

			if (newInput != null) {
				releaseModelInfo();
				loadModelInfo(newWorkspace);
			}
		}

		public void loadModelInfo(IWorkspace workspace) {
			projectInfoMap = new HashMap<IProject, ProjectInfo>();
			sourceContainerInfoMap = new HashMap<IPath, SourceContainerInfo>();

			IProject[] projects = workspace.getRoot().getProjects();
			for (IProject project : projects) {
				JastAddJModel model = JastAddModelProvider.getModel(project,
						JastAddJModel.class);
				if (model == null)
					continue;
				model.addListener(this);
				ProjectInfo projectInfo = new ProjectInfo();
				projectInfo.model = model;
				projectInfo.buildConfiguration = model
						.getBuildConfiguration(project);
				projectInfoMap.put(project, projectInfo);

				if (projectInfo.buildConfiguration != null) {
					for (SourcePathEntry sourcePathEntry : projectInfo.buildConfiguration.sourcePathList) {
						IResource resource = project
								.findMember(sourcePathEntry.sourcePath);
						if (resource == null
								|| !(resource instanceof IContainer))
							continue;
						SourceContainerInfo sourceContainerInfo = new SourceContainerInfo();
						sourceContainerInfo.model = model;
						sourceContainerInfo.project = project;
						sourceContainerInfo.sourceContainer = (IContainer) resource;
						sourceContainerInfo.sourcePathEntry = sourcePathEntry;
						sourceContainerInfoMap.put(resource.getFullPath(),
								sourceContainerInfo);
					}
				}
			}
		}

		public void releaseModelInfo() {
			if (projectInfoMap != null) {
				for (ProjectInfo projectInfo : projectInfoMap.values()) {
					projectInfo.model.removeListener(this);
				}
				projectInfoMap = null;
				sourceContainerInfoMap = null;
			}
		}

		public void dispose() {
			if (viewer != null) {
				IWorkspace workspace = null;
				Object obj = viewer.getInput();
				if (obj instanceof IWorkspace) {
					workspace = (IWorkspace) obj;
				} else if (obj instanceof IContainer) {
					workspace = ((IContainer) obj).getWorkspace();
				}
				if (workspace != null) {
					workspace.removeResourceChangeListener(this);
				}

				List<JastAddJModel> models = JastAddModelProvider
						.getModels(JastAddJModel.class);
				for (JastAddJModel model : models)
					model.removeListener(this);
			}
		}

		public void modelChangedEvent() {
			releaseModelInfo();
			loadModelInfo(ResourcesPlugin.getWorkspace());
		}

		public final void resourceChanged(final IResourceChangeEvent event) {
			doRefresh();
		}

		private void doRefresh() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					// Abort if this happens after disposes
					Control ctrl = viewer.getControl();
					if (ctrl == null || ctrl.isDisposed()) {
						return;
					}
					viewer.refresh();
				}
			});
		}

		protected IWorkbenchAdapter getAdapter(Object element) {
			return (IWorkbenchAdapter) Util.getAdapter(element,
					IWorkbenchAdapter.class);
		}

		public Object[] getChildren(Object element) {
			if (element instanceof IOutlineNode) {
				IOutlineNode node = (IOutlineNode) element;
				return node.outlineChildren().toArray();
			}

			IWorkbenchAdapter adapter = getAdapter(element);
			if (adapter == null)
				return new Object[0];
			Object[] resourceChildren = adapter.getChildren(element);
			IResource resource = (IResource) element;

			IPath sourceResourcePath = checkSourceResource(resource);
			if (sourceResourcePath != null) {
				if (resource instanceof IContainer) {
					final IContainer container = (IContainer) resource;
					final List<IResource> result = new ArrayList<IResource>();
					for (Object child : resourceChildren)
						if (child instanceof IFile)
							result.add((IFile) child);
					
					SourceContainerInfo sourceContainerInfo = sourceContainerInfoMap
							.get(container.getFullPath());
					if (sourceContainerInfo != null) {
						try {
							container.accept(new IResourceVisitor() {
								public boolean visit(IResource resource)
										throws CoreException {
									switch (resource.getType()) {
									case IResource.FOLDER:
										if (!resource.equals(container))
											result.add(resource);
										break;
									}
									return true;
								}
							});
						} catch (CoreException e) {
							sourceContainerInfo.model.logCoreException(e);
							return new Object[0];
						}
					}

					return result.toArray();
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					JastAddModel model = JastAddModelProvider.getModel(file);
					if (model != null) {
						IJastAddNode node = model.getTreeRoot(file);
						if (node != null && node instanceof IOutlineNode) {
							return ((IOutlineNode) node).outlineChildren()
									.toArray();
						}
					}
				}
			}

			return resourceChildren;
		}

		public Object[] getElements(Object element) {
			return getChildren(element);
		}

		public Object getParent(Object element) {
			IWorkbenchAdapter adapter = getAdapter(element);
			if (adapter != null) {
				return adapter.getParent(element);
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private class MyLabelProvider extends LabelProvider {

		private LabelProvider parentLabelProvider = new DecoratingLabelProvider(
				new WorkbenchLabelProvider(), getPlugin().getWorkbench()
						.getDecoratorManager().getLabelDecorator());

		public String getText(Object element) {
			if (element == null)
				return null;

			if (element instanceof IOutlineNode) {
				try {
					return ((IOutlineNode) element).contentOutlineLabel();
				} catch (Exception e) {
				}
			}

			IResource resource = (IResource) element;
			IPath sourceResourcePath = checkSourceResource(resource);
			if (sourceResourcePath != null) {
				if (resource instanceof IFolder && !sourceResourcePath.isEmpty())
					return sourceResourcePath.toString();
			}

			return parentLabelProvider.getText(element);
		}

		public Image getImage(Object element) {
			if (element instanceof IOutlineNode) {
				try {
					return ((IOutlineNode) element).contentOutlineImage();
				} catch (Exception e) {
				}
			}
			return parentLabelProvider.getImage(element);
		}
	}
	
    protected void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Object element = selection.getFirstElement();
        
        if(element instanceof IJastAddNode) {
			IJastAddNode node = (IJastAddNode)element;
			JastAddModel model = JastAddModelProvider.getModel(node);
			if (model != null)
				model.openFile(node);
        }
        else if(element instanceof IFile) {
        	TreeViewer viewer = getTreeViewer();
        	
        	IFile file = (IFile)element;
            IEditorDescriptor selectedDescriptor = null;
            
            
        	JastAddModel model = JastAddModelProvider.getModel((IFile)element);
        	if (model != null) {
	            IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
	        	IEditorDescriptor[] descriptors = editorReg.getEditors(file.getName());
	        	
	        	for(IEditorDescriptor descriptor : descriptors) 
	        		if (descriptor.getId().equals(model.getEditorID()))
	        			selectedDescriptor = descriptor;
	        	if (selectedDescriptor == null && descriptors.length > 0)
	        		selectedDescriptor = descriptors[0];
            }            
            
            OpenFileAction action = new OpenFileAction(getSite().getPage(), selectedDescriptor);
			action.selectionChanged((IStructuredSelection)viewer.getSelection());
			if (action.isEnabled())
				action.run();
        }
        else super.handleDoubleClick(event);
    }	
}
