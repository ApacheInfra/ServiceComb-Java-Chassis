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
package org.apache.servicecomb.governance.handler;

import java.time.Duration;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CircuitBreakerHandler extends AbstractGovernanceHandler<CircuitBreaker, CircuitBreakerPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerHandler.class);

  @Autowired
  private CircuitBreakerProperties circuitBreakerProperties;

  @Override
  protected String createKey(CircuitBreakerPolicy policy) {
    return "servicecomb.circuitBreaker." + policy.getName();
  }

  @Override
  public CircuitBreakerPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, circuitBreakerProperties.getParsedEntity());
  }

  @Override
  protected CircuitBreaker createProcessor(CircuitBreakerPolicy policy) {
    return getCircuitBreaker(policy);
  }

  private CircuitBreaker getCircuitBreaker(CircuitBreakerPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        //熔断 失败率(请求)百分比阈值
        .failureRateThreshold(policy.getFailureRateThreshold())
        //熔断 慢请求百分比阈值
        .slowCallRateThreshold(policy.getSlowCallRateThreshold())
        //从开过渡到半开的等待时间
        .waitDurationInOpenState(Duration.ofMillis(Integer.valueOf(policy.getWaitDurationInOpenState())))
        //请求时间定义
        .slowCallDurationThreshold(Duration.ofMillis(Integer.valueOf(policy.getSlowCallDurationThreshold())))
        //进入半开状态时 允许的请求数量
        .permittedNumberOfCallsInHalfOpenState(policy.getPermittedNumberOfCallsInHalfOpenState())
        //可以达到熔断条件的请求数量下限
        .minimumNumberOfCalls(policy.getMinimumNumberOfCalls())
        //可以选择基于时间的滑动窗口计数或者基于请求数量的滑动窗口计数
        .slidingWindowType(policy.getSlidingWindowTypeEnum())
        //滑动窗口，单位可能是请求数或者秒
        .slidingWindowSize(policy.getSlidingWindowSize())
        .build();
    CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    return circuitBreakerRegistry.circuitBreaker(policy.getName(), circuitBreakerConfig);
  }
}
