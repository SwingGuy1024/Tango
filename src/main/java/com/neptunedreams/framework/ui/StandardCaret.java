package com.neptunedreams.framework.ui;

import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

import org.checkerframework.checker.initialization.qual.UnderInitialization;

/**
 * <p>Implements Standard rules for extending the selection, consistent with the standard
 * behavior for extending the selection in all word processors, browsers, and other text
 * editing tools, on all platforms. Without this, Swing's behavior on extending the
 * selection is inconsistent with all other text editing tools.
 * </p><p>
 * Swing components don't handle selectByWord the way most UI text components do. If you
 * double-click on a word, they will all select the entire word. But if you do a 
 * click-and-drag, most components will (a) select the entire clicked word, and
 * (b) extend the selection a word at a time as the user drags across the text. And if
 * you double-click on a word and follow that with a shift-click, most components will
 * also extend the selection a word at a time.  Swing components handle a double-clicked
 * word the standard way, but do not handle click-and-drag or shift-click correctly. This
 * caret, which replaces the standard DefaultCaret, fixes this.</p>
 * <p>Created by IntelliJ IDEA.</p>
 * <p>Date: 2/23/20</p>
 * <p>Time: 10:58 PM</p>
 *
 * @author Miguel Muñoz
 */
public class StandardCaret extends DefaultCaret {
  // In the event of a double click, these are the positions of the low end and high end
  // of the word that was clicked.
  private int highMark;
  private int lowMark;
  private boolean selectingByWord = false; // true when the last selection was done by word.
  private boolean selectingByLine = false; // true when the last selection was done by paragraph. 
  
  private boolean tripleClickSelectsParagraph = true;
  private BiTextFunction lineStartFunction = StandardCaret::getParagraphStart;
  private BiTextFunction lineEndFunction = StandardCaret::getParagraphEnd;
  
  private static final PropertyChangeListener editableChangeListener = new EditableListener();
  private static int defaultBlinkRate = 0;

  /**
   * Instantiate an StandardCaret.
   */
  public StandardCaret() {
    super();
  }

  @Override
  public void setBlinkRate(int rate) {
    super.setBlinkRate(rate);
    if (rate != 0) {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      defaultBlinkRate = rate;
    }
  }

  /**
   * Instantiate a StandardCaret with the specified value for TripleClickSelectsParagraph;
   * @param tripleClickSelectsParagraph true for triple clicks selecting by paragraph, false for by line.
   * @see #setTripleClickSelectsParagraph(boolean) 
   */
  public StandardCaret(boolean tripleClickSelectsParagraph) {
    this();
    setTripleClickSelectsParagraph(tripleClickSelectsParagraph);
  }

  /**
   * <p>Returns the value of tripleClickSelectsParagraph. If this is false, a triple-click selects by
   * soft line breaks.</p>
   * <p>This value defaults to true.</p>
   * @return True if a triple click selects by hard line breaks, false if it selects by soft line breaks.
   */
  public boolean isTripleClickSelectsParagraph() {
    return tripleClickSelectsParagraph;
  }

  /**
   * <p>Sets the value of tripleClickSelectsByParagraph. This determines whether a triple click expands the
   * selection to the entire paragraph, which is delineated by hard line breaks, or just the line of text, which
   * is delineated by soft line breaks.</p>
   * <p>This value defaults to true.</p>
   * @param tripleClickSelectsParagraph To respond to triple clicks by selecting the full paragraph, set this to true.
   *                                   To use select just the line, set this to false.
   */
  private void setTripleClickSelectsParagraph(@UnderInitialization StandardCaret this, boolean tripleClickSelectsParagraph) {
    
    this.tripleClickSelectsParagraph = tripleClickSelectsParagraph;
    if (tripleClickSelectsParagraph) {
      lineStartFunction = StandardCaret::getParagraphStart;
      lineEndFunction = StandardCaret::getParagraphEnd;
    } else {
      lineStartFunction = Utilities::getRowStart;
      lineEndFunction = Utilities::getRowEnd;
    }
  }

  /**
   * <p>Install this Caret into a JTextComponent. Carets may not be shared among multiple
   * components.</p>
   * @param component The component to use the StandardCaret.
   */
  public void installInto(JTextComponent component) {
    replaceCaret(component, this);
  }

