/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;

import java.util.List;

/**
 * This class represents the key operations associated with persisting device related information.
 */
public interface DeviceDAO {

    int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

    int updateDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

    int removeDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    Device getDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    List<Device> getDevices(int tenantId) throws DeviceManagementDAOException;

    List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException;

    List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException;

    int getDeviceCount(int tenantId) throws DeviceManagementDAOException;

    List<Device> getDevicesByName(String deviceName, int tenantId) throws DeviceManagementDAOException;

    int addEnrollment(Device device, int tenantId) throws DeviceManagementDAOException;

    boolean setEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner, Status status,
                      int tenantId) throws DeviceManagementDAOException;

    Status getEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner,
                              int tenantId) throws DeviceManagementDAOException;

    EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, String currentUser,
                               int tenantId) throws DeviceManagementDAOException;


    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

    int getEnrolmentByStatus(DeviceIdentifier deviceId, Status status,
                                    int tenantId) throws DeviceManagementDAOException;
}

