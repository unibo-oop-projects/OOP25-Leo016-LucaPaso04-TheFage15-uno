package uno.view.scenes.impl;

import uno.controller.api.MenuObserver;
import uno.view.components.api.StyledButton;
import uno.view.components.impl.StyledButtonImpl;
import uno.view.scenes.api.MenuScene;
import uno.view.style.UnoTheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import javax.swing.border.EmptyBorder;

/**
 * The panel (JPanel) representing the main menu screen.
 * This implementation uses a modern and accessible graphical style.
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SE_BAD_FIELD")
public final class MenuSceneImpl extends JPanel implements MenuScene {

    private static final long serialVersionUID = 1L;

    private static final Dimension RIGID_AREA_DIMENSION = new Dimension(0, 20);
    private static final EmptyBorder TITLE_EMPTY_BORDER = new EmptyBorder(0, 0, 40, 0);

    private MenuObserver observer;

    /**
     * Constructs the MenuSceneImpl, setting up the layout and components.
     */
    public MenuSceneImpl() {
        super(new GridBagLayout()); // Use GridBagLayout to center everything
        setBackground(UnoTheme.BACKGROUND_COLOR);

        // Inner panel to hold components vertically
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // Make transparent to show background

        // 1. Title
        final JLabel title = new JLabel("UNO");
        title.setFont(UnoTheme.TITLE_FONT);
        title.setForeground(UnoTheme.TEXT_COLOR);
        title.setAlignmentX(CENTER_ALIGNMENT);

        title.setBorder(TITLE_EMPTY_BORDER);

        // 2. Create Styled Buttons - Ensuring Uniform Size
        final int btnWidth = 350;
        final int btnHeight = 60;

        final StyledButton classicButton = new StyledButtonImpl("Classic Mode");
        classicButton.setSize(btnWidth, btnHeight);
        classicButton.setMnemonic(KeyEvent.VK_C);

        final StyledButton flipButton = new StyledButtonImpl("Flip Mode");
        flipButton.setSize(btnWidth, btnHeight);
        flipButton.setMnemonic(KeyEvent.VK_F);

        final StyledButton allWildButton = new StyledButtonImpl("All Wild Mode");
        allWildButton.setSize(btnWidth, btnHeight);
        allWildButton.setMnemonic(KeyEvent.VK_W);

        // --- Rules Button ---
        final StyledButton rulesButton = new StyledButtonImpl("Game Rules");
        rulesButton.setSize(btnWidth, btnHeight);
        rulesButton.setMnemonic(KeyEvent.VK_R);

        final StyledButton quitButton = new StyledButtonImpl("Exit Game");
        quitButton.setSize(btnWidth, btnHeight);
        quitButton.setMnemonic(KeyEvent.VK_E);

        // 3. Add components to the inner panel
        contentPanel.add(title);
        contentPanel.add(classicButton.getComponent());
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));
        contentPanel.add(flipButton.getComponent());
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));
        contentPanel.add(allWildButton.getComponent());
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));
        // Add rules button with spacing
        contentPanel.add(rulesButton.getComponent());
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));

        contentPanel.add(quitButton.getComponent());

        // 4. Add the inner panel to the main scene (centered by GridBag)
        add(contentPanel, new GridBagConstraints());

        // 5. Link ActionListeners to the Observer
        classicButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartClassicGame();
            }
        });

        flipButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartFlipGame();
            }
        });

        allWildButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartAllWildGame();
            }
        });

        rulesButton.addActionListener(e -> {
            if (observer != null) {
                observer.onOpenRules();
            }
        });

        quitButton.addActionListener(e -> {
            if (observer != null) {
                observer.onQuit();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObserver(final MenuObserver observer) {
        this.observer = observer;
    }
}
