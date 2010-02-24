package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithPredecessorAction extends ClearCaseWorkspaceAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					// mike: don't know how this is used.
					// ConsoleOperationListener opListener = new
					// ConsoleOperationListener(monitor);
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						String path = resource.getLocation().toOSString();
						if (!ClearCasePlugin.isUseClearDlg()) {
							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
							p.compareWithPredecessor(path);
						} else {
							Runtime.getRuntime().exec(new String[] { "cleardlg", "/diffpred", path });
						}
					}

				} catch (IOException ex) {
				} finally {
					monitor.done();
				}

			}
		};
		executeInBackground(runnable, "Compare With Predecessor");

	}
}