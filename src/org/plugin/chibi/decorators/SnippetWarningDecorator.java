package org.plugin.chibi.decorators;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class SnippetWarningDecorator implements ILightweightLabelDecorator {
	
	private static final String MARKER_ID = "org.plugin.chibi" + ".snippetmarker";

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void decorate(Object resource, IDecoration decoration) {
		if(resource instanceof IResource){
			IMarker[] markers;
			try {
				markers = ((IResource)resource).findMarkers(MARKER_ID, true, IResource.DEPTH_ZERO);
				if (markers.length > 0) {
					decoration.addOverlay(ImageDescriptor.createFromFile(SnippetWarningDecorator.class, 
							"/icons/decorator_s.gif"), IDecoration.TOP_LEFT);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
		}
	}

}
