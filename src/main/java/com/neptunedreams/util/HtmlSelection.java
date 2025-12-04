package com.neptunedreams.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/20/23
 * <p>Time: 3:32 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class HtmlSelection implements Transferable, ClipboardOwner {
  private final String data;

  /**
   * Create an HtmlSelection from the data.
   *
   * @param data Html data
   */
  public HtmlSelection(final String data) {
    this.data = data;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{DataFlavor.allHtmlFlavor};

  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.equals(DataFlavor.allHtmlFlavor);
  }

  @Override
  public @NotNull Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
    if (flavor.equals(DataFlavor.allHtmlFlavor)) {
      return (data == null) ? "" : data;
    }
    throw new UnsupportedFlavorException(flavor);
  }

  @Override
  public void lostOwnership(final Clipboard clipboard, final Transferable contents) { }
}
