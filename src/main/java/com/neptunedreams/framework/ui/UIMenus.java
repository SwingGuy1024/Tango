package com.neptunedreams.framework.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;
import javax.swing.FocusManager;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/2/17
 * <p>Time: 11:43 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings({"HardCodedStringLiteral", "Singleton"})
public enum UIMenus implements CaretListener {
  Menu();
//  private Set<JTextComponent> registeredComponents = new HashSet<>();
//  private JTextComponent selectedComponent;
  private final FocusManager focusManager = FocusManager.getCurrentManager();
  private @Nullable JTextComponent caretOwner = null;
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction cutAction = new ClipboardAction("Cut", 'X', JTextComponent::cut);
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction copyAction = new ClipboardAction("Copy", 'C', JTextComponent::copy);
  @SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter"})
  private final MenuAction pasteAction = new ClipboardAction("Paste", 'V', JTextComponent::paste);

  UIMenus() {
    PropertyChangeListener focusListener = evt -> {
//      Component permFocusOwner = focusManager.getPermanentFocusOwner();
      Component permFocusOwner = (Component) evt.getNewValue();
      //noinspection ObjectEquality
      if (permFocusOwner != caretOwner) {
        if (caretOwner != null) {
          removeCaretListener(caretOwner);
          @SuppressWarnings("dereference.of.nullable") // makes no sense. caretOwner can't be null!
          final String text = caretOwner.getText();
//          System.out.printf("deFocus c with %s to %s%n", text, (permFocusOwner == null) ? "None" : permFocusOwner.getClass().toString());
        }
        if (permFocusOwner instanceof JTextComponent) {
          caretOwner = (JTextComponent) permFocusOwner;
          addCaretListener(caretOwner);
          @SuppressWarnings("dereference.of.nullable") // makes no sense. caretOwner can't be null!
          final String text = caretOwner.getText();
//          System.out.printf("Focus c with %s%n", text);
        }
      }
    };
    focusManager.addPropertyChangeListener("permanentFocusOwner", focusListener);
  }
  
  private final class ClipboardAction extends MenuAction {
    private final Consumer<JTextComponent> operation;
    private ClipboardAction(final String name, 
//                           @Nullable final Icon icon, 
                           final char acceleratorKey, 
                           Consumer<JTextComponent> operation) {
      super(name, null, acceleratorKey);
      this.operation = operation;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      Component focusOwner = focusManager.getPermanentFocusOwner();
      if (focusOwner instanceof JTextComponent textComponent) {
        // It better be!
        operation.accept(textComponent);
      }
    }

    @SuppressWarnings("UseOfClone")
    @Override
    public ClipboardAction clone() throws CloneNotSupportedException {
      return (ClipboardAction) super.clone();
    }
  }

  private void removeCaretListener(JTextComponent owner) {
    owner.removeCaretListener(this);
  }
  
  private void addCaretListener(@NotNull JTextComponent owner) {
    owner.addCaretListener(this);
  }
  
  public void installMenu(JFrame frame) {
    JMenu editMenu = new JMenu("Edit");
    JMenuItem cutItem = new JMenuItem(cutAction);
    editMenu.add(cutItem);
    editMenu.add(copyAction);
    editMenu.add(pasteAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(editMenu);
    frame.setJMenuBar(menuBar);
  }

  @Override
  public void caretUpdate(final CaretEvent e) {
    boolean selectionPresent = e.getDot() != e.getMark();
//    System.out.printf("Selection %b from %d =? %d%n", selectionPresent, e.getDot(), e.getMark());
    cutAction.setEnabled(selectionPresent);
    copyAction.setEnabled(selectionPresent);
    // Untested
    if (e.getSource() instanceof JTextComponent textComponent) {
      pasteAction.setEnabled(textComponent.isEditable());
    }
  }
}
