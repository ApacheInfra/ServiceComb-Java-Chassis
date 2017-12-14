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

package io.servicecomb.metrics.core.metric;

import com.google.common.util.concurrent.AtomicDouble;

public class DoubleGaugeMetric extends AbstractMetric implements WritableMetric {

  private final AtomicDouble gauge;

  public DoubleGaugeMetric(String name) {
    super(name);
    this.gauge = new AtomicDouble(0);
  }

  @Override
  public void update(Number num) {
    gauge.set(num.doubleValue());
  }

  @Override
  public void increment() {
    gauge.addAndGet(1);
  }

  @Override
  public void decrement() {
    gauge.addAndGet(-1);
  }

  @Override
  public Number get() {
    return gauge.get();
  }
}
