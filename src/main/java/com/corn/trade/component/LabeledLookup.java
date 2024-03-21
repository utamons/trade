package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class LabeledLookup extends JPanel {
	private final JTextField searchField;
	private final JPopupMenu popupMenu;
	private final List<String> items;
	private JMenuItem selectedItem;
	private int selectedIndex = -1;

	public LabeledLookup(String labelText, List<String> items, int padding, int height, Consumer<String> consumer) {
		setLayout(new BorderLayout());
		this.items = items;

		JLabel label = new JLabel(labelText);

		this.setMaximumSize(new Dimension(5000, height));
		this.setMinimumSize(new Dimension(500, height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);

		searchField = new JTextField(5);
		popupMenu = new JPopupMenu();

		add(label, BorderLayout.WEST);
		add(searchField, BorderLayout.EAST);

		searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "downArrow");
		popupMenu.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
		popupMenu.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "downArrow");
		popupMenu.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "upArrow");

		searchField.getActionMap().put("downArrow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popupMenu.requestFocusInWindow();
				selectedItem = (JMenuItem) popupMenu.getSubElements()[selectedIndex].getComponent();
				selectedItem.menuSelectionChanged(true);
			}
		});
		popupMenu.getActionMap().put("enterPressed", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (popupMenu.isVisible() && selectedItem != null) {
					selectedItem.doClick();
				}
			}
		});

		popupMenu.getActionMap().put("downArrow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (popupMenu.isVisible() && selectedIndex != -1) {
					if (selectedIndex < popupMenu.getSubElements().length - 1) {
						selectedIndex++;
					} else {
						selectedIndex = 0;
					}
					if (selectedItem != null)
						selectedItem.menuSelectionChanged(false);
					selectedItem = (JMenuItem) popupMenu.getSubElements()[selectedIndex].getComponent();
					selectedItem.menuSelectionChanged(true);
				}
			}
		});

		popupMenu.getActionMap().put("upArrow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (popupMenu.isVisible() && selectedIndex != -1) {
					if (selectedIndex > 0) {
						selectedIndex--;
					} else {
						selectedIndex = popupMenu.getSubElements().length - 1;
					}
					if (selectedItem != null)
						selectedItem.menuSelectionChanged(false);
					selectedItem = (JMenuItem) popupMenu.getSubElements()[selectedIndex].getComponent();
					selectedItem.menuSelectionChanged(true);
				}
			}
		});

		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				updatePopupMenu(e.getKeyChar(), consumer);
			}
		});

		searchField.addActionListener(e -> {
			popupMenu.setVisible(false);
			if (consumer != null) {
				consumer.accept(searchField.getText());
			}
		});
	}

	@SuppressWarnings("unused")
	public void setSuccessStatus(boolean successStatus) {
		searchField.setBackground(successStatus ? UIManager.getColor("TextField.background") : Color.RED);
	}

	private void updatePopupMenu(char keyChar, Consumer<String> consumer) {
		popupMenu.removeAll();
		selectedIndex = -1;

		String filter = searchField.getText() + keyChar;
		List<String> filteredItems = items.stream()
		                                  .filter(s -> s.toLowerCase().contains(filter.toLowerCase()))
		                                  .toList();
		int height = 0;
		for (String item : filteredItems) {
			JMenuItem menuItem = new JMenuItem(item);
			height += menuItem.getPreferredSize().height;
			menuItem.addActionListener(e -> {
				searchField.setText(item);
				consumer.accept(item);
				popupMenu.setVisible(false);
			});
			popupMenu.add(menuItem);
		}

		if (filteredItems.isEmpty()) {
			popupMenu.setVisible(false);
		} else {
			selectedIndex = 0;
			if (!popupMenu.isVisible()) {
				popupMenu.setVisible(true);
				popupMenu.setPopupSize(searchField.getWidth(), height);
				popupMenu.pack();
				popupMenu.show(searchField, 0, searchField.getHeight());
			} else {
				popupMenu.pack();
				popupMenu.setPopupSize(searchField.getWidth(), height);
			}
		}

		searchField.requestFocusInWindow();
	}

	public void setEnabled(boolean enabled) {
		searchField.setEnabled(enabled);
	}

	public void clear() {
		searchField.setText("");
	}

	public void setText(String assetName) {
		searchField.setText(assetName);
	}
}

