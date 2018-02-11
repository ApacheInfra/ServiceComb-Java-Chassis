/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.foundation.metrics.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestHealthCheckerManager {

  @Test
  public void testRegistry() {

    List<HealthChecker> checkers = new ArrayList<>();

    HealthCheckerManager manager = new DefaultHealthCheckerManager(checkers);

    manager.register(new HealthChecker() {
      @Override
      public String getName() {
        return "test";
      }

      @Override
      public HealthCheckResult check() {
        return new HealthCheckResult(false, "bad", "bad call");
      }
    });

    Map<String, HealthCheckResult> results = manager.check();

    Assert.assertEquals(1, results.size());

    HealthCheckResult result = manager.check().get("test");
    Assert.assertEquals(false, result.isHealthy());
    Assert.assertEquals("bad", result.getInformation());
    Assert.assertEquals("bad call", result.getExtraData());

    result = manager.check("test");
    Assert.assertEquals(false, result.isHealthy());
    Assert.assertEquals("bad", result.getInformation());
    Assert.assertEquals("bad call", result.getExtraData());
  }
}
