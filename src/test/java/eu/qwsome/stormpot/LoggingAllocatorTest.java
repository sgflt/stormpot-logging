/*
 * Copyright 2024 qwsome
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.qwsome.stormpot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.sql.SQLException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.qwsome.stormpot.test.TestLoggingAllocator;
import eu.qwsome.stormpot.test.TestPoolable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import stormpot.Slot;

@ExtendWith(MockitoExtension.class)
class LoggingAllocatorTest {

  @Mock
  private Slot mockSlot;

  private ListAppender<ILoggingEvent> logAppender;
  private Logger logger;


  @BeforeEach
  void setUp() {
    this.logger = (Logger) LoggerFactory.getLogger(LoggingAllocator.class);
    this.logAppender = new ListAppender<>();
    this.logAppender.start();
    this.logger.addAppender(this.logAppender);
    this.logger.setLevel(Level.DEBUG);
  }


  @Test
  void allocate_shouldReturnObjectFromTryAllocate_whenSuccessful() {
    final TestLoggingAllocator allocator = TestLoggingAllocator.successfulAllocator("test-value");

    final TestPoolable result = allocator.allocate(this.mockSlot);

    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo("test-value");
    assertThat(result.getSlot()).isEqualTo(this.mockSlot);
    assertThat(this.logAppender.list).isEmpty();
  }


  @Test
  void allocate_shouldThrowSlotAllocationException_whenTryAllocateThrowsException() {
    final var originalException = new RuntimeException("Allocation failed");
    final var allocator = TestLoggingAllocator.throwingAllocator(originalException);

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class)
        .hasMessage("Failed to allocate slot");
  }


  @Test
  void allocate_shouldLogWarning_whenTryAllocateThrowsException() {
    final var originalException = new RuntimeException("Allocation failed");
    final var allocator = TestLoggingAllocator.throwingAllocator(originalException);

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class);

    assertThat(this.logAppender.list).hasSize(1);
    final ILoggingEvent logEvent = this.logAppender.list.getFirst();
    assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
    assertThat(logEvent.getFormattedMessage()).contains("Failed to allocate slot");
    assertThat(logEvent.getThrowableProxy().getMessage()).isEqualTo("java.lang.RuntimeException: Allocation failed");
  }


  @Test
  void allocate_shouldLogWarningWithCorrectException_whenTryAllocateThrowsCheckedException() {
    final var originalException = new IOException("IO error during allocation");
    final var allocator = new TestLoggingAllocator(slot -> {
      throw new RuntimeException(originalException);
    });

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class);

    assertThat(this.logAppender.list).hasSize(1);
    final ILoggingEvent logEvent = this.logAppender.list.getFirst();
    assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
    assertThat(logEvent.getThrowableProxy().getMessage()).contains("IO error during allocation");
  }


  @Test
  void allocate_shouldLogWarningWithCorrectException_whenTryAllocateThrowsSQLException() {
    final var originalException = new SQLException("Database connection failed", "08001", 1);
    final var allocator = new TestLoggingAllocator(slot -> {
      throw new RuntimeException(originalException);
    });

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class);

    assertThat(this.logAppender.list).hasSize(1);
    final ILoggingEvent logEvent = this.logAppender.list.getFirst();
    assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
    assertThat(logEvent.getThrowableProxy().getMessage()).contains("Database connection failed");
  }


  @Test
  void allocate_shouldPreserveCauseChain_whenTryAllocateThrowsNestedExceptions() {
    final var rootCause = new IOException("Root cause");
    final var middleException = new RuntimeException("Middle exception", rootCause);
    final var allocator = TestLoggingAllocator.throwingAllocator(middleException);

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class)
        .hasMessage("Failed to allocate slot");
  }


  @Test
  void allocate_shouldHandleMultipleConsecutiveCalls() {
    final var allocator = TestLoggingAllocator.successfulAllocator();

    final var result1 = allocator.allocate(this.mockSlot);
    final var result2 = allocator.allocate(this.mockSlot);

    assertThat(result1).isNotNull();
    assertThat(result2).isNotNull();
    assertThat(result1).isNotSameAs(result2);
    assertThat(this.logAppender.list).isEmpty();
  }


  @Test
  void allocate_shouldHandleMixedSuccessAndFailureCalls() {
    final var successAllocator = TestLoggingAllocator.successfulAllocator();
    final var failingAllocator = TestLoggingAllocator.throwingAllocator(
        new RuntimeException("Allocation failed")
    );

    final var result = successAllocator.allocate(this.mockSlot);
    assertThat(result).isNotNull();

    assertThatThrownBy(() -> failingAllocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class);

    final var result2 = successAllocator.allocate(this.mockSlot);
    assertThat(result2).isNotNull();

    assertThat(this.logAppender.list).hasSize(1);
  }


  @Test
  void allocate_shouldThrowSlotAllocationException_evenWhenMultipleExceptionsOccur() {
    final TestLoggingAllocator allocator = TestLoggingAllocator.throwingAllocator(
        new RuntimeException("Original allocation failure")
    );

    assertThatThrownBy(() -> allocator.allocate(this.mockSlot))
        .isInstanceOf(SlotAllocationException.class)
        .hasMessage("Failed to allocate slot");
  }


  @Test
  void allocate_shouldPassCorrectSlot_toTryAllocateMethod() {
    final var allocator = new TestLoggingAllocator(slot -> {
      assertThat(slot).isSameAs(this.mockSlot);
      return new TestPoolable(slot);
    });

    final var result = allocator.allocate(this.mockSlot);

    assertThat(result).isNotNull();
    assertThat(result.getSlot()).isEqualTo(this.mockSlot);
  }
}
