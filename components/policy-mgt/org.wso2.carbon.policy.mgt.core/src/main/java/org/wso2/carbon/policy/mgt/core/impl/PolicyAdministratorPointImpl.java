/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.FeatureManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.ProfileManagerImpl;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyAdministratorPointImpl implements PolicyAdministratorPoint {

    private static final Log log = LogFactory.getLog(PolicyAdministratorPointImpl.class);

    private PolicyManager policyManager;
    private ProfileManager profileManager;
    private FeatureManager featureManager;
    // private PolicyEnforcementDelegator delegator;

    public PolicyAdministratorPointImpl() {
        this.policyManager = new PolicyManagerImpl();
        this.profileManager = new ProfileManagerImpl();
        this.featureManager = new FeatureManagerImpl();
        // this.delegator = new PolicyEnforcementDelegatorImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        Policy resultantPolicy = policyManager.addPolicy(policy);
//        try {
//            delegator.delegate(resultantPolicy, resultantPolicy.getDevices());
//        } catch (PolicyDelegationException e) {
//            throw new PolicyManagementException("Error occurred while delegating policy operation to the devices", e);
//        }
        return resultantPolicy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        Policy resultantPolicy = policyManager.updatePolicy(policy);
//        try {
//            delegator.delegate(resultantPolicy, resultantPolicy.getDevices());
//        } catch (PolicyDelegationException e) {
//            throw new PolicyManagementException("Error occurred while delegating policy operation to the devices", e);
//        }
        return resultantPolicy;
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException {
        return policyManager.updatePolicyPriorities(policies);
    }

    @Override
    public void activatePolicy(int policyId) throws PolicyManagementException {
        policyManager.activatePolicy(policyId);
    }

    @Override
    public void inactivatePolicy(int policyId) throws PolicyManagementException {
        policyManager.inactivatePolicy(policyId);
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        return policyManager.deletePolicy(policy);
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        return policyManager.deletePolicy(policyId);
    }

    @Override
    public void publishChanges() throws PolicyManagementException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(PolicyManagementConstants.DELEGATION_TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Policy delegations task is started for the tenant id " + tenantId);
            }

            TaskManager taskManager = taskService.getTaskManager(PolicyManagementConstants.DELEGATION_TASK_TYPE);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();

            triggerInfo.setIntervalMillis(0);
            triggerInfo.setRepeatCount(1);

            Map<String, String> properties = new HashMap<>();
            properties.put(PolicyManagementConstants.TENANT_ID, String.valueOf(tenantId));

            String taskName = PolicyManagementConstants.DELEGATION_TASK_NAME + "_" + String.valueOf(tenantId);

            TaskInfo taskInfo = new TaskInfo(taskName, PolicyManagementConstants.DELEGATION_TASK_CLAZZ, properties, triggerInfo);

            taskManager.registerTask(taskInfo);
            taskManager.rescheduleTask(taskInfo.getName());


        } catch (TaskException e) {
            String msg = "Error occurred while creating the policy delegation task for tenant " + PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getTenantId();
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

//        List<DeviceType> deviceTypes = policyManager.applyChangesMadeToPolicies();
//
//        if(log.isDebugEnabled()) {
//            log.debug("Number of device types which policies are changed .......... : " + deviceTypes.size() );
//        }
//
//        if (!deviceTypes.isEmpty()) {
//
//
//            DeviceManagementProviderService service = PolicyManagementDataHolder.getInstance()
//                    .getDeviceManagementService();
//            List<Device> devices = new ArrayList<>();
//            for (DeviceType deviceType : deviceTypes) {
//                try {
//                    devices.addAll(service.getAllDevices(deviceType.getName()));
//                } catch (DeviceManagementException e) {
//                    throw new PolicyManagementException("Error occurred while taking the devices", e);
//                }
//            }
//            HashMap<Integer, Integer> deviceIdPolicy = policyManager.getAppliedPolicyIdsDeviceIds();
//            List<Device> toBeNotified = new ArrayList<>();
//
//            for (Device device : devices) {
//                if (deviceIdPolicy.containsKey(device.getId())) {
//                    toBeNotified.add(device);
//                }
//            }
//            if (!toBeNotified.isEmpty()) {
//
//              //  ExecutorService executorService = getExecutor();
//              //  PolicyEnforcementDelegator enforcementDelegator = new PolicyEnforcementDelegatorImpl(toBeNotified);
////                Thread thread = new Thread(new PolicyEnforcementDelegatorImpl(toBeNotified));
////                thread.start();
//            }
//        }
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws
            PolicyManagementException {
        return policyManager.addPolicyToDevice(deviceIdentifierList, policy);
    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException {
        return policyManager.addPolicyToRole(roleNames, policy);
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {
        return policyManager.getPolicies();
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {
        return policyManager.getPolicy(policyId);
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.getPoliciesOfDevice(deviceIdentifier);
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceType) throws PolicyManagementException {
        return policyManager.getPoliciesOfDeviceType(deviceType);
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {
        return policyManager.getPoliciesOfRole(roleName);
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {
        return policyManager.getPoliciesOfUser(username);
    }

    @Override
    public boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.checkPolicyAvailable(deviceIdentifier);
    }

    @Override
    public boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.setPolicyApplied(deviceIdentifier);
    }

    @Override
    public void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException {
        policyManager.addAppliedPolicyToDevice(deviceIdentifier, policy);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.addProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public boolean deleteProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.deleteProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while deleting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.updateProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile getProfile(int profileId) throws PolicyManagementException {
        try {
            return profileManager.getProfile(profileId);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while retrieving profile";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Profile> getProfiles() throws PolicyManagementException {
        try {
            return profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while obtaining list of profiles.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

 /*   @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        return featureManager.addFeature(feature);
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        return featureManager.updateFeature(feature);

    }*/

    @Override
    public boolean deleteFeature(int featureId) throws FeatureManagementException {
        return featureManager.deleteFeature(featureId);
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        return policyManager.getPolicyCount();
    }

}
