package org.plugin.chibi.views;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
//import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
//import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.plugin.chibi.controllers.ComparisonResult;
import org.plugin.chibi.controllers.CompletionController;
import org.plugin.chibi.util.Util;

import chibi.model.CBWarning;
import chibi.model.CBMethod;
import chibi.model.ICBWarning;

import java.io.File;
import java.util.Map.Entry;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

public class SnippetsView extends ViewPart {

	public static final String ID = "org.plugin.chibi.views.Snippets";

	private Action tableClickAction;
	private Action tableDoubleClickAction;

	private static ComparisonResult comparisonResult;
	private TableViewer tableViewer;
	private StyledText txtCodeViewer;
	private static SnippetsView view;
	private Image warningImage;

	private static Group grpTableContainer = null;

	public SnippetsView() {
		view = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(3, true));

		GridData gridDataParent = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridDataParent.grabExcessHorizontalSpace = true;
		gridDataParent.grabExcessVerticalSpace = true;
		parent.setLayoutData(gridDataParent);

		grpTableContainer = new Group(parent, SWT.PUSH);
		grpTableContainer.setLayout(new GridLayout(1, true));
		grpTableContainer.setBackground(new Color(grpTableContainer.getDisplay(), 255, 255, 255));
		GridData gridDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridDataGroup.minimumWidth = 300;
		grpTableContainer.setLayoutData(gridDataGroup);

		// buildTableViewer		
		tableViewer = new TableViewer(grpTableContainer, SWT.PUSH | SWT.H_SCROLL | SWT.V_SCROLL);
		Table table = tableViewer.getTable();
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(tableViewer.getControl(), "org.plugin.popupmenu.snippetviewer");
		getSite().setSelectionProvider(tableViewer);		
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setSorter(new NameSorter());
		tableViewer.setInput(getViewSite());
		
		makeActions();
		setTableViewerListeners();

		// buildCodeViewer
		txtCodeViewer = new StyledText(parent, SWT.PUSH | SWT.BORDER);
		GridData gridDataText = new GridData();
		gridDataText.horizontalAlignment = GridData.FILL;
		gridDataText.verticalAlignment = GridData.FILL;
		gridDataText.horizontalSpan = 2;
		gridDataText.grabExcessHorizontalSpace=true;
		txtCodeViewer.setLayoutData(gridDataText);
		txtCodeViewer.setBackground(new org.eclipse.swt.graphics.Color(grpTableContainer.getDisplay(), 255, 255, 198));
		txtCodeViewer.setFont(new Font(grpTableContainer.getDisplay(), "Consolas", 10, SWT.NORMAL));

		grpTableContainer.pack();
		
