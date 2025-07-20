package eu.qwsome.stormpot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stormpot.Allocator;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * By default, threaded pool allocation swallows exceptions. Runtime problems then may be hard to debug.
 * This class provides conventional logging capabilities.
 */
public abstract class LoggingAllocator<T extends Poolable> implements Allocator<T> {
  private static final Logger LOG = LoggerFactory.getLogger(LoggingAllocator.class);


  /**
   * Method tries to allocate {@link Poolable}. If allocation fails, the cause is logged and notified to stormpot.
   */
  protected abstract T tryAllocate(Slot slot) throws Exception;


  @Override
  public final T allocate(final Slot slot) {
    try {
      return tryAllocate(slot);
    } catch (final Exception e) {
      LOG.warn("Failed to allocate slot", e);
      throw new SlotAllocationException("Failed to allocate slot");
    }
  }
}
