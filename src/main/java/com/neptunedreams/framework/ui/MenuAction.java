package com.neptunedreams.framework.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/2/17
 * <p>Time: 11:45 PM
 *
 * @author Miguel Muñoz
 */
abstract class MenuAction extends AbstractAction {
  // "method.invocation" Makes no sense! Call to putValue() "not allowed on the given receiver."
  // "argument" makes no sense since null is a valid value for icon in constructor.
  @SuppressWarnings({"method.invocation", "argument"})
  MenuAction(@NonNull String name, @Nullable Icon icon, Character acceleratorKey) {
    super(name, icon); // icon may be nullable here.
    KeyStroke keyStroke = KeyStroke.getKeyStroke(acceleratorKey, java.awt.event.InputEvent.META_DOWN_MASK);
    putValue(Action.ACCELERATOR_KEY, keyStroke);
  }

  @SuppressWarnings("UseOfClone")
  @Override
  public MenuAction clone() throws CloneNotSupportedException {
    return (MenuAction) super.clone();
  }
}
