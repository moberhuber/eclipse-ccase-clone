/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui.panel;

import net.sourceforge.eclipseccase.ui.verifier.AbstractValidationManagerProxy;

import net.sourceforge.eclipseccase.ui.verifier.AbstractFormattedVerifier;

import net.sourceforge.eclipseccase.ui.verifier.NonEmptyFieldVerifier;

import net.sourceforge.eclipseccase.ui.preferences.DiffViewerSettings.ExternalProgramParameters;

import net.sourceforge.eclipseccase.ui.preferences.DiffViewerSettings.ResourceSpecificParameterKind;

import net.sourceforge.eclipseccase.ui.verifier.CompositeVerifier;

import net.sourceforge.eclipseccase.ui.composite.DiffViewerExternalProgramComposite;

import net.sourceforge.eclipseccase.ui.preferences.DiffViewerSettings.ResourceSpecificParameters;

import net.sourceforge.eclipseccase.ui.preferences.DiffViewerSettings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;



/**
 * Edit file associations with external compare editor panel
 * 
 * @author Igor Burilo
 */
public class EditFileAssociationsPanel extends AbstractDialogPanel {

	protected DiffViewerSettings diffSettings;
	protected ResourceSpecificParameters param;
		
	protected Text extensionText;
	protected DiffViewerExternalProgramComposite diffExternalComposite;
	protected DiffViewerExternalProgramComposite mergeExternalComposite;
	
	public EditFileAssociationsPanel(ResourceSpecificParameters param, DiffViewerSettings diffSettings) {
		this.param = param;
		this.diffSettings = diffSettings;
		
		this.dialogTitle = this.param == null ? Messages.getString("EditFileAssociationsPanel_AddDialogTitle") : Messages.getString("EditFileAssociationsPanel_EditDialogTitle");
		this.dialogDescription = Messages.getString("EditFileAssociationsPanel_DialogDescription");
		this.defaultMessage = Messages.getString("EditFileAssociationsPanel_DialogDefaultMessage");
	}
	
	public ResourceSpecificParameters getResourceSpecificParameters() {
		return this.param;
	}
	
	protected void createControlsImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);
		
		//extension or mime-type
		Label extensionLabel = new Label(composite, SWT.NONE);
		data = new GridData();
		extensionLabel.setLayoutData(data);
		extensionLabel.setText(Messages.getString("EditFileAssociationsPanel_ExtensionMimeType_Label"));
		
		this.extensionText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = 100;
		this.extensionText.setLayoutData(data);
		
		this.diffExternalComposite = new DiffViewerExternalProgramComposite(Messages.getString("DiffViewerExternalProgramComposite_DiffProgramArguments_Label"), composite, this);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.horizontalSpan = 2;
		this.diffExternalComposite.setLayoutData(data);			
		
		this.mergeExternalComposite = new DiffViewerExternalProgramComposite(Messages.getString("DiffViewerExternalProgramComposite_MergeProgramArguments_Label"), composite, new AbstractValidationManagerProxy(this) {
			protected boolean isVerificationEnabled(Control input) {			
				return false;
			}			
		});
		data = new GridData(GridData.FILL_HORIZONTAL);		
		data.horizontalSpan = 2;
		this.mergeExternalComposite.setLayoutData(data);			
		
		CompositeVerifier cmpVerifier = new CompositeVerifier();
		cmpVerifier.add(new NonEmptyFieldVerifier(Messages.getString("EditFileAssociationsPanel_ExtensionMimeType_FieldName")));
		cmpVerifier.add(new AbstractFormattedVerifier(Messages.getString("EditFileAssociationsPanel_ExtensionMimeType_FieldName")) {
			protected String getErrorMessageImpl(Control input) {
				String kindString = ((Text) input).getText();
				ResourceSpecificParameterKind kind = ResourceSpecificParameterKind.getKind(kindString);				
				ResourceSpecificParameters resourceParams = EditFileAssociationsPanel.this.diffSettings.getResourceSpecificParameters(kind);
				if (resourceParams != null && (EditFileAssociationsPanel.this.param != null && !EditFileAssociationsPanel.this.param.kind.equals(kind) || EditFileAssociationsPanel.this.param == null)) {
					return Messages.getString("EditFileAssociationsPanel_DuplicateExtension_Verifier_Error");
				}
				return null;
			}

			protected String getWarningMessageImpl(Control input) {			
				return null;
			}			
		});
		this.attachTo(this.extensionText, cmpVerifier);
		
		//init value
		if (this.param != null) {
			if (this.param.kind.kindValue != null) {
				this.extensionText.setText(this.param.kind.formatKindValue());	
			}								
			this.diffExternalComposite.setProgramPath(this.param.params.diffProgramPath);
			this.diffExternalComposite.setProgramParameters(this.param.params.diffParamatersString);
			
			this.mergeExternalComposite.setProgramPath(this.param.params.mergeProgramPath);
			this.mergeExternalComposite.setProgramParameters(this.param.params.mergeParamatersString);
		}
	}		
	
	protected void saveChangesImpl() {
		String extensionStr = this.extensionText.getText();
		ResourceSpecificParameterKind kind = ResourceSpecificParameterKind.getKind(extensionStr);
		
		ExternalProgramParameters externalProgramParams = new ExternalProgramParameters(
				this.diffExternalComposite.getProgramPath(),
				this.mergeExternalComposite.getProgramPath(),
				this.diffExternalComposite.getProgramParameters(), 
				this.mergeExternalComposite.getProgramParameters());
				
		
		if (this.param == null) {
			this.param = new ResourceSpecificParameters(kind, externalProgramParams);
			this.param.isEnabled = true;
		} else {
			this.param.kind = kind;
			this.param.params = externalProgramParams;
		}					
	}
	
	protected void cancelChangesImpl() {
		this.param = null;		
	}

}
