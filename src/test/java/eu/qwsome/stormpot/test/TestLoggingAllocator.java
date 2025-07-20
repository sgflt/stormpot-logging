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
package eu.qwsome.stormpot.test;

import java.util.function.Function;

import eu.qwsome.stormpot.LoggingAllocator;
import stormpot.Slot;

public class TestLoggingAllocator extends LoggingAllocator<TestPoolable> {
  private final Function<Slot, TestPoolable> allocationFunction;


  public TestLoggingAllocator(final Function<Slot, TestPoolable> allocationFunction) {
    this.allocationFunction = allocationFunction;
  }


  public TestLoggingAllocator() {
    this(TestPoolable::new);
  }


  @Override
  public TestPoolable tryAllocate(final Slot slot) throws Exception {
    return this.allocationFunction.apply(slot);
  }


  @Override
  public void deallocate(final TestPoolable poolable) throws Exception {
    // Test implementation - no specific cleanup needed
  }


  public static TestLoggingAllocator throwingAllocator(final Exception exception) {
    return new TestLoggingAllocator(slot -> {
      throw new RuntimeException(exception);
    });
  }


  public static TestLoggingAllocator successfulAllocator() {
    return new TestLoggingAllocator();
  }


  public static TestLoggingAllocator successfulAllocator(final String value) {
    return new TestLoggingAllocator(slot -> new TestPoolable(slot, value));
  }
}
