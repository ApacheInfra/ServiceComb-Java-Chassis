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

package io.servicecomb.loadbalance.exception;

import io.servicecomb.core.exception.CseException;
import io.servicecomb.core.exception.ExceptionUtils;

public class LoadbalanceExceptionUtils extends ExceptionUtils {

  public static final String CSE_HANDLER_LB_WRONG_RULE = "cse.handler.lb.wrong.rule";

  static {
    ERROR_DESC_MGR.register(CSE_HANDLER_LB_WRONG_RULE, "Configured rule name is wrong.");
  }

  public static CseException createLoadbalanceException(String code, Throwable cause, Object... args) {
    String msg = String.format(ERROR_DESC_MGR.ensureFindValue(code), args);
    CseException exception = new CseException(code, msg, cause);
    return exception;
  }
}