		warningImage = Util.createImageIcon("warning_obj.gif");
	}
		

	@Override
	public void setFocus() {
	}

	public static void setComparisonResult(final ComparisonResult comparisonResult) {
		SnippetsView.comparisonResult = comparisonResult;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				view.tableViewer.setContentProvider(view.new ViewContentProvider());
				view.tableViewer.getTable().setRedraw(true);
				grpTableContainer.layout();
			}
		});
	}

	@SuppressWarnings("unused")
	private void showMessage(String message) {
		MessageDialog.openInformation(grpTableContainer.getShell(), "Snippets View", message);
	}

	private void setTableViewerListeners() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				tableClickAction.run();

			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				tableDoubleClickAction.run();
			}
		});
	}

	private void makeActions() {		
		
		tableClickAction = new Action() {
			public void run() {
				ISelection selection = view.tableViewer.getSelection();
				Object selectedNode = ((IStructuredSelection) selection).getFirstElement();

				if (selectedNode instanceof CBMethod) {
					CBMethod cbMethod = ((CBMethod) selectedNode);
					txtCodeViewer.setText(cbMethod.getSource());

					for (ICBWarning cbWarning : comparisonResult.getWarningsFound()) {
						for (Entry<String, Integer> message : ((CBWarning) cbWarning).getMessages().entrySet()) {
							int lineNumber = message.getValue();
							int linesCount = cbMethod.getSource().split("\n").length;
							// Ask if warning's message is related to this snippet
							if (lineNumber > cbMethod.getStartLineNumber()
									&& lineNumber <= linesCount + cbMethod.getStartLineNumber()) {
								StyleRange style = new StyleRange();
								style.fontStyle = SWT.BOLD;
								style.foreground = grpTableContainer.getDisplay().getSystemColor(SWT.COLOR_RED);

								lineNumber = lineNumber - cbMethod.getStartLineNumber();
								StyledTextContent styledTextContent = txtCodeViewer.getContent();
								int offsetStart = styledTextContent.getOffsetAtLine(lineNumber - 1);
								String lineWarned = cbMethod.getSource().split("\n")[lineNumber - 1];
								offsetStart = offsetStart + lineWarned.indexOf(message.getKey());

								style.start = offsetStart;
								style.length = message.getKey().length();

								txtCodeViewer.setStyleRange(style);
							}
						}
					}
				}
			}
		};

		tableDoubleClickAction = new Action() {
			public void run() {					
				
				CBMethod cbMethod = comparisonResult.getCbMethod();
				
				// File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation()+"/testtest/src/testtest/Testestest.java");
				File file = new File(cbMethod.getClassFullPath());
				if (file.exists() && file.isFile()) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					
					IEditorPart part = page.getActiveEditor();
					if (part instanceof AbstractTextEditor){
						ITextEditor editor = (ITextEditor)part;
						   IDocumentProvider dp = editor.getDocumentProvider();
						   IDocument doc = dp.getDocument(editor.getEditorInput());
						   int currentOffset;
						try {
							//Get current offset
							ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
							ISelection selection = selectionProvider.getSelection();
							if (selection instanceof ITextSelection) {
								ITextSelection textSelection = (ITextSelection)selection;
								currentOffset = textSelection.getOffset(); 
								int line = doc.getLineOfOffset(currentOffset);
								System.out.println(currentOffset);
								
								//Get snippet text
								Object selectedNode = ((IStructuredSelection) view.tableViewer.getSelection()).getFirstElement();

								if (selectedNode instanceof CBMethod) {
									CBMethod cbSnippet = ((CBMethod) selectedNode);
									//Paste text in current line								
									doc.replace(doc.getLineOffset(line), 0, cbSnippet.getSource()+"\n");
									
									//Paste imports on line 1, after package declaration
									CompletionController completionController = new CompletionController();
									String imports = completionController.getImports(doc.get(), cbSnippet);									
									doc.replace(doc.getLineOffset(1), 0, imports);
								}								
							}							
							
						} catch (BadLocationException e) {
							e.printStackTrace();
						}						   
					}	
					
//					IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
//					try {
//
//						IEditorPart openEditor = IDE.openEditorOnFileStore(page, fileStore);
//
//						if (openEditor instanceof ITextEditor) {
//							ITextEditor textEditor = (ITextEditor) openEditor;
//							IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
//							textEditor.selectAndReveal(
//									document.getLineOffset(cbMethod.getStartLineNumber() - 1),
//									document.getLineLength(cbMethod.getStartLineNumber() - 1));
//						}
//					} catch (PartInitException e) {
//						e.printStackTrace();
//					} catch (BadLocationException e) {
//						e.printStackTrace();
//					}
					
									    
				}
			}			
		};
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (comparisonResult == null) {
				return new String[] {};
			} else {
				CBMethod[] cbSnippets = new CBMethod[comparisonResult.getSnippetsFound().size()];
				for (int i = 0; i < comparisonResult.getSnippetsFound().size(); i++) {
					cbSnippets[i] = comparisonResult.getSnippetsFound().get(i);
				}
				return cbSnippets;
			}
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof CBMethod) {
				return ((CBMethod) obj).getDescription();
			}
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof CBMethod) {
				return warningImage;
			}
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

}
