package uk.ac.kent.software_dev.src;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;

/**
 * Represents a label to prompt which data to enter, a field to enter it into
 * and a warning that is shown when the data is incorrect. Allows easy handling
 * of the data fields by the button action listener
 */
abstract class DataEntryArea extends JPanel {
    private final JComponent field;
    private JPanel warning;

    /**
     * Constructs a DataEntryArea using a normal JTextField
     * 
     * @param label   The label for the field
     * @param warning The warning to display if the input is invalid
     */
    public DataEntryArea(final String label, final String warning) {
        this.field = new JTextField();
        helpContruct(label, warning);
    }

    /**
     * Constructs a DataEntryArea using the provided JTextField
     * 
     * @param label   The label for the field
     * @param warning The warning to display if the input is invalid
     */
    public DataEntryArea(final JComponent field, final String label, final String warning) {
        this.field = field;
        helpContruct(label, warning);
    }

    /**
     * Helper method for the constructor
     * 
     * @param label   The label for the field
     * @param warning The warning to display if the input is invalid
     */
    private void helpContruct(final String label, final String warning) {
        this.warning = new JPanel();
        this.warning.setLayout(new BoxLayout(this.warning, BoxLayout.X_AXIS));
        final JLabel warningLabel = ComponentFactory.makeLabel(warning, ComponentFactory.LABEL_FONT, false);
        warningLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        this.warning.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        warningLabel.setForeground(Color.red);
        this.warning.add(warningLabel);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JPanel field = new JPanel();
        field.setLayout(new GridLayout(1, 2));
        field.add(new JLabel(label));
        field.add(this.field);
        field.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        this.add(field);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * Checks whether the input is valid
     * 
     * @param input Input the validate
     * @return Whether the input is valid
     */
    protected abstract boolean inputValid(String input);

    public boolean validateInput() {
        if (field instanceof JTextComponent) {
            final JTextComponent textField = (JTextComponent) field;
            if (textField.getText() == null || !inputValid(textField.getText())) {
                this.add(warning);
                return false;
            } else {
                this.remove(warning);
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Retrieves the contents input by the user
     * 
     * @return The contents
     */
    public Object getContents() {
        if (field instanceof JTextComponent) {
            final JTextComponent textField = (JTextComponent) field;
            final String text = field instanceof JPasswordField
                    ? String.valueOf(((JPasswordField) field).getPassword())
                    : textField.getText();
            return text;
        } else if (field instanceof JComboBox) {
            final JComboBox<?> comboBox = (JComboBox<?>) field;
            return comboBox.getSelectedItem();
        } else {
            return null;
        }
    }
}