  /**
   * Install a new StandardCaret into a JTextComponent, such as a JTextField or
   * JTextArea, and starts the Caret blinking using the same blink-rate as the
   * previous Caret.
   *
   * @param components The JTextComponent subclass
   */
  public static void installStandardCaret(JTextComponent... components) {
    for (JTextComponent textComponent: components) {
      replaceCaret(textComponent, new StandardCaret());
    }
  }

  /**
   * Installs the specified Caret into the JTextComponent, and starts the Caret blinking
   * using the same blink-rate as the previous Caret. This works with any Caret
   *
   * @param component The text component to get the new Caret
   * @param caret     The new Caret to install
   */
  public static void replaceCaret(final JTextComponent component, final Caret caret) {
    final Caret priorCaret = component.getCaret();
    int blinkRate = priorCaret.getBlinkRate();
    if (priorCaret instanceof PropertyChangeListener) {
      // For example, com.apple.laf.AquaCaret, the troublemaker, installs this listener
      // which doesn't get removed when the Caret gets uninstalled.
      component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
    }
    priorCaret.deinstall(component);
    component.removePropertyChangeListener(editableChangeListener);
    component.setCaret(caret);
    component.addPropertyChangeListener("editable", editableChangeListener);
    caret.install(component);
    caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    // if user is doing a shift-click. Construct a new MouseEvent that happened at one
    // end of the word, and send that to super.mousePressed().
    boolean isExtended = isExtendSelection(e);
    if (selectingByWord && isExtended) {
      MouseEvent alternateEvent = getRevisedMouseEvent(e, Utilities::getWordStart, Utilities::getWordEnd);
      super.mousePressed(alternateEvent);
    } else if (selectingByLine && isExtended) {
      MouseEvent alternateEvent = getRevisedMouseEvent(e, lineStartFunction, lineEndFunction);
      super.mousePressed(alternateEvent);
    } else  {
      if (!isExtended) {
        int clickCount = e.getClickCount();
        selectingByWord = clickCount == 2;
        selectingByLine = clickCount == 3;
      }
      super.mousePressed(e); // let the system select the clicked word
      // save the low end of the selected word.
      lowMark = getMark();
      if (selectingByWord || selectingByLine) {
        // User did a double- or triple-click...
        // They've selected the whole word. Record the high end.
        highMark = getDot();
      } else {
        // Not a multi-click.
        highMark = lowMark;
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (selectingByLine) {
      int mark = getMark();
      int dot = getDot();
      lowMark = Math.min(mark, dot);
      highMark = Math.max(mark, dot);
    }
  }

  private MouseEvent getRevisedMouseEvent(final MouseEvent e, final BiTextFunction getStart, final BiTextFunction getEnd) {
    int newPos;
    int pos = getPos(e);
    final JTextComponent textComponent = getComponent();
    try {
      if (pos > highMark) {
        newPos = getEnd.loc(textComponent, pos);
        setDot(lowMark);
      } else if (pos < lowMark) {
        newPos = getStart.loc(textComponent, pos);
        setDot(highMark);
      } else {
        if (getMark() == lowMark) {
          newPos = getEnd.loc(textComponent, pos);
        } else {
          newPos = getStart.loc(textComponent, pos);
        }
        pos = -1; // ensure we make a new event
      }
    } catch (BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
    MouseEvent alternateEvent;
    if (newPos == pos) {
      alternateEvent = e;
    } else {
      alternateEvent = makeNewEvent(e, newPos);
    }
    return alternateEvent;
  }

  private boolean isExtendSelection(MouseEvent e) {
    // We extend the selection when the shift is down but control is not. Other modifiers don't matter.
    int modifiers = e.getModifiersEx();
    int shiftAndControlDownMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
    return (modifiers & shiftAndControlDownMask) == MouseEvent.SHIFT_DOWN_MASK;
  }

  @Override
  public void setDot(final int dot, final Position.Bias dotBias) {
    super.setDot(dot, dotBias);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if (!selectingByWord && !selectingByLine) {
      super.mouseDragged(e);
    } else {
      BiTextFunction getStart;
      BiTextFunction getEnd;
      if (selectingByWord) {
        getStart = Utilities::getWordStart;
        getEnd = Utilities::getWordEnd;
      } else {
        // selecting by paragraph
        getStart = lineStartFunction;
        getEnd = lineEndFunction;
      }
      // Calling super.mouseDragged() just calls moveDot() after getting the position. We can do
      // the same thing...
      // There's no "setMark()" method. You can set the mark by calling setDot(). It sets
      // both the mark and the dot to the same place. Then you can call moveDot() to put
      // the dot somewhere else.
      if ((!e.isConsumed()) && SwingUtilities.isLeftMouseButton(e)) {
        int pos = getPos(e);
        JTextComponent component = getComponent();
        try {
          if (pos > highMark) {
            int wordEnd = getEnd.loc(component, pos);
            setDot(lowMark);
            moveDot(wordEnd);
          } else if (pos < lowMark) {
            int wordStart = getStart.loc(component, pos);
            setDot(wordStart); // Sets the mark, too
            moveDot(highMark);
          } else {
            setDot(lowMark);
            moveDot(highMark);
          }
        } catch (BadLocationException ex) {
          //noinspection CallToPrintStackTrace
          ex.printStackTrace();
        }
      }
    }
  }

  private int getPos(final MouseEvent e) {
    JTextComponent component = getComponent();
    Point2D pt = new Point2D.Double(e.getX(), e.getY());
    Position.Bias[] biasRet = new Position.Bias[1];
    return component.getUI().viewToModel2D(component, pt, biasRet);
  }
  
  private MouseEvent makeNewEvent(MouseEvent e, int pos) {
    JTextComponent component = getComponent();
    try {
      // I'm guessing what I should set for the Bias. If anyone tries this using a right-to-left language, this
      // might not work. 
      Rectangle2D rect = component.getUI().modelToView2D(component, pos, Position.Bias.Forward);
      //noinspection NumericCastThatLosesPrecision
      return new MouseEvent(
          component,
          e.getID(),
          e.getWhen(),
          e.getModifiersEx(),
          (int) rect.getX(),
          (int) rect.getY(),
          e.getClickCount(),
          e.isPopupTrigger(),
          e.getButton()
      );
    } catch (BadLocationException ev) {
      //noinspection CallToPrintStackTrace
      ev.printStackTrace();
      throw new IllegalStateException(ev);
    }
  }

  /**
   * Don't use this. I should throw CloneNotSupportedException, but it won't compile if I
   * do. Changing the access to protected doesn't help. If you don't believe me, try it
   * yourself. This is because the base class implements Cloneable.
   * @return A bad clone of this.
   */      
  @SuppressWarnings({"CloneReturnsClassType", "UseOfClone"})
  @Override
  public Object clone() {
    return super.clone();
  }
  
  @FunctionalInterface
  private interface BiTextFunction {
    int loc(JTextComponent component, int position) throws BadLocationException;
  }
  
  private static int getParagraphStart(JTextComponent c, int offset) {
    return Utilities.getParagraphElement(c, offset).getStartOffset();
  }
  
  private static int getParagraphEnd(JTextComponent c, int offset) {
    return Utilities.getParagraphElement(c, offset).getEndOffset();
  }
  
  private static class EditableListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      boolean isEditable = (Boolean) evt.getNewValue();
      int focusId = isEditable? FocusEvent.FOCUS_GAINED : FocusEvent.FOCUS_LOST;
      JTextComponent textComponent = (JTextComponent) evt.getSource();
      final DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
      FocusEvent dummyFocusEvent = new FocusEvent(textComponent, focusId);
      if (isEditable) {
        if (textComponent.hasFocus()) {
          caret.focusGained(dummyFocusEvent); // sets the caret visible, among other things
          caret.setBlinkRate(defaultBlinkRate);
        }
      } else {
        if (textComponent.hasFocus()) {
          caret.focusLost(dummyFocusEvent); // stops caret blinking
          // The caret.focusLost() method only turns this on under certain conditions, because it seems to assume that
          // the state should only change when the focus changes. But clicking the edit button should also 
          // preserve the selection state, which would require leaving this on when the focus doesn't change.
          // So we do it here.
//          caret.setBlinkRate(0); // stop blinking the caret.
          caret.setVisible(false);
          caret.setSelectionVisible(true);
        }
      }
    }
  }
}
