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

package org.apache.servicecomb.localregistry;

import static org.apache.servicecomb.registry.definition.DefinitionConst.DEFAULT_APPLICATION_ID;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.base.ServiceCombConstants;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.yaml.snakeyaml.Yaml;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicPropertyFactory;

public class LocalRegistryStore {
  private static final String REGISTRY_FILE_NAME = "registry.yaml";

  public static final LocalRegistryStore INSTANCE = new LocalRegistryStore();

  private Microservice selfMicroservice;

  private MicroserviceInstance selfMicroserviceInstance;

  // key is microservice id
  private Map<String, Microservice> microserviceMap = new ConcurrentHashMap<>();

  // first key is microservice id
  // second key is instance id
  private Map<String, Map<String, MicroserviceInstance>> microserviceInstanceMap = new ConcurrentHashMap<>();

  public LocalRegistryStore() {

  }

  @VisibleForTesting
  public void initSelfWithMocked(Microservice microservice, MicroserviceInstance microserviceInstance) {
    this.selfMicroservice = microservice;
    this.selfMicroserviceInstance = microserviceInstance;
  }

  public void init() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    MicroserviceFactory microserviceFactory = new MicroserviceFactory();
    selfMicroservice = microserviceFactory.create(microserviceDefinition);
    selfMicroserviceInstance = selfMicroservice.getInstance();
    microserviceMap.clear();
    microserviceInstanceMap.clear();
  }

  public void run() {
    selfMicroservice.setServiceId("[local]-[" + selfMicroservice.getAppId()
        + "]-[" + selfMicroservice.getServiceName() + "]");
    selfMicroserviceInstance.setInstanceId(selfMicroservice.getServiceId());
    selfMicroserviceInstance.setServiceId(selfMicroservice.getServiceId());

    InputStream is = null;

    try {
      ClassLoader loader = JvmUtils.findClassLoader();
      Enumeration<URL> urls = loader.getResources(REGISTRY_FILE_NAME);
      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        is = url.openStream();
        if (is != null) {
          initFromData(is);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // nothing to do
        }
      }
    }

    addSelf();
  }

  private void addSelf() {
    microserviceMap.put(selfMicroservice.getServiceId(), selfMicroservice);
    Map<String, MicroserviceInstance> selfInstanceMap = new HashMap<>(1);
    selfInstanceMap.put(selfMicroserviceInstance.getInstanceId(), selfMicroserviceInstance);
    microserviceInstanceMap.put(selfMicroservice.getServiceId(), selfInstanceMap);
  }

  public Microservice getSelfMicroservice() {
    return selfMicroservice;
  }

  public MicroserviceInstance getSelfMicroserviceInstance() {
    return selfMicroserviceInstance;
  }

  private void initFromData(InputStream is) {
    Yaml yaml = new Yaml();
    @SuppressWarnings("unchecked")
    Map<String, Object> data = yaml.loadAs(is, Map.class);
    initFromData(data);
  }

  private void initFromData(Map<String, Object> data) {
    for (Entry<String, Object> entry : data.entrySet()) {
      String name = entry.getKey();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> serviceConfigs = (List<Map<String, Object>>) entry.getValue();
      for (Map<String, Object> serviceConfig : serviceConfigs) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> instancesConfig =
            (List<Map<String, Object>>) serviceConfig.get("instances");

        String appId = (String) serviceConfig.get("appid");
        String version = (String) serviceConfig.get("version");
        String serviceId = (String) serviceConfig.get("id");
        @SuppressWarnings("unchecked")
        List<String> schemas = (List<String>) serviceConfig.get("schemaIds");

        Microservice microservice = new Microservice();
        microservice.setAppId(validAppId(appId));
        microservice.setServiceName(name);
        microservice.setVersion(version);
        microservice.setServiceId(serviceId == null ? UUID.randomUUID().toString() : serviceId);
        microserviceMap.put(microservice.getServiceId(), microservice);
        if (schemas != null) {
          microservice.setSchemas(schemas);
        }

        addInstances(instancesConfig, microservice);
      }
    }
  }

  private String validAppId(String configAppId) {
    if (!StringUtils.isEmpty(configAppId)) {
      return configAppId;
    }
    if (DynamicPropertyFactory.getInstance()
        .getStringProperty(ServiceCombConstants.CONFIG_APPLICATION_ID_KEY, null).get() != null) {
      return DynamicPropertyFactory.getInstance()
          .getStringProperty(ServiceCombConstants.CONFIG_APPLICATION_ID_KEY, null).get();
    }
    return DEFAULT_APPLICATION_ID;
  }

  private void addInstances(List<Map<String, Object>> instancesConfig, Microservice microservice) {
    Map<String, MicroserviceInstance> instanceMap = new ConcurrentHashMap<>();
    microserviceInstanceMap.put(microservice.getServiceId(), instanceMap);

    if (instancesConfig == null) {
      return;
    }

    for (Map<String, Object> instanceConfig : instancesConfig) {
      @SuppressWarnings("unchecked")
      List<String> endpoints = (List<String>) instanceConfig.get("endpoints");

      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setInstanceId(UUID.randomUUID().toString());
      instance.setEndpoints(endpoints);
      instance.setServiceId(microservice.getServiceId());

      instanceMap.put(instance.getInstanceId(), instance);
    }
  }

  public Microservice getMicroservice(String microserviceId) {
    return microserviceMap.get(microserviceId);
  }

  public List<Microservice> getAllMicroservices() {
    return microserviceMap.values().stream().collect(Collectors.toList());
  }

  public String getSchema(String microserviceId, String schemaId) {
    Microservice microservice = microserviceMap.get(microserviceId);
    if (microservice == null) {
      return null;
    }
    return microserviceMap.get(microserviceId).getSchemaMap().get(schemaId);
  }

  public MicroserviceInstance findMicroserviceInstance(String serviceId, String instanceId) {
    Map<String, MicroserviceInstance> microserviceInstance = microserviceInstanceMap.get(serviceId);
    if (microserviceInstance == null) {
      return null;
    }
    return microserviceInstanceMap.get(serviceId).get(instanceId);
  }

  // local registry do not care about version and revision
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    FindInstancesResponse findInstancesResponse = new FindInstancesResponse();
    List<MicroserviceInstance> instances = new ArrayList<>();

    Collectors.toList();
    microserviceInstanceMap.values().forEach(
        allInstances -> allInstances.values().stream().filter(
            aInstance -> {
              Microservice service = microserviceMap.get(aInstance.getServiceId());
              return service.getAppId().equals(appId) && service.getServiceName().equals(serviceName);
            }
        ).forEach(item -> instances.add(item)));
    if (instances.isEmpty()) {
      microserviceInstances.setMicroserviceNotExist(true);
    } else {
      findInstancesResponse.setInstances(instances);
      microserviceInstances.setMicroserviceNotExist(false);
      microserviceInstances.setInstancesResponse(findInstancesResponse);
    }
    return microserviceInstances;
  }
}
