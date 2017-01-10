package org.plugin.chibi.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.plugin.chibi.Activator;
import org.plugin.chibi.controllers.ComparisonResult;
import org.plugin.chibi.controllers.SnippetController;
import org.plugin.chibi.util.Util;
import org.plugin.chibi.views.SnippetsByProjectView;

import chibi.model.CBWarning;
import chibi.model.ICBWarning;

import org.eclipse.jface.viewers.TreeSelection;

public class PackagePopupHandler extends AbstractHandler {
	private static final String MARKER_ID = "org.plugin.chibi" + ".snippetmarker";
	List<IResource> members = new ArrayList<IResource>();
	private static final List<String> EXTENSIONS = Arrays.asList("java");
	private List<String> classesSourceCode = new ArrayList<String>();
	private List<String> classesNames = new ArrayList<String>();
	private List<String> classesFullPath = new ArrayList<String>();
	private List<IFile> classesResource = new ArrayList<IFile>();
	private List<ComparisonResult> comparisonResults = new ArrayList<ComparisonResult>();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		classesSourceCode.clear();
		classesNames.clear();
		classesFullPath.clear();
		classesResource.clear();
		members.clear();
		IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		IWorkbenchPage activePage = editorPart.getSite().getPage();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkbenchWindow window =
		PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		ISelection selection = window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
		IStructuredSelection structured = (IStructuredSelection) window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
		TreePath projectPath = ((TreeSelection)structured).getPaths()[0];
		String projectName = ((IJavaElement)projectPath.getFirstSegment()).getElementName();
		try {		

			IResource[] rootMembers = root.members();
			for (int i = 0; i < rootMembers.length; i++) {
				if (rootMembers[i].getName().equals(projectName)) {
					processMembers(rootMembers[i]);
					
					for (IResource member : members) {
						IFile file = (IFile)member;
						InputStream inputStream = file.getContents();							
						classesSourceCode.add(Util.readFileContents(inputStream));	
						classesNames.add(file.getName());
						classesFullPath.add(file.getWorkspace().getRoot().getLocation() + file.getFullPath().toPortableString());
						classesResource.add(file);						
					}
					break;
				}
			}
			
			SnippetController snippetController = new SnippetController();
			comparisonResults = snippetController.compareByProject(classesSourceCode, classesNames, classesFullPath);
			SnippetsByProjectView.setComparisonResult(comparisonResults, classesNames);
			
			for (int i = 0; i < classesNames.size(); i++) {		
				String className = classesNames.get(i);
				List<ComparisonResult> methods = comparisonResults.stream().filter(n->n.getCbMethod().getClassName().equals(className)).collect(Collectors.toList());
				IFile classFile = classesResource.get(i);
				classFile.deleteMarkers(MARKER_ID, false, 1);
				
				for (ComparisonResult comparisonResult : methods) {					
						
						String warningText = "";
						for (ICBWarning warning : comparisonResult.getWarningsFound()) {
							if (warning.hasMessage()) {
								warningText = warningText + ((CBWarning)warning).getName()+". ";
							}
						}
						
						final IMarker marker = classFile.createMarker(MARKER_ID);
						marker.setAttribute(IMarker.MESSAGE, warningText);
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
						marker.setAttribute(IMarker.LINE_NUMBER, comparisonResult.getCbMethod().getStartLineNumber());
					}
//				}
			}
			
			// Snippet to open our Custom View
			activePage.showView(SnippetsByProjectView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void processMembers(IResource container){
		if (container instanceof IFile) {
			IFile file = (IFile)container;
			if (file.getType() == IFile.FILE){ 
				if (EXTENSIONS.contains(file.getFileExtension())) {
					members.add(container);
				}
			}			
		}
		else {
			try {
				for (IResource i : ((IContainer)container).members()) {
					processMembers(i);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
