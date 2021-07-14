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
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

@Component
public class BulkheadHandler extends AbstractGovernanceHandler<Bulkhead, BulkheadPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BulkheadHandler.class);

  @Autowired
  private BulkheadProperties bulkheadProperties;

  @Override
  protected String createKey(BulkheadPolicy policy) {
    return "servicecomb.bulkhead." + policy.getName();
  }

  @Override
  public BulkheadPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, bulkheadProperties.getParsedEntity());
  }

  @Override
  protected Bulkhead createProcessor(BulkheadPolicy policy) {
    return getBulkhead(policy);
  }

  private Bulkhead getBulkhead(BulkheadPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(policy.getMaxConcurrentCalls())
        .maxWaitDuration(Duration.ofMillis(Integer.valueOf(policy.getMaxWaitDuration())))
        .build();

    BulkheadRegistry registry = BulkheadRegistry.of(config);

    return registry.bulkhead(policy.getName());
  }
}
