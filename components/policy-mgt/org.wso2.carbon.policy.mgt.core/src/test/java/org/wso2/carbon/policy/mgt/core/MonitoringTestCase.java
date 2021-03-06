/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementAdminService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.MonitoringManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.services.PolicyMonitoringServiceTest;
import org.wso2.carbon.policy.mgt.core.task.MonitoringTask;

import java.util.List;

public class MonitoringTestCase extends BasePolicyManagementDAOTest {

    private static final Log log = LogFactory.getLog(MonitoringTestCase.class);

    private static final String ANDROID = "android";

    DeviceIdentifier identifier = new DeviceIdentifier();

    @BeforeClass
    @Override
    public void init() throws Exception {
    }

    @Test
    public void testMonitorDao() throws PolicyManagementException, DeviceManagementException {

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();

        List<Policy> policies = policyManagerService.getPolicies(ANDROID);
        List<Device> devices = service.getAllDevices(ANDROID);

        for (Policy policy : policies) {
            log.debug(policy.getPolicyName() + "-----P");
        }

        for (Device device : devices) {
            log.debug(device.getDeviceIdentifier() + " ----- D");
        }


        identifier.setType(ANDROID);
        identifier.setId(devices.get(0).getDeviceIdentifier());

        PolicyAdministratorPoint administratorPoint = new PolicyAdministratorPointImpl();

        administratorPoint.setPolicyUsed(identifier, policies.get(0));

    }

    @Test(dependsOnMethods = ("testMonitorDao"))
    public void getDeviceAppliedPolicy() throws PolicyManagementException {

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = manager.getAppliedPolicyToDevice(identifier);

        if (policy != null) {

            log.debug(policy.getId());
            log.debug(policy.getPolicyName());
            log.debug(policy.getCompliance());
        } else {
            log.debug("Applied policy was a null object.");
        }
    }


    @Test(dependsOnMethods = ("getDeviceAppliedPolicy"))
    public void addComplianceOperation() throws PolicyManagementException, DeviceManagementException,
            PolicyComplianceException {

        log.debug("Compliance operations adding started.");

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = manager.getAppliedPolicyToDevice(identifier);
        OperationManager operationManager = new OperationManagerImpl();

        DeviceManagementDataHolder.getInstance().setOperationManager(operationManager);

        log.debug(policy.getId());
        log.debug(policy.getPolicyName());
        log.debug(policy.getCompliance());

        MonitoringManager monitoringManager = new MonitoringManagerImpl();

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        List<Device> devices = service.getAllDevices(ANDROID);

        monitoringManager.addMonitoringOperation(devices);

        log.debug("Compliance operations adding done.");

    }


    @Test(dependsOnMethods = ("addComplianceOperation"))
    public void checkComplianceFromMonitoringService() throws PolicyManagementException, DeviceManagementException,
            PolicyComplianceException {



        PolicyMonitoringServiceTest monitoringServiceTest = new PolicyMonitoringServiceTest();
        PolicyManagementDataHolder.getInstance().setPolicyMonitoringService(monitoringServiceTest.getType(),
                monitoringServiceTest);

        DeviceManagementProviderService adminService = new DeviceManagementProviderServiceImpl();

       // PolicyManager policyManagerService = new PolicyManagerImpl();

        List<Device> devices = adminService.getAllDevices();

        for (Device device : devices) {
            log.debug(device.getDeviceIdentifier());
            log.debug(device.getType());
            log.debug(device.getName());
        }

        monitoringServiceTest.notifyDevices(devices);

        PolicyManager manager = new PolicyManagerImpl();
        Policy policy = manager.getAppliedPolicyToDevice(identifier);

        Object ob = new Object();

        monitoringServiceTest.checkPolicyCompliance(identifier, policy, ob);
    }


    @Test(dependsOnMethods = ("checkComplianceFromMonitoringService"))
    public void checkCompliance() throws DeviceManagementException, PolicyComplianceException, PolicyManagementException {

        PolicyMonitoringServiceTest monitoringServiceTest = new PolicyMonitoringServiceTest();
        PolicyManagementDataHolder.getInstance().setPolicyMonitoringService(monitoringServiceTest.getType(),
                monitoringServiceTest);
        DeviceManagementProviderService adminService = new DeviceManagementProviderServiceImpl();


        List<Device> devices = adminService.getAllDevices();


        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(devices.get(0).getDeviceIdentifier());
        deviceIdentifier.setType(devices.get(0).getType());

        Object ob = new Object();

        MonitoringManager monitoringManager = new MonitoringManagerImpl();

        log.debug(identifier.getId());
        log.debug(identifier.getType());

        monitoringManager.checkPolicyCompliance(identifier, ob);

    }
}
