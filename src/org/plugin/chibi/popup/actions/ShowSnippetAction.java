package org.plugin.chibi.popup.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.resources.IResourceChangeEvent;
//import org.eclipse.core.resources.IResourceChangeListener;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
//import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
//import org.plugin.chibi.views.*;
import org.eclipse.ui.handlers.IHandlerService;
import org.plugin.chibi.Activator;

public class ShowSnippetAction implements IObjectActionDelegate {

//	private static final String MARKER_ID = "org.plugin.chibi" + ".snippetmarker";
	private static ShowSnippetAction listener;
//	private Shell shell;

	/**
	 * Constructor for Action1.
	 */
//	public ShowSnippetAction() {
//		super();
//	}
	
	public static ShowSnippetAction getListener() {
		if (listener == null)
			listener = new ShowSnippetAction();
		return listener;
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		// get editor
		IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		Command command = ((ICommandService) editorPart.getSite().getService(ICommandService.class)).getCommand("org.plugin.chibi.showSnippetsAction");
		final Event trigger = new Event();
		ExecutionEvent executionEvent = ((IHandlerService) editorPart.getSite().getService(IHandlerService.class)).createExecutionEvent(command, trigger);
		try {
			command.executeWithChecks(executionEvent);
		} catch (Exception e) {
		}
		
//		try {
//			// get editor
//			IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.getActiveEditor();
//			IWorkbenchPage activePage = editorPart.getSite().getPage();	
//			
//			//Snippet to open our Custom View
//			activePage.showView(SnippetsView.ID);
//
//			// Snippet to set the SnippetsViewData
//			ITextEditor textEditor = (ITextEditor) editorPart;
//			if (editorPart instanceof ITextEditor) {
//
//				IDocumentProvider provider = textEditor.getDocumentProvider();
//				IEditorInput input = editorPart.getEditorInput();
//				IDocument document = provider.getDocument(input);
//				String text = document.get();
//				// MessageDialog.openInformation(shell, "Do Something
//				// Menu","text: " + text);
//				List<String> items = new ArrayList<String>();
//				items.add(text);
//				items.add("2"+text);
//				SnippetsView.setArguments(items);
//			}
//
//			// Snippet to get the selected text					
//			if (editorPart instanceof AbstractTextEditor) {
//				int offset = 0;
//				int length = 0;
//				String selectedText = null;
//				IEditorSite iEditorSite = editorPart.getEditorSite();
//				if (iEditorSite != null) {
//					// get selection provider
//					ISelectionProvider selectionProvider = iEditorSite.getSelectionProvider();
//					if (selectionProvider != null) {
//						ISelection iSelection = selectionProvider.getSelection();
//						// offset
//						offset = ((ITextSelection) iSelection).getOffset();
//						if (!iSelection.isEmpty()) {
//							selectedText = ((ITextSelection) iSelection).getText();
//							// length
//							length = ((ITextSelection) iSelection).getLength();
//							System.out.println("length: " + length);
//							// MessageDialog.openInformation(
//							// shell,
//							// "Do Something Menu",
//							// "Length: " + length + " Offset: " + offset);
//						}
//					}
//				}
//
//			}
//
//			// Snippet to create a marker
//			IResource resource = (IResource) textEditor.getEditorInput().getAdapter(IResource.class);
//			resource.deleteMarkers(MARKER_ID, false, 1);
//			final IMarker marker = resource.createMarker(MARKER_ID);
//			marker.setAttribute(IMarker.MESSAGE, "Snippet warning");
//			// marker.setAttribute(IMarker.CHAR_START, 5);
//			// marker.setAttribute(IMarker.CHAR_END, 10);
//			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
//			marker.setAttribute(IMarker.LINE_NUMBER, 3);
//			
//		} catch (Exception e) {
//			System.out.println(e.getStackTrace());
//		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
