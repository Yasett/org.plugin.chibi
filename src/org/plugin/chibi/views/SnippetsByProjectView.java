package org.plugin.chibi.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.plugin.chibi.controllers.ComparisonResult;
import org.plugin.chibi.util.Util;

import chibi.model.CBMethod;
import chibi.model.CBWarning;
import chibi.model.ICBWarning;

public class SnippetsByProjectView extends ViewPart {
	public static final String ID = "org.plugin.chibi.views.SnippetsByProject";
	private static List<ComparisonResult> comparisonResults;
	private static List<String> classesNames;
	private TreeViewer treeViewer;
	private static SnippetsByProjectView view;
	private Action treeClickAction;
	private Action treeDoubleClickAction;
	private StyledText txtCodeViewer;
	private Image warningImage;
	private Image classImage;
	private Image methodImage;
	
	private static Group grpTreeContainer = null;

	public SnippetsByProjectView() {
		view = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(3, true));
		
		GridData gridDataParent = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridDataParent.grabExcessHorizontalSpace = true;
		gridDataParent.grabExcessVerticalSpace = true;
		parent.setLayoutData(gridDataParent);
		
		grpTreeContainer = new Group(parent, SWT.PUSH);
		grpTreeContainer.setLayout(new GridLayout(1, true));
		grpTreeContainer.setBackground(new Color(grpTreeContainer.getDisplay(), 255, 255, 255));
		GridData gridDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridDataGroup.minimumWidth = 300;
		grpTreeContainer.setLayoutData(gridDataGroup);
		
		//buildTreeViewer		
		treeViewer = new TreeViewer(grpTreeContainer, SWT.PUSH | SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setInput(classesNames);
		
		makeActions();
		setTreeViewerListeners();
//		treeViewer.expandAll();
		
		//buildCodeViewer
		txtCodeViewer = new StyledText(parent, SWT.PUSH | SWT.BORDER);
		GridData gridDataText = new GridData();
		gridDataText.horizontalAlignment = GridData.FILL;
		gridDataText.verticalAlignment = GridData.FILL;
		gridDataText.horizontalSpan = 2;
		gridDataText.grabExcessHorizontalSpace=true;
		txtCodeViewer.setLayoutData(gridDataText);
		txtCodeViewer.setBackground(new org.eclipse.swt.graphics.Color(parent.getDisplay(), 255, 255, 198));
		txtCodeViewer.setFont(new Font(parent.getDisplay(), "Consolas", 10, SWT.NORMAL));
		
		parent.pack();
		
		warningImage = Util.createImageIcon("warning_obj.gif");
		classImage = Util.createImageIcon("classes.gif");
		methodImage = Util.createImageIcon("method_public.gif");
	}
	
	private void makeActions() {
		treeClickAction = new Action() {
			public void run() {
			    IStructuredSelection thisSelection = (IStructuredSelection) view.treeViewer.getSelection();
			    Object selectedNode = thisSelection.getFirstElement(); 
			    ITreeSelection selection = ((ITreeSelection)thisSelection);
    			Object parentNode = selection.getPaths()[0].getParentPath().getLastSegment();
    			
    			//if parent is a CBMethod, this is a snippet
			    if (parentNode instanceof CBMethod) {
					CBMethod cbMethod = ((CBMethod)selectedNode);
										
					List<ComparisonResult> results = comparisonResults.stream().filter(n -> n.getCbMethod().getSignature().equals(((CBMethod)parentNode).getSignature()) && n.getCbMethod().getClassName().equals(((CBMethod)parentNode).getClassName())).collect(Collectors.toList());
					
					if (results.size() > 0) {
						txtCodeViewer.setText(cbMethod.getSource());
						ComparisonResult comparisonResult = results.get(0);
						
						//set warnings
						for (ICBWarning cbWarning : comparisonResult.getWarningsFound()) {
							for (Entry<String, Integer> message : ((CBWarning) cbWarning).getMessages().entrySet()) {
								int lineNumber = message.getValue();
								int linesCount = cbMethod.getSource().split("\n").length;
								// Ask if warning's message is related to this snippet
								if (lineNumber > cbMethod.getStartLineNumber()
										&& lineNumber <= linesCount + cbMethod.getStartLineNumber()) {
									StyleRange style = new StyleRange();
									style.fontStyle = SWT.BOLD;
									style.foreground = grpTreeContainer.getDisplay().getSystemColor(SWT.COLOR_RED);

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
			}
		};
		
		treeDoubleClickAction = new Action() {
			public void run() {
				IStructuredSelection thisSelection = (IStructuredSelection) view.treeViewer.getSelection();
//			    Object selectedNode = thisSelection.getFirstElement(); 
			    
			    ITreeSelection selection = ((ITreeSelection)thisSelection);
    			Object parentNode = selection.getPaths()[0].getParentPath().getLastSegment();
    			
    			//if parent is a CBMethod, this is a snippet
			    if (parentNode instanceof CBMethod) {
			    	CBMethod cbMethod = ((CBMethod)parentNode);
			    	File file = new File(cbMethod.getClassFullPath());
					if (file.exists() && file.isFile()) {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
						try {
							IEditorPart openEditor = IDE.openEditorOnFileStore(page, fileStore);

							if (openEditor instanceof ITextEditor) {
								ITextEditor textEditor = (ITextEditor) openEditor;
								IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
								textEditor.selectAndReveal(
										document.getLineOffset(cbMethod.getStartLineNumber() - 1),
										document.getLineLength(cbMethod.getStartLineNumber() - 1));
							}
						} catch (PartInitException e) {
							e.printStackTrace();
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
			    }
			}
		};
	}
	
	private void setTreeViewerListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				treeClickAction.run();

			}
		});

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				treeDoubleClickAction.run();
			}
		});
	}

