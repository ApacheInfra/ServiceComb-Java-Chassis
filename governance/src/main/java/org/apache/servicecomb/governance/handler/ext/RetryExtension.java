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

package org.apache.servicecomb.governance.handler.ext;

import java.util.List;

public interface RetryExtension {
  boolean isRetry(List<String> statusList, Object result);

  Class<? extends Throwable>[] retryExceptions();

  default Boolean isContain(List<String> ststusList, String responseStatus) {
    return ststusList.stream().anyMatch(status -> judge(status, responseStatus));
  }

  default Boolean judge(String status, String responseStatus) {
    char[] statusChar = status.toCharArray();
    char[] responseChar = responseStatus.toCharArray();
    for(int i=0;i<statusChar.length-1;i++) {
      if(statusChar[i]!=responseChar[i] && statusChar[i]!='x') {
        return false;
      }
    }
    return true;
  }
}
