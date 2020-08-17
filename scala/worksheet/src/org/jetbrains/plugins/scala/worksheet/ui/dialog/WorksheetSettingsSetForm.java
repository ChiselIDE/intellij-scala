package org.jetbrains.plugins.scala.worksheet.ui.dialog;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.plugins.scala.ScalaBundle;
import org.jetbrains.plugins.scala.project.settings.ScalaCompilerSettingsProfile;
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetCommonSettings;
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetExternalRunType;
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings;
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetProjectSettings;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

@SuppressWarnings({"unchecked"})
public class WorksheetSettingsSetForm {
    private final PsiFile myFile;
    private final Project myProject;

    private JCheckBox interactiveModeCheckBox;
    private JCheckBox makeProjectBeforeRunCheckBox;
    private ModulesComboBox moduleComboBox;
    private JPanel mainPanel;
    private JComboBox<ScalaCompilerSettingsProfile> compilerProfileComboBox;
    private ActionButton openCompilerProfileSettingsButton;
    private JComboBox<WorksheetExternalRunType> runTypeComboBox;

    WorksheetSettingsSetForm(PsiFile file, WorksheetSettingsData settingsData) {
        myFile = file;
        myProject = file.getProject();
        $$$setupUI$$$();
        init(settingsData);
    }

    WorksheetSettingsSetForm(Project project, WorksheetSettingsData settingsData) {
        myFile = null;
        myProject = project;
        $$$setupUI$$$();
        init(settingsData);
    }

    private void init(WorksheetSettingsData settingsData) {
        moduleComboBox.fillModules(myProject);

        WorksheetCommonSettings settings = myFile != null ?
                WorksheetFileSettings.apply(myFile) :
                WorksheetProjectSettings.apply(myProject);

        Module defaultModule = settings.getModuleFor();
        if (defaultModule != null) {
            moduleComboBox.setSelectedModule(defaultModule);
            boolean enabled = myFile == null || WorksheetFileSettings.isScratchWorksheet(myFile.getVirtualFile(), myFile.getProject());
            moduleComboBox.setEnabled(enabled);
        }

        runTypeComboBox.setModel(new DefaultComboBoxModel<>(WorksheetExternalRunType.getAllRunTypes()));
        runTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof WorksheetExternalRunType) {
                    value = ((WorksheetExternalRunType) value).getMenuText();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        runTypeComboBox.setSelectedItem(settingsData.runType());

        interactiveModeCheckBox.setSelected(settingsData.isInteractive());
        makeProjectBeforeRunCheckBox.setSelected(settingsData.isMakeBeforeRun());
        compilerProfileComboBox.setModel(new DefaultComboBoxModel<>(settingsData.profiles()));
        compilerProfileComboBox.setSelectedItem(settingsData.compilerProfile());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public PsiFile getFile() {
        return myFile;
    }

    public WorksheetExternalRunType getRunType() {
        return (WorksheetExternalRunType) runTypeComboBox.getSelectedItem();
    }

    public void onProfilesReload(ScalaCompilerSettingsProfile compilerProfile, ScalaCompilerSettingsProfile[] profiles) {
        compilerProfileComboBox.setSelectedItem(null);
        compilerProfileComboBox.setModel(new DefaultComboBoxModel<>(profiles));
        compilerProfileComboBox.setSelectedItem(compilerProfile);
    }

    public WorksheetSettingsData getFilledSettingsData() {
        return new WorksheetSettingsData(
                interactiveModeCheckBox.isSelected(),
                makeProjectBeforeRunCheckBox.isSelected(),
                (WorksheetExternalRunType) runTypeComboBox.getSelectedItem(),
                moduleComboBox.isEnabled() ? moduleComboBox.getSelectedModule() : null,
                (ScalaCompilerSettingsProfile) compilerProfileComboBox.getSelectedItem(),
                null
        );
    }

    public String getSelectedProfileName() {
        ScalaCompilerSettingsProfile selectedProfile = (ScalaCompilerSettingsProfile) compilerProfileComboBox.getSelectedItem();
        return selectedProfile == null ? null : selectedProfile.getName();
    }

    private void createUIComponents() {
        moduleComboBox = new ModulesComboBox();
        moduleComboBox.setToolTipText(ScalaBundle.message("worksheet.settings.panel.using.class.path.of.the.module"));

        openCompilerProfileSettingsButton = new ShowCompilerProfileSettingsButton(this).getActionButton();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        interactiveModeCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(interactiveModeCheckBox, this.$$$getMessageFromBundle$$$("messages/ScalaBundle", "worksheet.settings.panel.interactive.mode"));
        mainPanel.add(interactiveModeCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        makeProjectBeforeRunCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(makeProjectBeforeRunCheckBox, this.$$$getMessageFromBundle$$$("messages/ScalaBundle", "worksheet.settings.panel.change.make.button"));
        mainPanel.add(makeProjectBeforeRunCheckBox, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainPanel.add(moduleComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("messages/ScalaBundle", "worksheet.settings.panel.use.class.path.of.module"));
        mainPanel.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        compilerProfileComboBox = new JComboBox();
        mainPanel.add(compilerProfileComboBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("messages/ScalaBundle", "worksheet.settings.panel.compiler.profile"));
        mainPanel.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainPanel.add(openCompilerProfileSettingsButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        runTypeComboBox = new JComboBox();
        mainPanel.add(runTypeComboBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("messages/ScalaBundle", "worksheet.settings.panel.run.type"));
        mainPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
