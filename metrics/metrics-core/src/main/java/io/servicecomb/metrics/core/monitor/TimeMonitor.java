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

package io.servicecomb.metrics.core.monitor;

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MinGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

import io.servicecomb.metrics.core.metric.TimeMetric;

public class TimeMonitor {
  private final StepCounter total;

  private final StepCounter count;

  private final MinGauge min;

  private final MaxGauge max;

  public void update(long value) {
    if (value > 0) {
      total.increment(value);
      count.increment();
      max.update(value);
      min.update(value);
    }
  }

  public TimeMonitor(String name) {
    total = new StepCounter(MonitorConfig.builder(name + ".total").build());
    count = new StepCounter(MonitorConfig.builder(name + ".count").build());
    min = new MinGauge(MonitorConfig.builder(name + ".min").build());
    max = new MaxGauge(MonitorConfig.builder(name + ".max").build());
  }

  public TimeMetric toMetric(int pollerIndex) {
    return new TimeMetric(total.getCount(pollerIndex), count.getCount(pollerIndex),
        min.getValue(pollerIndex), max.getValue(pollerIndex));
  }
}
