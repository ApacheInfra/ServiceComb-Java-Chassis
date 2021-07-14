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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.entity.Configurable;

public abstract class AbstractPolicy extends Configurable {
  @Override
  public boolean isValid() {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    return true;
  }

  public String getTimeDuration(String time, int defaultValue) {
    return String.valueOf(defaultMethod(time, defaultValue));
  }

  public int defaultMethod(String time, int defaultValue) {
    if (StringUtils.isEmpty(time)) {
      return defaultValue;
    }
    if (time.endsWith("ms")) {
      return Integer.valueOf(time.substring(0, time.length() - 2));
    }
    if (time.endsWith("s")) {
      return Integer.valueOf(time.substring(0, time.length() - 1)) * 1000;
    }
    if (time.endsWith("m")) {
      return Integer.valueOf(time.substring(0, time.length() - 1)) * 60 * 1000;
    }
    if (time.endsWith("h")) {
      return Integer.valueOf(time.substring(0, time.length() - 1)) * 60 * 60 * 1000;
    }
    return Integer.valueOf(time) * 1000;
  }
}
