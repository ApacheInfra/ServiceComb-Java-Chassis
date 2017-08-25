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

package io.servicecomb.serviceregistry.api.response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRegisterInstanceResponse {

  RegisterInstanceResponse oRegisterInstanceResponse = null;

  @Before
  public void setUp() throws Exception {
    oRegisterInstanceResponse = new RegisterInstanceResponse();
  }

  @After
  public void tearDown() throws Exception {
    oRegisterInstanceResponse = null;
  }

  @Test
  public void testDefaultValues() {
    Assert.assertNull(oRegisterInstanceResponse.getInstanceId());
  }

  @Test
  public void testIntializedValues() {
    initFields(); //Initialize the Object
    Assert.assertEquals("testInstanceID", oRegisterInstanceResponse.getInstanceId());
  }

  private void initFields() {
    oRegisterInstanceResponse.setInstanceId("testInstanceID");
  }
}
