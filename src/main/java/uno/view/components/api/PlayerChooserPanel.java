package uno.view.components.api;

/**
 * Interface representing a UI component used to select a specific Player 
 * (e.g., for targeted effects like swapping hands).
 */
public interface PlayerChooserPanel {

    /**
     * Closes the chooser component after a selection has been made 
     * or the operation is canceled.
     */
    void closeChooser();
}
