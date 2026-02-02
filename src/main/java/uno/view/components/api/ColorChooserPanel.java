package uno.view.components.api;

/**
 * Interface representing a UI component used to select a CardColor.
 */
public interface ColorChooserPanel {

    /**
     * Closes the chooser component after a selection has been made 
     * or the operation is canceled.
     */
    void closeChooser();

    /**
     * Returns the selected color if applicable, or handles the internal state.
     * (Optional: often these components notify an observer instead of returning a value).
     */
}
