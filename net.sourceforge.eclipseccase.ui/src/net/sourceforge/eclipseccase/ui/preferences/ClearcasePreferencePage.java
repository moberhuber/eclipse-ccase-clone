/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.preferences;

import java.io.IOException;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.IClearcasePreferenceConstants;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The main preference page for the Eclipse ClearCase integration.
 */
public class ClearcasePreferencePage extends FieldEditorPreferencePageWithCategories
        implements IWorkbenchPreferencePage, IClearcasePreferenceConstants {

    private static final String COMMENTS = "Comments";

    private static final String SOURCE_MANAGEMENT = "Source Management";

    private static final String GENERAL = "General";

    private static final String[] CATEGORIES = new String[] { GENERAL,
            SOURCE_MANAGEMENT, COMMENTS};

    /**
     * Internal implementation of a JFace preference store atop a core runtime
     * preference store.
     */
    static final class ClearcasePreferenceStore implements
            IPersistentPreferenceStore {

        /**
         * Flag to indicate that the listener has been added.
         */
        private boolean listenerAdded = false;

        /**
         * The underlying core runtime preference store; <code>null</code> if
         * it has not been initialized yet.
         */
        private Preferences prefs = null;

        /**
         * Identity list of old listeners (element type:
         * <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
         */
        private ListenerList listeners = new ListenerList();

        /**
         * Indicates whether property change events should be suppressed (used
         * in implementation of <code>putValue</code>). Initially and usually
         * <code>false</code>.
         * 
         * @see IPreferenceStore#putValue
         */
        boolean silentRunning = false;

        /**
         * Creates a new instance for the this plug-in.
         */
        public ClearcasePreferenceStore() {
            // Important: do not call initialize() here
            // due to heinous reentrancy problems.
        }

        /**
         * Initializes this preference store.
         */
        void initialize() {
            // ensure initialization is only done once.
            if (this.prefs != null) { return; }
            // here's where we first ask for the plug-in's core runtime
            // preferences;
            // note that this causes this method to be reentered
            this.prefs = ClearcasePlugin.getInstance().getPluginPreferences();
            // avoid adding the listener a second time when reentered
            if (!this.listenerAdded) {
                // register listener that funnels everything to
                // firePropertyChangeEvent
                this.prefs
                        .addPropertyChangeListener(new Preferences.IPropertyChangeListener() {

                            public void propertyChange(
                                    Preferences.PropertyChangeEvent event) {
                                if (!silentRunning) {
                                    firePropertyChangeEvent(
                                            event.getProperty(), event
                                                    .getOldValue(), event
                                                    .getNewValue());
                                }
                            }
                        });
                this.listenerAdded = true;
            }
        }

        /**
         * Returns the underlying preference store.
         * 
         * @return the underlying preference store
         */
        private Preferences getPrefs() {
            if (prefs == null) {
                // although we try to ensure initialization is done eagerly,
                // this cannot be guaranteed, so ensure it is done here
                initialize();
            }
            return prefs;
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void addPropertyChangeListener(
                final IPropertyChangeListener listener) {
            listeners.add(listener);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void removePropertyChangeListener(
                IPropertyChangeListener listener) {
            listeners.remove(listener);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void firePropertyChangeEvent(String name, Object oldValue,
                Object newValue) {

            // efficiently handle case of 0 listeners
            if (listeners.isEmpty()) {
            // no one interested
            return; }

            // important: create intermediate array to protect against
            // listeners
            // being added/removed during the notification
            final Object[] list = listeners.getListeners();
            final PropertyChangeEvent event = new PropertyChangeEvent(this,
                    name, oldValue, newValue);
            Platform.run(new SafeRunnable(JFaceResources
                    .getString("PreferenceStore.changeError")) { //$NON-NLS-1$

                        public void run() {
                            for (int i = 0; i < list.length; i++) {
                                ((IPropertyChangeListener) list[i])
                                        .propertyChange(event);
                            }
                        }
                    });

        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public boolean contains(String name) {
            return getPrefs().contains(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public boolean getBoolean(String name) {
            return getPrefs().getBoolean(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public boolean getDefaultBoolean(String name) {
            return getPrefs().getDefaultBoolean(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public double getDefaultDouble(String name) {
            return getPrefs().getDefaultDouble(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public float getDefaultFloat(String name) {
            return getPrefs().getDefaultFloat(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public int getDefaultInt(String name) {
            return getPrefs().getDefaultInt(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public long getDefaultLong(String name) {
            return getPrefs().getDefaultLong(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public String getDefaultString(String name) {
            return getPrefs().getDefaultString(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public double getDouble(String name) {
            return getPrefs().getDouble(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public float getFloat(String name) {
            return getPrefs().getFloat(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public int getInt(String name) {
            return getPrefs().getInt(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public long getLong(String name) {
            return getPrefs().getLong(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public String getString(String name) {
            return getPrefs().getString(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public boolean isDefault(String name) {
            return getPrefs().isDefault(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public boolean needsSaving() {
            return getPrefs().needsSaving();
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void putValue(String name, String value) {
            try {
                // temporarily suppress event notification while setting value
                silentRunning = true;
                getPrefs().setValue(name, value);
            } finally {
                silentRunning = false;
            }
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, double value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, float value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, int value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, long value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, String value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setDefault(String name, boolean value) {
            getPrefs().setDefault(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setToDefault(String name) {
            getPrefs().setToDefault(name);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, double value) {
            getPrefs().setValue(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, float value) {
            getPrefs().setValue(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, int value) {
            getPrefs().setValue(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, long value) {
            getPrefs().setValue(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, String value) {
            getPrefs().setValue(name, value);
        }

        /*
         * (non-javadoc) Method declared on IPreferenceStore
         */
        public void setValue(String name, boolean value) {
            getPrefs().setValue(name, value);
        }

        /**
         * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
         */
        public void save() throws IOException {
            ClearcasePlugin.getInstance().savePluginPreferences();
        }

    }

    /**
     * Creates a new instance.
     */
    public ClearcasePreferencePage() {
        setDescription("Configure the Eclipse ClearCase integration.");

        // Set the preference store for the preference page.
        setPreferenceStore(new ClearcasePreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {

        // general settings

        addField(new BooleanFieldEditor(IGNORE_NEW,
                "Do not look for new resources not under source control",
                getFieldEditorParent(GENERAL)));

        addField(new BooleanFieldEditor(RECURSIVE,
                "Perform all operations recursively",
                getFieldEditorParent(GENERAL)));

        addField(new BooleanFieldEditor(PRESERVE_TIMES,
                "Preserve file modification times",
                getFieldEditorParent(GENERAL)));

        // use cleartool option only available on windows
        BooleanFieldEditor useCleartool = new BooleanFieldEditor(USE_CLEARTOOL,
                "Use the cleartool executable for all operations",
                getFieldEditorParent(GENERAL));
        useCleartool.setEnabled(ClearcasePlugin.isWindows(),
                getFieldEditorParent(GENERAL));
        addField(useCleartool);
        
        //RadioGroupFieldEditor clearcaseLayer = new RadioGroupFieldEditor(CLEARCASE_API,
        //        "Interface for ClearCase operations",1,
        //        new String[][]{
        //        {"Native - CAL (ClearCase Automation Library)", CLEARCASE_NATIVE},
        //        {"Native - cleartool executable", CLEARCASE_CLEARTOOL},
        //        {"Compatible - ClearDlg executable", CLEARCASE_CLEARDLG}
        //        }
        //        ,getFieldEditorParent(GENERAL),true);
        //addField(clearcaseLayer);
        
        addField(new RadioGroupFieldEditor(SAVE_DIRTY_EDITORS,
                "Save dirty editors before ClearCase operations", 1,
                new String[][] { { "Always", VALUE_FORCE},
                        { "Prompt", VALUE_ASK}, { "Never", VALUE_NEVER}},
                getFieldEditorParent(GENERAL), true));

        // source management

        addField(new BooleanFieldEditor(CHECKOUT_AUTO,
                "Automatically checkout resources if necessary",
                getFieldEditorParent(SOURCE_MANAGEMENT)));

        addField(new BooleanFieldEditor(ADD_AUTO,
                "Automatically add resources if necessary",
                getFieldEditorParent(SOURCE_MANAGEMENT)));

        addField(new BooleanFieldEditor(ADD_WITH_CHECKIN,
                "Automatically checkin resources after manual add",
                getFieldEditorParent(SOURCE_MANAGEMENT)));

        addField(new BooleanFieldEditor(
                CHECKOUT_LATEST,
                "Update to latest version before checkout (useful on SnapShot views)",
                getFieldEditorParent(SOURCE_MANAGEMENT)));

        addField(new BooleanFieldEditor(
                USE_CLEARDLG,
                "Use ClearCase user interface (cleardlg executable)",
                getFieldEditorParent(SOURCE_MANAGEMENT)));

        addField(new RadioGroupFieldEditor(
                IClearcasePreferenceConstants.CHECKOUT_RESERVED,
                "Reserved Checkouts", 1, new String[][] {
                        { "Always", VALUE_FORCE},
                        { "Only if possible", VALUE_IF_POSSIBLE},
                        { "Never", VALUE_NEVER}},
                getFieldEditorParent(SOURCE_MANAGEMENT), true));

        // comments

        addField(new BooleanFieldEditor(COMMENT_ADD, "Prompt on add",
                getFieldEditorParent(COMMENTS)));

        addField(new BooleanFieldEditor(COMMENT_CHECKIN, "Prompt on checkin",
                getFieldEditorParent(COMMENTS)));

        addField(new BooleanFieldEditor(COMMENT_CHECKOUT, "Prompt on checkout",
                getFieldEditorParent(COMMENTS)));

        addField(new BooleanFieldEditor(COMMENT_CHECKOUT_NEVER_ON_AUTO,
                "Never prompt on auto checkout", getFieldEditorParent(COMMENTS)));

        addField(new BooleanFieldEditor(COMMENT_ADD_NEVER_ON_AUTO,
                "Never prompt on auto add", getFieldEditorParent(COMMENTS)));

        addField(new BooleanFieldEditor(
                IClearcasePreferenceConstants.COMMENT_ESCAPE,
                "Escape \"<>'&& with XML entities",
                getFieldEditorParent(COMMENTS)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        if (super.performOk()) {
            ClearcasePlugin.getInstance().resetClearcase();
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.eclipseccase.ui.preferences.TabFieldEditorPreferencePage#getCategories()
     */
    protected String[] getCategories() {
        return CATEGORIES;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
     */
    protected String getDescription(String category) {
        if(GENERAL.equals(category))
            return "Configure general preferences of the ClearCase integration:";
        if(SOURCE_MANAGEMENT.equals(category))
            return "Customize source management features:";
        if(COMMENTS.equals(category))
            return "Specify what to do with comments:";
        return null;
    }

}