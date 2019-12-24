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

package org.apache.servicecomb.codec.protobuf.definition;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.codec.protobuf.utils.ScopedProtobufSchemaManager;
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;
import org.apache.servicecomb.core.definition.OperationMeta;

public class OperationProtobuf {
  private ScopedProtobufSchemaManager scopedProtobufSchemaManager;

  private OperationMeta operationMeta;

  private WrapSchema requestSchema;

  private WrapSchema responseSchema;

  public OperationProtobuf(ScopedProtobufSchemaManager scopedProtobufSchemaManager, OperationMeta operationMeta) {
    this.scopedProtobufSchemaManager = scopedProtobufSchemaManager;
    this.operationMeta = operationMeta;

    requestSchema = scopedProtobufSchemaManager.getOrCreateArgsSchema(operationMeta);

    responseSchema = scopedProtobufSchemaManager.getOrCreateSchema(operationMeta.getResponsesMeta().findResponseType(
        Status.OK.getStatusCode()));
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public WrapSchema getRequestSchema() {
    // TODO : work with request
    return requestSchema;
  }

  public WrapSchema getResponseSchema() {
    // TODO : work with response
    return responseSchema;
  }

  public WrapSchema findResponseSchema(int statusCode) {
    if (Family.SUCCESSFUL.equals(Family.familyOf(statusCode))) {
      return responseSchema;
    }

    return scopedProtobufSchemaManager.getOrCreateSchema(operationMeta.getResponsesMeta().findResponseType(statusCode));
  }
}
