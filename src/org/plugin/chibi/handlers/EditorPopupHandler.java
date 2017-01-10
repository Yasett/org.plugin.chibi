package org.plugin.chibi.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.plugin.chibi.Activator;
import org.plugin.chibi.controllers.ComparisonResult;
import org.plugin.chibi.controllers.SnippetController;
import org.plugin.chibi.views.SnippetsView;
import org.eclipse.ui.IFileEditorInput;

import chibi.model.CBWarning;
import chibi.model.ICBWarning;

import org.eclipse.jdt.ui.*;

public class EditorPopupHandler extends AbstractHandler {
	private static final String MARKER_ID = "org.plugin.chibi" + ".snippetmarker";
	private static ComparisonResult result;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get editor
		IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		IWorkbenchPage activePage = editorPart.getSite().getPage();

		// Snippet to get the method signature
		try {
			String methodSignature = "";
			IJavaElement elem = JavaUI.getEditorInputJavaElement(editorPart.getEditorInput());
			if (elem instanceof ICompilationUnit) {
				ITextSelection sel = (ITextSelection) editorPart.getEditorSite().getSelectionProvider().getSelection();
				IJavaElement selected = ((ICompilationUnit) elem).getElementAt(sel.getOffset());
				if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
					methodSignature = getMethodSignature(((IMethod) selected).getSource());
				}
			}
			
			if (methodSignature != "") {
				// Snippet to open our Custom View
				activePage.showView(SnippetsView.ID);
				
				// Snippet to set the SnippetsViewData
				ITextEditor textEditor = (ITextEditor) editorPart;
				if (editorPart instanceof ITextEditor) {

					IDocumentProvider provider = textEditor.getDocumentProvider();
					IEditorInput input = editorPart.getEditorInput();
					IDocument document = provider.getDocument(input);
					String text = document.get();
					String className = input.getName();
					IPath classFullPath = ((IFileEditorInput) input).getFile().getLocation();
//					String name3 = name2.toString().substring(name2.toString().indexOf("src"));
					SnippetController snippetController = new SnippetController();
					result = snippetController.compareByMethod(text, methodSignature, className, classFullPath.toString());

					SnippetsView.setComparisonResult(result);
					
					// Snippet to create a marker
					try {
						IResource resource = (IResource) textEditor.getEditorInput().getAdapter(IResource.class);
						resource.deleteMarkers(MARKER_ID, false, 1);
						String warningText = "";
						for (ICBWarning warning : result.getWarningsFound()) {
							if (warning.hasMessage()) {
								warningText = warningText + ((CBWarning)warning).getName()+". ";
							}
						}
						final IMarker marker = resource.createMarker(MARKER_ID);
						marker.setAttribute(IMarker.MESSAGE, warningText);
						// marker.setAttribute(IMarker.CHAR_START, 5);
						// marker.setAttribute(IMarker.CHAR_END, 10);
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
						marker.setAttribute(IMarker.LINE_NUMBER, result.getCbMethod().getStartLineNumber());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}

			} else {
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				MessageDialog.openInformation(window.getShell(), "Encontrar snippets",
						"Invoque la herramienta dentro del método que desea analizar.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		

		

//		try {
//			

			// Snippet to get the selected text
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
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return null;
	}
	
	private String getMethodSignature(String source){
		String signature = "";
		signature = (source.split("\n"))[0];
		signature = signature.trim().replace("{", "").replace("}", "");
		return signature;
	}

}
