/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.datacollector.runner;

import com.streamsets.datacollector.util.ContainerError;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Small abstraction on top of blocking queue to model a pool of runners.
 */
public class RunnerPool <T> {

  /**
   * Internal blocking queue with all available runners
   */
  private ArrayBlockingQueue<T> queue;

  /**
   * Create new runner pool.
   *
   * @param runners Runners that this pool object should manage
   */
  public RunnerPool(List<T> runners) {
    queue = new ArrayBlockingQueue<>(runners.size());
    queue.addAll(runners);
  }

  /**
   * Get exclusive runner for use.
   *
   * @return Runner that is not being used by anyone else.
   * @throws PipelineRuntimeException Thrown in case that current thread is unexpectedly interrupted
   */
  public T getRunner() throws PipelineRuntimeException {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      throw new PipelineRuntimeException(ContainerError.CONTAINER_0801, e);
    }
  }

  /**
   * Return given runner back to the pool.
   *
   * @param runner Runner to be returned
   */
  public void returnRunner(T runner) {
    queue.add(runner);
  }
}
