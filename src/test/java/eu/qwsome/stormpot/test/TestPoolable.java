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

import stormpot.Poolable;
import stormpot.Slot;

public class TestPoolable implements Poolable {
  private final Slot slot;
  private final String value;
  private boolean released = false;


  public TestPoolable(final Slot slot, final String value) {
    this.slot = slot;
    this.value = value;
  }


  public TestPoolable(final Slot slot) {
    this(slot, "test-value");
  }


  @Override
  public void release() {
    if (isReleased()) {
      throw new IllegalStateException("Object already released");
    }
    this.released = true;
    this.slot.release(this);
  }


  public String getValue() {
    return this.value;
  }


  public boolean isReleased() {
    return this.released;
  }


  public Slot getSlot() {
    return this.slot;
  }
}
