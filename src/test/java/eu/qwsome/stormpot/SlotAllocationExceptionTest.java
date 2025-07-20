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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class SlotAllocationExceptionTest {

  @Test
  void constructor_shouldCreateExceptionWithMessage() {
    final var message = "Test allocation failure";

    final var exception = new SlotAllocationException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isNull();
  }


  @Test
  void constructor_shouldCreateExceptionWithNullMessage() {
    final var exception = new SlotAllocationException(null);

    assertThat(exception.getMessage()).isNull();
    assertThat(exception.getCause()).isNull();
  }


  @Test
  void constructor_shouldCreateExceptionWithEmptyMessage() {
    final var message = "";

    final var exception = new SlotAllocationException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isNull();
  }


  @Test
  void exception_shouldBeInstanceOfRuntimeException() {
    final var exception = new SlotAllocationException("test");

    assertThat(exception).isInstanceOf(RuntimeException.class);
  }


  @Test
  void exception_shouldHaveNullCauseByDefault() {
    final SlotAllocationException exception = new SlotAllocationException("Test message");

    assertThat(exception.getCause()).isNull();
  }


  @Test
  void exception_shouldBeSerializable() throws IOException, ClassNotFoundException {
    final var message = "Serialization test";
    final var original = new SlotAllocationException(message);

    final var baos = new ByteArrayOutputStream();
    try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
    }

    final byte[] serialized = baos.toByteArray();

    final ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
    final SlotAllocationException deserialized;
    try (final var ois = new ObjectInputStream(bais)) {
      deserialized = (SlotAllocationException) ois.readObject();
    }

    assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
    assertThat(deserialized.getCause()).isNull();
  }


  @Test
  void exception_shouldHaveCorrectStackTrace() {
    final SlotAllocationException exception = new SlotAllocationException("Stack trace test");

    final StackTraceElement[] stackTrace = exception.getStackTrace();

    assertThat(stackTrace).isNotEmpty();
    assertThat(stackTrace[0].getMethodName()).isEqualTo("exception_shouldHaveCorrectStackTrace");
    assertThat(stackTrace[0].getClassName()).isEqualTo(SlotAllocationExceptionTest.class.getName());
  }


  @Test
  void exception_shouldSupportSuppressedExceptions() {
    final SlotAllocationException exception = new SlotAllocationException("Main exception");
    final RuntimeException suppressed1 = new RuntimeException("Suppressed 1");
    final RuntimeException suppressed2 = new RuntimeException("Suppressed 2");

    exception.addSuppressed(suppressed1);
    exception.addSuppressed(suppressed2);

    final Throwable[] suppressedExceptions = exception.getSuppressed();
    assertThat(suppressedExceptions).hasSize(2);
    assertThat(suppressedExceptions[0]).isEqualTo(suppressed1);
    assertThat(suppressedExceptions[1]).isEqualTo(suppressed2);
  }


  @Test
  void toString_shouldIncludeClassNameAndMessage() {
    final var message = "Test message for toString";
    final var exception = new SlotAllocationException(message);

    final String toString = exception.toString();

    assertThat(toString)
        .contains("SlotAllocationException")
        .contains(message);
  }


  @Test
  void toString_shouldHandleNullMessage() {
    final var exception = new SlotAllocationException(null);

    final var toString = exception.toString();

    assertThat(toString)
        .contains("SlotAllocationException")
        .doesNotContain("null");
  }
}
