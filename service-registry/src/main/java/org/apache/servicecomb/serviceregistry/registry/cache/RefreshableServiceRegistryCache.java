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

package org.apache.servicecomb.serviceregistry.registry.cache;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.apache.servicecomb.serviceregistry.task.event.SafeModeChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache the pulled microservice instances.
 */
public class RefreshableServiceRegistryCache implements ServiceRegistryCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshableServiceRegistryCache.class);

  Map<MicroserviceCacheKey, RefreshableMicroserviceCache> microserviceCache = new ConcurrentHashMapEx<>();

  Microservice consumerService;

  ServiceRegistryClient srClient;

  boolean emptyInstanceProtectionEnabled = false;

  Consumer<List<MicroserviceCache>> cacheRefreshedWatcher;

  ReentrantLock refreshLock = new ReentrantLock();

  public RefreshableServiceRegistryCache(Microservice consumerService, ServiceRegistryClient srClient) {
    this.consumerService = consumerService;
    this.srClient = srClient;
  }

  public void refreshCache() {
    if (!refreshLock.tryLock()) {
      LOGGER.info("ignore concurrent refresh request");
      return;
    }

    try {
      List<MicroserviceCache> refreshedCaches = refreshInnerState(false);
      notifyWatcher(refreshedCaches);
    } catch (Exception e) {
      LOGGER.error("failed to refresh caches", e);
    } finally {
      refreshLock.unlock();
    }
  }

  public void forceRefreshCache() {
    refreshLock.lock();
    try {
      List<MicroserviceCache> refreshedCaches = refreshInnerState(true);
      notifyWatcher(refreshedCaches);
    } catch (Exception e) {
      LOGGER.error("failed to refresh caches", e);
    } finally {
      refreshLock.unlock();
    }
  }

  private List<MicroserviceCache> refreshInnerState(boolean isForced) {
    return microserviceCache.values().stream()
        .peek(cache -> {
          if (isForced) {
            cache.forceRefresh();
          } else {
            cache.refresh();
          }
        })
        .filter(this::isRefreshedMicroserviceCache)
        .peek(this::removeCacheIfServiceNotFound)
        .collect(Collectors.toList());
  }

  private boolean isRefreshedMicroserviceCache(MicroserviceCache microserviceCache) {
    return MicroserviceCacheStatus.REFRESHED.equals(microserviceCache.getStatus())
        || MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(microserviceCache.getStatus());
  }

  private void notifyWatcher(List<MicroserviceCache> refreshedCaches) {
    if (refreshedCaches.isEmpty() || null == cacheRefreshedWatcher) {
      return;
    }
    cacheRefreshedWatcher.accept(refreshedCaches);
  }

  @Override
  public MicroserviceCache findServiceCache(MicroserviceCacheKey microserviceCacheKey) {
    microserviceCacheKey.validate();
    RefreshableMicroserviceCache targetCache = microserviceCache
        .computeIfAbsent(microserviceCacheKey, pk -> {
          RefreshableMicroserviceCache microserviceCache = createMicroserviceCache(microserviceCacheKey);
          microserviceCache.refresh();
          return microserviceCache;
        });
    removeCacheIfServiceNotFound(targetCache);
    return targetCache;
  }

  private void removeCacheIfServiceNotFound(MicroserviceCache targetCache) {
    if (MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(targetCache.getStatus())) {
      microserviceCache.remove(targetCache.getKey());
      LOGGER.info("microserviceCache[{}] got removed", targetCache.getKey());
    }
  }

  RefreshableMicroserviceCache createMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
    return new RefreshableMicroserviceCache(
        consumerService,
        microserviceCacheKey,
        srClient,
        emptyInstanceProtectionEnabled);
  }

  public RefreshableServiceRegistryCache setEmptyInstanceProtectionEnabled(boolean emptyInstanceProtectionEnabled) {
    this.emptyInstanceProtectionEnabled = emptyInstanceProtectionEnabled;
    return this;
  }

  @Override
  public ServiceRegistryCache setCacheRefreshedWatcher(
      Consumer<List<MicroserviceCache>> cacheRefreshedWatcher) {
    this.cacheRefreshedWatcher = cacheRefreshedWatcher;
    return this;
  }

  public void clearRevisionId(MicroserviceCacheKey key) {
    RefreshableMicroserviceCache microserviceCache = this.microserviceCache.get(key);
    if (null != microserviceCache) {
      microserviceCache.clearRevisionId();
    }
  }

  public void clearAllRevisionId() {
    for (Entry<MicroserviceCacheKey, RefreshableMicroserviceCache> cacheEntry : microserviceCache.entrySet()) {
      cacheEntry.getValue().clearRevisionId();
    }
  }

  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent event) {
    List<MicroserviceCache> refreshedCaches =
        microserviceCache.entrySet().stream()
            .peek(cacheEntry -> cacheEntry.getValue().onMicroserviceInstanceChanged(event))
            .filter(cacheEntry -> isRefreshedMicroserviceCache(cacheEntry.getValue()))
            .map(Entry::getValue)
            .collect(Collectors.toList());

    notifyWatcher(refreshedCaches);
  }

  public void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    for (Entry<MicroserviceCacheKey, RefreshableMicroserviceCache> cacheEntry : microserviceCache.entrySet()) {
      cacheEntry.getValue().onSafeModeChanged(modeChangeEvent);
    }
  }
}
