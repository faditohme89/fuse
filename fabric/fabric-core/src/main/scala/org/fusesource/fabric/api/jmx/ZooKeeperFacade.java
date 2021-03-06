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
package org.fusesource.fabric.api.jmx;

import java.util.List;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.fusesource.fabric.service.FabricServiceImpl;
import org.fusesource.fabric.zookeeper.IZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fusesource.fabric.internal.Objects.notNull;

/**
 */
public class ZooKeeperFacade implements ZooKeeperFacadeMXBean {
    private static final transient Logger LOG = LoggerFactory.getLogger(ZooKeeperFacade.class);

    private final FabricServiceImpl fabricService;
    private ObjectName objectName;

    public ZooKeeperFacade(FabricServiceImpl fabricService) {
        this.fabricService = fabricService;
    }

    public ObjectName getObjectName() throws MalformedObjectNameException {
        if (objectName == null) {
            // TODO to avoid mbean clashes if ever a JVM had multiple FabricService instances, we may
            // want to add a parameter of the fabric ID here...
            objectName = new ObjectName("org.fusesource.fabric:type=ZooKeeper");
        }
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public void registerMBeanServer(MBeanServer mbeanServer) {
        try {
            ObjectName name = getObjectName();
			if (!mbeanServer.isRegistered(name)) {
				mbeanServer.registerMBean(this, name);
			}
		} catch (Exception e) {
            LOG.warn("An error occured during mbean server registration: " + e, e);
        }
    }

    public void unregisterMBeanServer(MBeanServer mbeanServer) {
        if (mbeanServer != null) {
            try {
                ObjectName name = getObjectName();
				if (mbeanServer.isRegistered(name)) {
					mbeanServer.unregisterMBean(name);
				}
			} catch (Exception e) {
                LOG.warn("An error occured during mbean server registration: " + e, e);
            }
        }
    }

    protected FabricServiceImpl getFabricService() {
        return fabricService;
    }

    /**
     * Returns the ZooKeeper client or throwns an exception if its not configured properly
     */
    protected IZKClient getZooKeeper() {
        IZKClient zooKeeper = fabricService.getZooKeeper();
        notNull(zooKeeper, "zooKeeper");
        return zooKeeper;
    }

    // Facade API
    //-------------------------------------------------------------------------


    public ZkContents read(String path) throws KeeperException, InterruptedException {
        IZKClient zk = getZooKeeper();
        Stat exists = zk.exists(path);
        if (exists == null) {
            return null;
        }
        int numChildren = exists.getNumChildren();
        int dataLength = exists.getDataLength();
        List<String> children = null;
        String data = null;
        if (numChildren > 0) {
            children = zk.getChildren(path);
        } else {
            data = zk.getStringData(path);
        }
        return new ZkContents(dataLength, children, data);
    }

    @Override
    public String getContents(String path) throws KeeperException, InterruptedException {
        byte[] data = getZooKeeper().getData(path);
        return new String(data);
    }
}