	public static void setComparisonResult(final List<ComparisonResult> comparisonResults,
			final List<String> classesNames) {
		SnippetsByProjectView.comparisonResults = comparisonResults;
		SnippetsByProjectView.classesNames = classesNames;

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				view.treeViewer.setInput(classesNames);
//				view.treeViewer.setContentProvider(view.new FileTreeContentProvider());
//				view.treeViewer.refresh();
			}
		});
	}

	@Override
	public void setFocus() {
	}

	class TreeContentProvider implements ITreeContentProvider {
		@SuppressWarnings("rawtypes")
		public Object[] getChildren(Object parentElement) {
			if (comparisonResults == null) {
				return new String[] {};
			} else {
				if (parentElement instanceof String) {
					List<CBMethod> cbMethods = new ArrayList<CBMethod>();
					String parent = parentElement.toString();
					for (ComparisonResult results : comparisonResults) {
						if (results.getCbMethod().getClassName().equals(parent)) {
							cbMethods.add(results.getCbMethod());
						}
					}
					return cbMethods.toArray(new CBMethod[]{});
				} else if((parentElement instanceof List)) {
					return ((List) parentElement).toArray();
				}
				else if((parentElement instanceof CBMethod)) {
					CBMethod cbMethod = (CBMethod)parentElement;
					
					List<ComparisonResult> methods = comparisonResults.stream().filter(n -> n.getCbMethod().getSignature().equals(cbMethod.getSignature()) && n.getCbMethod().getClassName().equals(cbMethod.getClassName())).collect(Collectors.toList());
					
					if (methods.size() > 0) {
						List<CBMethod> snippetsFound = methods.get(0).getSnippetsFound();
						CBMethod[] snippets = new CBMethod[snippetsFound.size()];
						for (int i = 0; i < snippetsFound.size(); i++) {
							snippets[i] = snippetsFound.get(i);
						}
						
						return snippets;
					}
					return null;
				}else{
					return null;
				}
			}
		}

		public Object getParent(Object element) {
			if (comparisonResults == null) {
				return new String[] {};
			} else {
				if (element instanceof String) {
					return null;
				} else {
					return ((CBMethod) element).getClassName();					
				}
			}
		}

		public boolean hasChildren(Object arg0) {
			Object[] obj = getChildren(arg0);
			return obj == null ? false : obj.length > 0;
		}

		@SuppressWarnings("rawtypes")
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				return ((List) inputElement).toArray();
			} else if (inputElement instanceof String) {
				List<CBMethod> cbMethods = new ArrayList<CBMethod>();
				String parent = inputElement.toString();
				for (ComparisonResult results : comparisonResults) {
					if (results.getCbMethod().getClassName().equals(parent)) {
						cbMethods.add(results.getCbMethod());
					}
				}
				return cbMethods.toArray(new CBMethod[]{});
			} else if (inputElement instanceof CBMethod) {
				CBMethod cbMethod = (CBMethod)inputElement;
				List<ComparisonResult> methods = comparisonResults.stream().filter(n -> n.getCbMethod().getSignature().equals(cbMethod.getSignature()) && n.getCbMethod().getClassName().equals(cbMethod.getClassName())).collect(Collectors.toList());
				
				if (methods.size() > 0) {
					List<CBMethod> snippetsFound = methods.get(0).getSnippetsFound();
					CBMethod[] snippets = new CBMethod[snippetsFound.size()];
					for (int i = 0; i < snippetsFound.size(); i++) {
						snippets[i] = snippetsFound.get(i);
					}
					
					return snippets;
				}
			}
			return null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	class TreeLabelProvider extends LabelProvider {

		public TreeLabelProvider() {
		}

		public Image getImage(Object element) {
			if (element instanceof String) {
				return classImage;
			}
			
			if (element instanceof CBMethod) {
				//find out if it is a method or snippet
				CBMethod cbMethod = (CBMethod)element;
				List<ComparisonResult> methods = comparisonResults.stream().filter(n -> n.getCbMethod().getSignature().equals(cbMethod.getSignature()) && n.getCbMethod().getClassName().equals(cbMethod.getClassName())).collect(Collectors.toList());
				
				if (methods.size() > 0) {
					//Is method
					return methodImage;
				}
				else {
					//Is snippet
					return warningImage;
				}
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof CBMethod) {
				//find out if it is a method or snippet
				CBMethod cbMethod = (CBMethod)element;
				List<ComparisonResult> methods = comparisonResults.stream().filter(n -> n.getCbMethod().getSignature().equals(cbMethod.getSignature()) && n.getCbMethod().getClassName().equals(cbMethod.getClassName())).collect(Collectors.toList());
				
				if (methods.size() > 0) {
					//Is method
					return cbMethod.getMethodName();
				}
				else {
					//Is snippet
					return cbMethod.getDescription();
				}
			}
			else {
				return element.toString();
			}			
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

	}
}
