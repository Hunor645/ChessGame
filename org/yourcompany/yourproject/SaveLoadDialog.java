package org.yourcompany.yourproject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Save/Load dialog providing slot selection UI.
 *
 * Presents a list of save slots to the user and allows selecting a slot
 * for saving or loading a game. This class only handles the UI selection
 * and returns the chosen slot index via `getSelectedSlot()`.
 */
public class SaveLoadDialog extends JDialog {
    private int selectedSlot = -1;
    private boolean isSave = false;

    public SaveLoadDialog(JFrame parent, boolean isSave) {
        super(parent, isSave ? "Save" : "Load", true);
        this.isSave = isSave;
        initUI();
    }

    private void initUI() {
        setSize(400, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // List of save slots
        List<SaveManager.SaveSlot> slots = SaveManager.listSaveSlots();
        DefaultListModel<SaveManager.SaveSlot> model = new DefaultListModel<>();

        if (isSave) {
            // For saving: show all slots (existing and empty)
            for (int i = 0; i < 10; i++) {
                SaveManager.SaveSlot slot = null;
                for (SaveManager.SaveSlot s : slots) {
                    if (s.slot == i) {
                        slot = s;
                        break;
                    }
                }
                if (slot != null) {
                    model.addElement(slot);
                } else {
                    model.addElement(new SaveManager.SaveSlot(i, "Slot " + i + " - Empty", 0));
                }
            }
        } else {
            // For loading: show only existing slots
            model.addAll(slots);
        }

        JList<SaveManager.SaveSlot> slotList = new JList<>(model);
        slotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(slotList), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton(isSave ? "Save" : "Load");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
                if (slotList.getSelectedIndex() >= 0) {
                selectedSlot = slotList.getSelectedValue().slot;
                dispose();
            } else {
                    JOptionPane.showMessageDialog(this, "Please select a slot!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> {
            selectedSlot = -1;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }
}
