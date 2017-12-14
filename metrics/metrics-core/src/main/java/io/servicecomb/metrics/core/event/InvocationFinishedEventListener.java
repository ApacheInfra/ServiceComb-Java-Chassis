/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.event;

import io.servicecomb.core.metrics.InvocationFinishedEvent;
import io.servicecomb.foundation.common.event.Event;
import io.servicecomb.foundation.common.event.EventListener;
import io.servicecomb.metrics.core.registry.InvocationThreadLocalCache;
import io.servicecomb.metrics.core.registry.ThreadLocalMonitorManager;
import io.servicecomb.swagger.invocation.InvocationType;

public class InvocationFinishedEventListener implements EventListener {
  @Override
  public Class<? extends Event> getConcernedEvent() {
    return InvocationFinishedEvent.class;
  }

  @Override
  public void process(Event data) {
    InvocationFinishedEvent event = (InvocationFinishedEvent) data;
    InvocationThreadLocalCache cache = ThreadLocalMonitorManager.getInvocationMonitor()
        .getInvocationThreadLocalCache(event.getOperationName());
    cache.increaseCallCount(event.getInvocationType());
    //only producer invocation need increase queue time
    if (InvocationType.PRODUCER.equals(event.getInvocationType())) {
      cache.increaseExecutionTime(event.getProcessElapsedNanoTime());
    }
  }
}
