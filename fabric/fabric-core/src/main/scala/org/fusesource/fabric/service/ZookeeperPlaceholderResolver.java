/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
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
package org.fusesource.fabric.service;

import org.apache.zookeeper.KeeperException;
import org.fusesource.fabric.api.FabricException;
import org.fusesource.fabric.api.PlaceholderResolver;
import org.fusesource.fabric.zookeeper.IZKClient;
import org.fusesource.fabric.zookeeper.utils.ZookeeperCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperPlaceholderResolver implements PlaceholderResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperPlaceholderResolver.class);
    private static final String ZOOKEEPER_SCHEME = "zk";

    private IZKClient zooKeeper;

    @Override
    public String getScheme() {
        return ZOOKEEPER_SCHEME;
    }

    @Override
    public String resolve(String pid, String key, String value) {
        try {
            return new String(ZookeeperCommandBuilder.loadUrl(value).execute(zooKeeper), "UTF-8");
        } catch (KeeperException.NoNodeException e) {
            LOGGER.debug("Could not load property value: {}. Ignoring.", value, e);
            return value;
        } catch (Exception e) {
            throw new FabricException(e);
        }
    }

    public IZKClient getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(IZKClient zooKeeper) {
        this.zooKeeper = zooKeeper;
    }
}
