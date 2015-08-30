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

package org.wso2.carbon.policy.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.Criterion;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyCriterion;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.*;

public class PolicyDAOImpl implements PolicyDAO {

    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagerDAOException {
        return persistPolicy(policy);
    }

    @Override
    public Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_TYPE_POLICY (DEVICE_TYPE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, getDeviceTypeId(deviceType));
            stmt.setInt(2, policy.getId());
            stmt.executeQuery();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while adding the device type policy to database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;

    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_ROLE_POLICY (ROLE_NAME, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (String role : roleNames) {
                stmt.setString(1, role);
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while adding the role name with policy to database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_USER_POLICY (POLICY_ID, USERNAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (String username : usernameList) {
                stmt.setInt(1, policy.getId());
                stmt.setString(2, username);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while adding the user name with policy to database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToDevice(List<Device> devices, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_POLICY (DEVICE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (Device device : devices) {
                stmt.setInt(1, device.getId());
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while adding the device ids  with policy to " +
                    "database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  PRIORITY = ?, UPDATED = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);

            for (Policy policy : policies) {
                stmt.setInt(1, policy.getPriorityId());
                stmt.setInt(2, 1);
                stmt.setInt(3, policy.getId());
                stmt.setInt(4, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating policy priorities in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public void activatePolicy(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  UPDATED = ?, ACTIVE = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1);
            stmt.setInt(2, 1);
            stmt.setInt(3, policyId);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating policy id (" + policyId +
                    ") in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void activatePolicies(List<Integer> policyIds) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  UPDATED = ?, ACTIVE = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            for (int policyId : policyIds) {
                stmt.setInt(1, 1);
                stmt.setInt(2, 1);
                stmt.setInt(3, policyId);
                stmt.setInt(4, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating all the updated in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void markPoliciesAsUpdated(List<Integer> policyIds) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  UPDATED = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            for (int policyId : policyIds) {
                stmt.setInt(1, 0);
                stmt.setInt(2, policyId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating all the updated in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void inactivatePolicy(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  ACTIVE = ?, UPDATED = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, 0);
            stmt.setInt(2, 1);
            stmt.setInt(3, policyId);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating policy id (" + policyId +
                    ") in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public HashMap<Integer, Integer> getUpdatedPolicyIdandDeviceTypeId() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        HashMap<Integer, Integer> map = new HashMap<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_CHANGE_MGT WHERE TENANT_ID = ?";
            stmt.setInt(1, tenantId);
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                map.put(resultSet.getInt("POLICY_ID"), resultSet.getInt("DEVICE_TYPE_ID"));
            }

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the changed policies form database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return map;
    }


    @Override
    public Criterion addCriterion(Criterion criteria) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_CRITERIA (TENANT_ID, NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, tenantId);
            stmt.setString(2, criteria.getName());
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            while (generatedKeys.next()) {
                criteria.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while inserting the criterion (" + criteria.getName() +
                    ") to database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return criteria;
    }

    @Override
    public Criterion updateCriterion(Criterion criteria) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_CRITERIA SET NAME = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, criteria.getName());
            stmt.setInt(2, criteria.getId());
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while inserting the criterion (" + criteria.getName() +
                    ") to database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return criteria;
    }

    @Override
    public Criterion getCriterion(int id) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Criterion criterion = new Criterion();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE ID= ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
            }
            return criterion;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public Criterion getCriterion(String name) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Criterion criterion = new Criterion();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE NAME= ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
            }
            return criterion;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public boolean checkCriterionExists(String name) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        boolean exist = false;
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                //TODO: FIXME
                exist = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while checking whether criterion (" + name +
                    ") exists", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return exist;
    }

    @Override
    public boolean deleteCriterion(Criterion criteria) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_CRITERIA WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, criteria.getId());
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Criterion (" + criteria.getName() + ") delete from database.");
            }
            return true;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Unable to delete the policy (" + criteria.getName() +
                    ") from database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<Criterion> getAllPolicyCriteria() throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Criterion> criteria = new ArrayList<Criterion>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Criterion criterion = new Criterion();
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
                criteria.add(criterion);
            }
            return criteria;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public Policy addPolicyCriteria(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CRITERIA (CRITERIA_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            List<PolicyCriterion> criteria = policy.getPolicyCriterias();
            for (PolicyCriterion criterion : criteria) {

                stmt.setInt(1, criterion.getCriteriaId());
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            int i = 0;

            while (generatedKeys.next()) {
                policy.getPolicyCriterias().get(i).setId(generatedKeys.getInt(1));
                i++;
            }

        } catch (SQLException e) {
            String msg = "Error occurred while inserting the criterion to policy (" + policy.getPolicyName() + ") " +
                    "to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return policy;

    }

    @Override
    public boolean addPolicyCriteriaProperties(List<PolicyCriterion> policyCriteria) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CRITERIA_PROPERTIES (POLICY_CRITERION_ID, PROP_KEY, PROP_VALUE, " +
                    "CONTENT) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);

            for (PolicyCriterion criterion : policyCriteria) {
                Properties prop = criterion.getProperties();
                for (String name : prop.stringPropertyNames()) {

                    stmt.setInt(1, criterion.getId());
                    stmt.setString(2, name);
                    stmt.setString(3, prop.getProperty(name));
                    stmt.setBytes(4, PolicyManagerUtil.getBytes(criterion.getObjectMap()));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            //   stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new PolicyManagerDAOException("Error occurred while inserting the criterion properties " +
                    "to database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return false;
    }

    @Override
    public List<PolicyCriterion> getPolicyCriteria(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<PolicyCriterion> criteria = new ArrayList<PolicyCriterion>();
        try {
            conn = this.getConnection();
            String query = "SELECT DPC.ID, DPC.CRITERIA_ID, DPCP.PROP_KEY, DPCP.PROP_VALUE, DPCP.CONTENT FROM " +
                    "DM_POLICY_CRITERIA DPC LEFT JOIN DM_POLICY_CRITERIA_PROPERTIES DPCP " +
                    "ON DPCP.POLICY_CRITERION_ID = DPC.ID RIGHT JOIN DM_CRITERIA DC " +
                    "ON DC.ID=DPC.CRITERIA_ID WHERE DPC.POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            int criteriaId = 0;

            PolicyCriterion policyCriterion = null;
            Properties prop = null;
            while (resultSet.next()) {

                if (criteriaId != resultSet.getInt("ID")) {
                    if (policyCriterion != null) {
                        policyCriterion.setProperties(prop);
                        criteria.add(policyCriterion);
                    }
                    policyCriterion = new PolicyCriterion();
                    prop = new Properties();
                    criteriaId = resultSet.getInt("ID");

                    policyCriterion.setId(resultSet.getInt("ID"));
                    policyCriterion.setCriteriaId(resultSet.getInt("CRITERIA_ID"));
                } else {
                    prop.setProperty(resultSet.getString("PROP_KEY"), resultSet.getString("PROP_VALUE"));
                }
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the criteria related to policies from " +
                    "the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return criteria;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET NAME = ?,  PROFILE_ID = ?, PRIORITY = ?, COMPLIANCE = ?," +
                    " UPDATED = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, policy.getPriorityId());
            stmt.setString(4, policy.getCompliance());
            stmt.setInt(5, 1);
            stmt.setInt(6, policy.getId());
            stmt.setInt(7, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating policy (" + policy.getPolicyName() +
                    ") in database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public void recordUpdatedPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CHANGE_MGT (POLICY_ID, DEVICE_TYPE_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.setInt(2, policy.getProfile().getDeviceType().getId());
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating the policy changes in the database for" +
                    " " +
                    "policy name (" + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void recordUpdatedPolicies(List<Policy> policies) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CHANGE_MGT (POLICY_ID, DEVICE_TYPE_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            for (Policy policy : policies) {
                stmt.setInt(1, policy.getId());
                stmt.setInt(2, policy.getProfile().getDeviceType().getId());
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating the policy changes in the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeRecordsAboutUpdatedPolicies() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY_CHANGE_MGT WHERE TENANT_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while deleting the policy changes in the database for" +
                    " " +
                    "tenant id  (" + tenantId + ")", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Policy policy = new Policy();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY WHERE ID= ? AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setProfileId(resultSet.getInt("PROFILE_ID"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
            }
            return policy;

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }


    @Override
    public Policy getPolicyByProfileID(int profileId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Policy policy = new Policy();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY WHERE PROFILE_ID= ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policy.setId(resultSet.getInt("ID"));
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
            }
            return policy;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<Policy> getAllPolicies() throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Policy> policies = new ArrayList<Policy>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Policy policy = new Policy();
                policy.setId(resultSet.getInt("ID"));
                policy.setProfileId(resultSet.getInt("PROFILE_ID"));
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(tenantId);
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
                policy.setOwnershipType(resultSet.getString("OWNERSHIP_TYPE"));
                policy.setUpdated(PolicyManagerUtil.convertIntToBoolean(resultSet.getInt("UPDATED")));
                policy.setActive(PolicyManagerUtil.convertIntToBoolean(resultSet.getInt("ACTIVE")));
                policies.add(policy);
            }
            return policies;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<Policy> getPolicyOfDeviceType(String deviceTypeName) throws PolicyManagerDAOException {
        return null;
    }

    @Override
    public List<Integer> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceIdList = new ArrayList<Integer>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                deviceIdList.add(resultSet.getInt("DEVICE_ID"));
            }
            return deviceIdList;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the device related to policies", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }


    public List<String> getPolicyAppliedRoles(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<String> roleNames = new ArrayList<String>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_ROLE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                roleNames.add(resultSet.getString("ROLE_NAME"));
            }
            return roleNames;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the roles related to policies", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<String> getPolicyAppliedUsers(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<String> users = new ArrayList<String>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_USER_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                users.add(resultSet.getString("USERNAME"));
            }
            return users;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the roles related to policies", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }


    @Override
    public void addEffectivePolicyToDevice(int deviceId, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_POLICY_APPLIED (DEVICE_ID, POLICY_ID, POLICY_CONTENT, " +
                    "CREATED_TIME, UPDATED_TIME, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, policy.getId());
            stmt.setBytes(3, PolicyManagerUtil.getBytes(policy));
            stmt.setTimestamp(4, currentTimestamp);
            stmt.setTimestamp(5, currentTimestamp);
            stmt.setInt(6, tenantId);
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new PolicyManagerDAOException("Error occurred while adding the evaluated feature list to device", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void setPolicyApplied(int deviceId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_DEVICE_POLICY_APPLIED SET APPLIED_TIME = ?, APPLIED = ? WHERE DEVICE_ID = ? AND" +
                    " TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp);
            stmt.setBoolean(2, true);
            stmt.setInt(3, deviceId);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while updating applied policy to device (" +
                    deviceId + ")", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public void updateEffectivePolicyToDevice(int deviceId, Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_DEVICE_POLICY_APPLIED SET POLICY_ID = ?, POLICY_CONTENT = ?, UPDATED_TIME = ?, " +
                    "APPLIED = ? WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.setBytes(2, PolicyManagerUtil.getBytes(policy));
            stmt.setTimestamp(3, currentTimestamp);
            stmt.setBoolean(4, false);
            stmt.setInt(5, deviceId);
            stmt.setInt(6, tenantId);
            stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new PolicyManagerDAOException("Error occurred while updating the evaluated feature list " +
                    "to device", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean checkPolicyAvailable(int deviceId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        boolean exist = false;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            exist = resultSet.next();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while checking whether device (" + deviceId +
                    ") has a policy to apply", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return exist;
    }

    @Override
    public List<Integer> getPolicyIdsOfDevice(Device device) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE DEVICE_ID =  ? ";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, device.getId());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the device policy table", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return policyIds;
    }

    @Override
    public List<Integer> getPolicyOfRole(String roleName) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();
        try {
            conn = this.getConnection();
            String query = "SELECT *  FROM DM_ROLE_POLICY WHERE ROLE_NAME = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roleName);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the role policy table", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return policyIds;
    }

    @Override
    public List<Integer> getPolicyOfUser(String username) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();
        try {
            conn = this.getConnection();
            String query = "SELECT *  FROM DM_USER_POLICY WHERE USERNAME = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the user policy table", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return policyIds;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policy.getPolicyName() + ") delete from database.");
            }
            return true;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Unable to delete the policy (" + policy.getPolicyName() +
                    ") from database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            stmt.setInt(2, tenantId);
            int deleted = stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policyId + ") delete from database.");
            }
            if (deleted > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Unable to delete the policy (" + policyId + ") from database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean deleteAllPolicyRelatedConfigs(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();

            String userPolicy = "DELETE FROM DM_USER_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(userPolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            String rolePolicy = "DELETE FROM DM_ROLE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(rolePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            String devicePolicy = "DELETE FROM DM_DEVICE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(devicePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            String deleteCriteria = "DELETE FROM DM_POLICY_CRITERIA WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(deleteCriteria);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policyId + ") related configs deleted from database.");
            }
            return true;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Unable to delete the policy (" + policyId +
                    ") related configs from database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws PolicyManagerDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }

    private Policy persistPolicy(Policy policy) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID, PRIORITY, COMPLIANCE, OWNERSHIP_TYPE," +
                    " " +
                    "UPDATED, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, readHighestPriorityOfPolicies());
            stmt.setString(5, policy.getCompliance());
            stmt.setString(6, policy.getOwnershipType());
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0 && log.isDebugEnabled()) {
                String msg = "No rows are updated on the policy table.";
                log.debug(msg);
            }
            generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                policy.setId(generatedKeys.getInt(1));
            }
            // checking policy id here, because it object could have passed with id from the calling method.
            if (policy.getId() == 0) {
                throw new RuntimeException("No rows were inserted, policy id cannot be null.");
            }

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while adding policy to the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return policy;
    }


    /**
     * This method returns the device type id when supplied with device type name.
     *
     * @param deviceType device type.
     * @return integer value
     * @throws PolicyManagerDAOException
     */
    private int getDeviceTypeId(String deviceType) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int deviceTypeId = -1;
        try {
            conn = this.getConnection();
            String query = "SELECT ID FROM DM_DEVICE_TYPE WHERE NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                deviceTypeId = resultSet.getInt("ID");
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while selecting the device type id", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceTypeId;
    }


    private int readHighestPriorityOfPolicies() throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int priority = 0;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT MAX(PRIORITY) PRIORITY FROM DM_POLICY WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                priority = resultSet.getInt("PRIORITY") + 1;
            }
            if (log.isDebugEnabled()) {
                log.debug("Priority of the new policy added is (" + priority + ")");
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the highest priority of the policies", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return priority;
    }

    @Override
    public int getPolicyCount() throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int policyCount = 0;
        try {
            conn = this.getConnection();
            String query = "SELECT COUNT(ID) AS POLICY_COUNT FROM DM_POLICY WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyCount = resultSet.getInt("POLICY_COUNT");
            }
            return policyCount;
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while reading the policies from the database", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public int getAppliedPolicyId(int deviceId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("POLICY_ID");
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the applied policy id", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return 0;
    }

    @Override
    public Policy getAppliedPolicy(int deviceId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Policy policy = null;
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                ByteArrayInputStream bais = null;
                ObjectInputStream ois = null;
                byte[] contentBytes;

                try {
                    contentBytes = (byte[]) resultSet.getBytes("POLICY_CONTENT");
                    bais = new ByteArrayInputStream(contentBytes);
                    ois = new ObjectInputStream(bais);
                    policy = (Policy) ois.readObject();
                } finally {
                    if (bais != null) {
                        try {
                            bais.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ByteArrayOutputStream", e);
                        }
                    }
                    if (ois != null) {
                        try {
                            ois.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ObjectOutputStream", e);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the applied policy", e);
        } catch (IOException e) {
            throw new PolicyManagerDAOException("Unable to read the byte stream for content", e);
        } catch (ClassNotFoundException e) {
            throw new PolicyManagerDAOException("Class not found while converting the object", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
//
//        if (policy != null && log.isDebugEnabled()) {
//            log.debug("Applied policy logging details started ------------------");
//            log.debug("Applied policy name " + policy.getPolicyName() + "for the device id " + deviceId);
//            log.debug(policy.getCompliance());
//            log.debug(policy.getId());
//            log.debug(policy.getPriorityId());
//            log.debug("Applied policy logging details finished....");
//        }
        return policy;
    }

    @Override
    public HashMap<Integer, Integer> getAppliedPolicyIds(List<Integer> deviceIds) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        HashMap<Integer, Integer> devicePolicyIds = new HashMap<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, PolicyManagerUtil.makeString(deviceIds));
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                devicePolicyIds.put(resultSet.getInt("DEVICE_ID"), resultSet.getInt("POLICY_ID"));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the applied policy", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return devicePolicyIds;
    }

    @Override
    public HashMap<Integer, Integer> getAppliedPolicyIdsDeviceIds() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        HashMap<Integer, Integer> devicePolicyIds = new HashMap<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                devicePolicyIds.put(resultSet.getInt("DEVICE_ID"), resultSet.getInt("POLICY_ID"));
            }
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while getting the applied policy", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return devicePolicyIds;
    }

}
