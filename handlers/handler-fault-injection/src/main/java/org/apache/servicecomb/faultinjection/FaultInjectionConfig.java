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

package org.apache.servicecomb.faultinjection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

/**
 * Handles the fault injection configuration read from micro service file/config
 * center.
 */
public final class FaultInjectionConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(FaultInjectionConfig.class);

  // key is configuration parameter.
  public static List<String> cfgCallback = new LinkedList<>();

  public int getConfigVal(String config, int defaultValue) {
    DynamicIntProperty dynamicIntProperty = DynamicPropertyFactory.getInstance().getIntProperty(config,
        defaultValue);
    if (!cfgCallback.contains(config)) {
      cfgCallback.add(config);
      dynamicIntProperty.addCallback(() -> {
        int newValue = dynamicIntProperty.get();
        String cfgName = dynamicIntProperty.getName();

        //store the value in config center map and check for next requests.
        FaultInjectionUtil.setConfigCenterValue(cfgName, new AtomicInteger(newValue));
        LOGGER.info("{} changed to {}", cfgName, newValue);
      });
    }

    return dynamicIntProperty.get();
  }
}
