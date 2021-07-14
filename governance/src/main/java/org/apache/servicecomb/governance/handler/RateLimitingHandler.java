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
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Component
public class RateLimitingHandler extends AbstractGovernanceHandler<RateLimiter, RateLimitingPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingHandler.class);

  @Autowired
  private RateLimitProperties rateLimitProperties;

  @Override
  protected String createKey(RateLimitingPolicy policy) {
    return "servicecomb.rateLimiting." + policy.getName();
  }

  @Override
  public RateLimitingPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, rateLimitProperties.getParsedEntity());
  }

  @Override
  protected RateLimiter createProcessor(RateLimitingPolicy policy) {
    return getRateLimiter(policy);
  }

  private RateLimiter getRateLimiter(RateLimitingPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    RateLimiterConfig config;
    config = RateLimiterConfig.custom()
        .limitForPeriod(policy.getRate())
        .limitRefreshPeriod(Duration.ofMillis(Integer.valueOf(policy.getLimitRefreshPeriod())))
        .timeoutDuration(Duration.ofMillis(Integer.valueOf(policy.getTimeoutDuration())))
        .build();
    RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
    return rateLimiterRegistry.rateLimiter(policy.getName());
  }
}
