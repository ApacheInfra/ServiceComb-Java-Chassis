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
package org.apache.servicecomb.foundation.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class RequestRootSerializer {
  private RootSerializer rootSerializer;

  private boolean noTypesInfo;

  private boolean isWrap;

  public RequestRootSerializer(RootSerializer serializer, boolean isWrapp, boolean noTypesInfo) {
    this.rootSerializer = serializer;
    this.noTypesInfo = noTypesInfo;
    this.isWrap = isWrapp;
  }

  @SuppressWarnings("unchecked")
  public byte[] serialize(Object value) throws IOException {
    if (noTypesInfo && !isWrap) {
      return this.rootSerializer.serialize(((Map<String, Object>) value).values().iterator().next());
    } else {
      return this.rootSerializer.serialize(value);
    }
  }

  @SuppressWarnings("unchecked")
  public void serialize(OutputStream outputStream, Object value) throws IOException {
    if (noTypesInfo && !isWrap) {
      this.rootSerializer.serialize(outputStream, ((Map<String, Object>) value).values().iterator().next());
    } else {
      this.rootSerializer.serialize(outputStream, value);
    }
  }
}
