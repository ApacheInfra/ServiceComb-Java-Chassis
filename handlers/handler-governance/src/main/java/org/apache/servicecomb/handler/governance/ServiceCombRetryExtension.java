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

package org.apache.servicecomb.handler.governance;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.governance.handler.ext.RetryExtension;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

import io.vertx.core.VertxException;

@Component
public class ServiceCombRetryExtension implements RetryExtension {
  @Override
  public boolean isRetry(List<String> statusList, Object result) {
    if (!(result instanceof Response)) {
      return false;
    }
    Response resp = (Response) result;
    if (!resp.isFailed()) {
      return false;
    }
    if (InvocationException.class.isInstance(resp.getResult())) {
      InvocationException e = resp.getResult();
      return isContain(statusList, String.valueOf(e.getStatusCode()));
    }
    return true;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class<? extends Throwable>[] retryExceptions() {
    return new Class[]{
        ConnectException.class,
        SocketTimeoutException.class,
        IOException.class,
        VertxException.class,
        NoRouteToHostException.class,
        InvocationException.class};
  }
}
