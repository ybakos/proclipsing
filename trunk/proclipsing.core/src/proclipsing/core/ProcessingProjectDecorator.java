package proclipsing.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class ProcessingProjectDecorator implements ILightweightLabelDecorator {

	private static final int POS = IDecoration.TOP_LEFT;
	private ImageDescriptor image;

	public void decorate(Object element, IDecoration decoration) {
		if (image == null) {
			image = Activator.getDefault().
				getImageRegistry().getDescriptor(Activator.PROJECT_DECORATOR);
		}

		IProject p = (IProject) element;

		try {
			if (p.isOpen() && p.hasNature(ProcessingProjectNature.class.getName())) {
				decoration.addOverlay(image, POS);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}	
	}
	
	public void addListener(ILabelProviderListener listener) {}

	public void dispose() {}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {}

}
