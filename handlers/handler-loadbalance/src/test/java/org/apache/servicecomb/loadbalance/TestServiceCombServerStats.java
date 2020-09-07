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

package org.apache.servicecomb.loadbalance;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.testing.MockClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class TestServiceCombServerStats {
  @Before
  public void before() {
    releaseTryingChance();
  }

  @After
  public void after() {
    releaseTryingChance();
  }

  @Test
  public void testSimpleThread() {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    stats.markFailure();
    stats.markFailure();
    Assert.assertEquals(stats.getCountinuousFailureCount(), 2);
    stats.markSuccess();
    Assert.assertEquals(stats.getCountinuousFailureCount(), 0);
    stats.markSuccess();
    Assert.assertEquals(stats.getTotalRequests(), 4);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testMiltiThread() throws Exception {
    long time = System.currentTimeMillis();
    ServiceCombServerStats stats = new ServiceCombServerStats();
    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
          stats.markFailure();
          stats.markFailure();
          stats.markSuccess();
          stats.markSuccess();
          latch.countDown();
        }
      }.start();
    }
    latch.await(30, TimeUnit.SECONDS);
    Assert.assertEquals(stats.getTotalRequests(), 4 * 10);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    Assert.assertTrue(stats.getLastVisitTime() <= System.currentTimeMillis() && stats.getLastVisitTime() >= time);
    Assert.assertTrue(stats.getLastActiveTime() <= System.currentTimeMillis() && stats.getLastActiveTime() >= time);
  }

  @Test
  public void testTimeWindow() {
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return 1000;
      }
    };
    ServiceCombServerStats stats = new ServiceCombServerStats();
    Assert.assertEquals(stats.getLastVisitTime(), 1000);
    stats.markSuccess();
    stats.markFailure();
    Assert.assertEquals(stats.getTotalRequests(), 2);
    Assert.assertEquals(stats.getFailedRate(), 50);
    Assert.assertEquals(stats.getSuccessRate(), 50);
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return 60000 + 2000;
      }
    };
    stats.markSuccess();
    Assert.assertEquals(stats.getTotalRequests(), 1);
    Assert.assertEquals(stats.getFailedRate(), 0);
    Assert.assertEquals(stats.getSuccessRate(), 100);
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_null_precondition() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_chance_occupied() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());

    Invocation otherInvocation = new Invocation();
    Assert.assertFalse(ServiceCombServerStats.applyForTryingChance(otherInvocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  @Test
  public void testGlobalAllowIsolatedServerTryingFlag_apply_with_flag_outdated() {
    Invocation invocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    Assert.assertSame(invocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
    ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().clock = new MockClock(new Holder<>(
        ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().startTryingTimestamp + 60000
    ));

    Invocation otherInvocation = new Invocation();
    Assert.assertTrue(ServiceCombServerStats.applyForTryingChance(otherInvocation));
    Assert
        .assertSame(otherInvocation, ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get().getInvocation());
  }

  public static void releaseTryingChance() {
    ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.set(null);
  }

  public static Invocation getTryingIsolatedServerInvocation() {
    return Optional.ofNullable(ServiceCombServerStats.globalAllowIsolatedServerTryingFlag.get())
        .map(TryingIsolatedServerMarker::getInvocation)
        .orElse(null);
  }
}
