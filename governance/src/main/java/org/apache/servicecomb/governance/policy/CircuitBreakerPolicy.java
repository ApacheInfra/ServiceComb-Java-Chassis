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
package org.apache.servicecomb.governance.policy;

import org.springframework.util.StringUtils;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;

public class CircuitBreakerPolicy extends AbstractPolicy {

  public static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50;

  public static final int DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100;

  public static final int DEFAULT_WAIT_DURATION_IN_OPEN_STATUS = 60000;

  //ms
  public static final int DEFAULT_SLOW_CALL_DURATION_THRESHOLD = 60000;

  // the number of permitted calls when the CircuitBreaker is half open.
  public static final int DEFAULT_PERMITTED = 10;

  public static final int DEFAULT_MINIMUM_NUMBER_CALLS = 100;

  public static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;

  private int failureRateThreshold = DEFAULT_FAILURE_RATE_THRESHOLD;

  private int slowCallRateThreshold = DEFAULT_SLOW_CALL_RATE_THRESHOLD;

  private String waitDurationInOpenState;

  private String slowCallDurationThreshold;

  private int permittedNumberOfCallsInHalfOpenState = DEFAULT_PERMITTED;

  private int minimumNumberOfCalls = DEFAULT_MINIMUM_NUMBER_CALLS;

  private String slidingWindowType;

  private int slidingWindowSize = DEFAULT_SLIDING_WINDOW_SIZE;

  public CircuitBreakerPolicy() {
  }

  @Override
  public boolean isValid() {
    if (failureRateThreshold > 100 || failureRateThreshold <= 0) {
      return false;
    }
    if (slowCallRateThreshold > 100 || slowCallRateThreshold <= 0) {
      return false;
    }
    if (Integer.valueOf(getWaitDurationInOpenState()) <= 0) {
      return false;
    }
    if (Integer.valueOf(getSlowCallDurationThreshold()) <= 0) {
      return false;
    }
    if (permittedNumberOfCallsInHalfOpenState <= 0) {
      return false;
    }
    if (minimumNumberOfCalls <= 0) {
      return false;
    }

    return super.isValid();
  }

  public int getFailureRateThreshold() {
    return failureRateThreshold;
  }

  public void setFailureRateThreshold(int failureRateThreshold) {
    this.failureRateThreshold = failureRateThreshold;
  }

  public int getSlowCallRateThreshold() {
    return slowCallRateThreshold;
  }

  public void setSlowCallRateThreshold(int slowCallRateThreshold) {
    this.slowCallRateThreshold = slowCallRateThreshold;
  }

  public String getWaitDurationInOpenState() {
    return getTimeDuration(waitDurationInOpenState, DEFAULT_WAIT_DURATION_IN_OPEN_STATUS);
  }

  public void setWaitDurationInOpenState(String waitDurationInOpenState) {
    this.waitDurationInOpenState = waitDurationInOpenState;
  }

  public String getSlowCallDurationThreshold() {
    return getTimeDuration(slowCallDurationThreshold, DEFAULT_SLOW_CALL_DURATION_THRESHOLD);
  }

  public void setSlowCallDurationThreshold(String slowCallDurationThreshold) {
    this.slowCallDurationThreshold = slowCallDurationThreshold;
  }

  public int getPermittedNumberOfCallsInHalfOpenState() {
    return permittedNumberOfCallsInHalfOpenState;
  }

  public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
    this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
  }

  public int getMinimumNumberOfCalls() {
    return minimumNumberOfCalls;
  }

  public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }

  public SlidingWindowType getSlidingWindowTypeEnum() {
    if (StringUtils.isEmpty(slidingWindowType)) {
      slidingWindowType = "count";
    }
    switch (slidingWindowType) {
      case "time":
        return SlidingWindowType.TIME_BASED;
      case "count":
      default:
        return SlidingWindowType.COUNT_BASED;
    }
  }

  public String getSlidingWindowType() {
    return this.slidingWindowType;
  }

  public void setSlidingWindowType(String slidingWindowType) {
    this.slidingWindowType = slidingWindowType;
  }

  // time's unit is second
  public int getSlidingWindowSize() {
    return slidingWindowSize;
  }

  public void setSlidingWindowSize(int slidingWindowSize) {
    this.slidingWindowSize = slidingWindowSize;
  }

  @Override
  public String toString() {
    return "CircuitBreakerPolicy{" +
        "failureRateThreshold=" + failureRateThreshold +
        ", slowCallRateThreshold=" + slowCallRateThreshold +
        ", waitDurationInOpenState=" + waitDurationInOpenState +
        ", slowCallDurationThreshold=" + slowCallDurationThreshold +
        ", permittedNumberOfCallsInHalfOpenState=" + permittedNumberOfCallsInHalfOpenState +
        ", minimumNumberOfCalls=" + minimumNumberOfCalls +
        ", slidingWindowType='" + slidingWindowType + '\'' +
        ", slidingWindowSize=" + slidingWindowSize +
        '}';
  }
}
