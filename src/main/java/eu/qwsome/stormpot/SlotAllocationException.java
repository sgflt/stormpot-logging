package eu.qwsome.stormpot;

import java.io.Serial;

public class SlotAllocationException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = -1L;


  public SlotAllocationException(final String message) {
    super(message);
  }
}
