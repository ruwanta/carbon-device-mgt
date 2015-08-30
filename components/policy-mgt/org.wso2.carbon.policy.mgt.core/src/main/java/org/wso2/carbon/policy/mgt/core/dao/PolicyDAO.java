/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.Criterion;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyCriterion;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;

import java.util.HashMap;
import java.util.List;

public interface PolicyDAO {

    Policy addPolicy(Policy policy) throws PolicyManagerDAOException;

    Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToDevice(List<Device> devices, Policy policy) throws PolicyManagerDAOException;

    boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagerDAOException;

    void activatePolicy(int policyId) throws PolicyManagerDAOException;

    void activatePolicies(List<Integer> policyIds) throws PolicyManagerDAOException;

    void markPoliciesAsUpdated(List<Integer> policyIds) throws PolicyManagerDAOException;

    void inactivatePolicy(int policyId) throws PolicyManagerDAOException;

    HashMap<Integer, Integer> getUpdatedPolicyIdandDeviceTypeId() throws PolicyManagerDAOException;

    Criterion addCriterion(Criterion criteria) throws PolicyManagerDAOException;

    Criterion updateCriterion(Criterion criteria) throws PolicyManagerDAOException;

    Criterion getCriterion(int id) throws PolicyManagerDAOException;

    Criterion getCriterion(String name) throws PolicyManagerDAOException;

    boolean checkCriterionExists(String name) throws PolicyManagerDAOException;

    boolean deleteCriterion(Criterion criteria) throws PolicyManagerDAOException;

    List<Criterion> getAllPolicyCriteria() throws PolicyManagerDAOException;

    Policy addPolicyCriteria(Policy policy) throws PolicyManagerDAOException;

    boolean addPolicyCriteriaProperties(List<PolicyCriterion> policyCriteria) throws PolicyManagerDAOException;

    List<PolicyCriterion> getPolicyCriteria(int policyId) throws PolicyManagerDAOException;

    Policy updatePolicy(Policy policy) throws PolicyManagerDAOException;

    void recordUpdatedPolicy(Policy policy) throws PolicyManagerDAOException;

    void recordUpdatedPolicies(List<Policy> policies) throws PolicyManagerDAOException;

    void removeRecordsAboutUpdatedPolicies() throws PolicyManagerDAOException;

    Policy getPolicy(int policyId) throws PolicyManagerDAOException;

    Policy getPolicyByProfileID(int profileId) throws PolicyManagerDAOException;

    List<Integer> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagerDAOException;

    List<Policy> getAllPolicies() throws PolicyManagerDAOException;

    List<Policy> getPolicyOfDeviceType(String deviceType) throws PolicyManagerDAOException;

    List<Integer> getPolicyIdsOfDevice(Device device) throws PolicyManagerDAOException;

    List<Integer> getPolicyOfRole(String roleName) throws PolicyManagerDAOException;

    List<Integer> getPolicyOfUser(String username) throws PolicyManagerDAOException;

    boolean deletePolicy(Policy policy) throws PolicyManagerDAOException;

    boolean deletePolicy(int policyId) throws PolicyManagerDAOException;

    boolean deleteAllPolicyRelatedConfigs(int policyId) throws PolicyManagerDAOException;

    List<String> getPolicyAppliedRoles(int policyId) throws PolicyManagerDAOException;

    List<String> getPolicyAppliedUsers(int policyId) throws PolicyManagerDAOException;

    void addEffectivePolicyToDevice(int deviceId, Policy policy)
            throws PolicyManagerDAOException;

    void setPolicyApplied(int deviceId) throws PolicyManagerDAOException;

    void updateEffectivePolicyToDevice(int deviceId, Policy policy)
            throws PolicyManagerDAOException;

    boolean checkPolicyAvailable(int deviceId) throws PolicyManagerDAOException;

    int getPolicyCount() throws PolicyManagerDAOException;

    int getAppliedPolicyId(int deviceId) throws PolicyManagerDAOException;

    Policy getAppliedPolicy(int deviceId) throws PolicyManagerDAOException;

    HashMap<Integer, Integer> getAppliedPolicyIds(List<Integer> deviceIds) throws PolicyManagerDAOException;

    HashMap<Integer, Integer> getAppliedPolicyIdsDeviceIds() throws PolicyManagerDAOException;
 }
