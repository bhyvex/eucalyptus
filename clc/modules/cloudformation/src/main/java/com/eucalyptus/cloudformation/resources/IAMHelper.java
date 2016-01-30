/*************************************************************************
 * Copyright 2009-2014 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/
package com.eucalyptus.cloudformation.resources;

import com.eucalyptus.auth.euare.AccessKeyMetadataType;
import com.eucalyptus.auth.euare.GroupType;
import com.eucalyptus.auth.euare.InstanceProfileType;
import com.eucalyptus.auth.euare.ListAccessKeysResponseType;
import com.eucalyptus.auth.euare.ListAccessKeysType;
import com.eucalyptus.auth.euare.ListGroupsForUserResponseType;
import com.eucalyptus.auth.euare.ListGroupsForUserType;
import com.eucalyptus.auth.euare.ListGroupsResponseType;
import com.eucalyptus.auth.euare.ListGroupsType;
import com.eucalyptus.auth.euare.ListInstanceProfilesResponseType;
import com.eucalyptus.auth.euare.ListInstanceProfilesType;
import com.eucalyptus.auth.euare.ListRolesResponseType;
import com.eucalyptus.auth.euare.ListRolesType;
import com.eucalyptus.auth.euare.ListUsersResponseType;
import com.eucalyptus.auth.euare.ListUsersType;
import com.eucalyptus.auth.euare.RoleType;
import com.eucalyptus.auth.euare.UserType;
import com.eucalyptus.cloudformation.resources.standard.propertytypes.EmbeddedIAMPolicy;
import com.eucalyptus.cloudformation.util.MessageHelper;
import com.eucalyptus.component.ServiceConfiguration;
import com.eucalyptus.util.async.AsyncRequests;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ethomas on 1/28/16.
 */
public class IAMHelper {
  public static UserType getUser(ServiceConfiguration configuration, String userName, String effectiveUserId) throws Exception {
    UserType retVal = null;
    // if no user, return
    boolean seenAllUsers = false;
    String userMarker = null;
    while (!seenAllUsers && retVal == null) {
      ListUsersType listUsersType = MessageHelper.createMessage(ListUsersType.class, effectiveUserId);
      if (userMarker != null) {
        listUsersType.setMarker(userMarker);
      }
      ListUsersResponseType listUsersResponseType = AsyncRequests.<ListUsersType,ListUsersResponseType> sendSync(configuration, listUsersType);
      if (listUsersResponseType.getListUsersResult().getIsTruncated() == Boolean.TRUE) {
        userMarker = listUsersResponseType.getListUsersResult().getMarker();
      } else {
        seenAllUsers = true;
      }
      if (listUsersResponseType.getListUsersResult().getUsers() != null && listUsersResponseType.getListUsersResult().getUsers().getMemberList() != null) {
        for (UserType userType: listUsersResponseType.getListUsersResult().getUsers().getMemberList()) {
          if (userType.getUserName().equals(userName)) {
            retVal = userType;
            break;
          }
        }
      }
    }
    return retVal;
  }

  public static boolean userExists(ServiceConfiguration configuration, String userName, String effectiveUserId) throws Exception {
    return getUser(configuration, userName, effectiveUserId) != null;
  }

  public static AccessKeyMetadataType getAccessKey(ServiceConfiguration configuration, String accessKeyId, String userName, String effectiveUserId) throws Exception {
    AccessKeyMetadataType retVal = null;
    boolean seenAllAccessKeys = false;
    String accessKeyMarker = null;
    while (!seenAllAccessKeys && (retVal == null)) {
      ListAccessKeysType listAccessKeysType = MessageHelper.createMessage(ListAccessKeysType.class, effectiveUserId);
      listAccessKeysType.setUserName(userName);
      if (accessKeyMarker != null) {
        listAccessKeysType.setMarker(accessKeyMarker);
      }
      ListAccessKeysResponseType listAccessKeysResponseType = AsyncRequests.<ListAccessKeysType,ListAccessKeysResponseType> sendSync(configuration, listAccessKeysType);
      if (listAccessKeysResponseType.getListAccessKeysResult().getIsTruncated() == Boolean.TRUE) {
        accessKeyMarker = listAccessKeysResponseType.getListAccessKeysResult().getMarker();
      } else {
        seenAllAccessKeys = true;
      }
      if (listAccessKeysResponseType.getListAccessKeysResult().getAccessKeyMetadata() != null && listAccessKeysResponseType.getListAccessKeysResult().getAccessKeyMetadata().getMemberList() != null) {
        for (AccessKeyMetadataType accessKeyMetadataType: listAccessKeysResponseType.getListAccessKeysResult().getAccessKeyMetadata().getMemberList()) {
          if (accessKeyMetadataType.getAccessKeyId().equals(accessKeyId)) {
            retVal = accessKeyMetadataType;
            break;
          }
        }
      }
    }
    return retVal;
  }

  public static boolean accessKeyExists(ServiceConfiguration configuration, String accessKeyId, String userName, String effectiveUserId) throws Exception {
    return getAccessKey(configuration, accessKeyId, userName, effectiveUserId) != null;
  }

  public static boolean groupExists(ServiceConfiguration configuration, String groupName, String effectiveUserId) throws Exception {
    return (getGroup(configuration, groupName, effectiveUserId) != null);
  }

  public static GroupType getGroup(ServiceConfiguration configuration, String groupName, String effectiveUserId) throws Exception {
    GroupType retVal = null;
    boolean seenAllGroups = false;
    String groupMarker = null;
    while (!seenAllGroups && retVal == null) {
      ListGroupsType listGroupsType = MessageHelper.createMessage(ListGroupsType.class, effectiveUserId);
      if (groupMarker != null) {
        listGroupsType.setMarker(groupMarker);
      }
      ListGroupsResponseType listGroupsResponseType = AsyncRequests.<ListGroupsType,ListGroupsResponseType> sendSync(configuration, listGroupsType);
      if (listGroupsResponseType.getListGroupsResult().getIsTruncated() == Boolean.TRUE) {
        groupMarker = listGroupsResponseType.getListGroupsResult().getMarker();
      } else {
        seenAllGroups = true;
      }
      if (listGroupsResponseType.getListGroupsResult().getGroups() != null && listGroupsResponseType.getListGroupsResult().getGroups().getMemberList() != null) {
        for (GroupType groupType: listGroupsResponseType.getListGroupsResult().getGroups().getMemberList()) {
          if (groupType.getGroupName().equals(groupName)) {
            retVal = groupType;
            break;
          }
        }
      }
    }
    return retVal;
  }

  public static Set<String> getPolicyNames(List<EmbeddedIAMPolicy> policies) {
    Set<String> policyNames = Sets.newLinkedHashSet();
    if (policies != null) {
      for (EmbeddedIAMPolicy policy : policies) {
        policyNames.add(policy.getPolicyName());
      }
    }
    return policyNames;
  }

  public static boolean instanceProfileExists(ServiceConfiguration configuration, String instanceProfileName, String effectiveUserId) throws Exception {
    return getInstanceProfile(configuration, instanceProfileName, effectiveUserId) != null;
  }

  public static InstanceProfileType getInstanceProfile(ServiceConfiguration configuration, String instanceProfileName, String effectiveUserId) throws Exception {
    InstanceProfileType retVal = null;
    boolean seenAllInstanceProfiles = false;
    String instanceProfileMarker = null;
    while (!seenAllInstanceProfiles && retVal == null) {
      ListInstanceProfilesType listInstanceProfilesType = MessageHelper.createMessage(ListInstanceProfilesType.class, effectiveUserId);
      if (instanceProfileMarker != null) {
        listInstanceProfilesType.setMarker(instanceProfileMarker);
      }
      ListInstanceProfilesResponseType listInstanceProfilesResponseType = AsyncRequests.<ListInstanceProfilesType,ListInstanceProfilesResponseType> sendSync(configuration, listInstanceProfilesType);
      if (listInstanceProfilesResponseType.getListInstanceProfilesResult().getIsTruncated() == Boolean.TRUE) {
        instanceProfileMarker = listInstanceProfilesResponseType.getListInstanceProfilesResult().getMarker();
      } else {
        seenAllInstanceProfiles = true;
      }
      if (listInstanceProfilesResponseType.getListInstanceProfilesResult().getInstanceProfiles() != null && listInstanceProfilesResponseType.getListInstanceProfilesResult().getInstanceProfiles().getMember() != null) {
        for (InstanceProfileType instanceProfileType: listInstanceProfilesResponseType.getListInstanceProfilesResult().getInstanceProfiles().getMember()) {
          if (instanceProfileType.getInstanceProfileName().equals(instanceProfileName)) {
            retVal = instanceProfileType;
            break;
          }
        }
      }
    }
    return retVal;
  }

  public static List<String> getExistingGroups(ServiceConfiguration configuration, Set<String> passedInGroups, String effectiveUserId) throws Exception {
    List<String> realGroups = Lists.newArrayList();
    boolean seenAllGroups = false;
    String groupMarker = null;
    while (!seenAllGroups) {
      ListGroupsType listGroupsType = MessageHelper.createMessage(ListGroupsType.class, effectiveUserId);
      if (groupMarker != null) {
        listGroupsType.setMarker(groupMarker);
      }
      ListGroupsResponseType listGroupsResponseType = AsyncRequests.<ListGroupsType,ListGroupsResponseType> sendSync(configuration, listGroupsType);
      if (listGroupsResponseType.getListGroupsResult().getIsTruncated() == Boolean.TRUE) {
        groupMarker = listGroupsResponseType.getListGroupsResult().getMarker();
      } else {
        seenAllGroups = true;
      }
      if (listGroupsResponseType.getListGroupsResult().getGroups() != null && listGroupsResponseType.getListGroupsResult().getGroups().getMemberList() != null) {
        for (GroupType groupType: listGroupsResponseType.getListGroupsResult().getGroups().getMemberList()) {
          if (passedInGroups.contains(groupType.getGroupName())) {
            realGroups.add(groupType.getGroupName());
          }
        }
      }
    }
    return realGroups;
  }

  public static List<String> getExistingUsers(ServiceConfiguration configuration, Set<String> passedInUsers, String effectiveUserId) throws Exception {
    List<String> realUsers = Lists.newArrayList();
    boolean seenAllUsers = false;
    String userMarker = null;
    while (!seenAllUsers) {
      ListUsersType listUsersType = MessageHelper.createMessage(ListUsersType.class, effectiveUserId);
      if (userMarker != null) {
        listUsersType.setMarker(userMarker);
      }
      ListUsersResponseType listUsersResponseType = AsyncRequests.<ListUsersType,ListUsersResponseType> sendSync(configuration, listUsersType);
      if (listUsersResponseType.getListUsersResult().getIsTruncated() == Boolean.TRUE) {
        userMarker = listUsersResponseType.getListUsersResult().getMarker();
      } else {
        seenAllUsers = true;
      }
      if (listUsersResponseType.getListUsersResult().getUsers() != null && listUsersResponseType.getListUsersResult().getUsers().getMemberList() != null) {
        for (UserType userType: listUsersResponseType.getListUsersResult().getUsers().getMemberList()) {
          if (passedInUsers.contains(userType.getUserName())) {
            realUsers.add(userType.getUserName());
          }
        }
      }
    }
    return realUsers;
  }

  public static List<String> getExistingRoles(ServiceConfiguration configuration, Set<String> passedInRoles, String effectiveUserId) throws Exception {
    List<String> realRoles = Lists.newArrayList();
    boolean seenAllRoles = false;
    String roleMarker = null;
    while (!seenAllRoles) {
      ListRolesType listRolesType = MessageHelper.createMessage(ListRolesType.class, effectiveUserId);
      if (roleMarker != null) {
        listRolesType.setMarker(roleMarker);
      }
      ListRolesResponseType listRolesResponseType = AsyncRequests.<ListRolesType,ListRolesResponseType> sendSync(configuration, listRolesType);
      if (listRolesResponseType.getListRolesResult().getIsTruncated() == Boolean.TRUE) {
        roleMarker = listRolesResponseType.getListRolesResult().getMarker();
      } else {
        seenAllRoles = true;
      }
      if (listRolesResponseType.getListRolesResult().getRoles() != null && listRolesResponseType.getListRolesResult().getRoles().getMember() != null) {
        for (RoleType roleType: listRolesResponseType.getListRolesResult().getRoles().getMember()) {
          if (passedInRoles.contains(roleType.getRoleName())) {
            realRoles.add(roleType.getRoleName());
          }
        }
      }
    }
    return realRoles;
  }

  public static boolean roleExists(ServiceConfiguration configuration, String roleName, String effectiveUserId) throws Exception {
    return getRole(configuration, roleName, effectiveUserId) != null;
  }

  private static RoleType getRole(ServiceConfiguration configuration, String roleName, String effectiveUserId) throws Exception {
    RoleType retVal = null;
    boolean seenAllRoles = false;
    String RoleMarker = null;
    while (!seenAllRoles && retVal == null) {
      ListRolesType listRolesType = MessageHelper.createMessage(ListRolesType.class, effectiveUserId);
      if (RoleMarker != null) {
        listRolesType.setMarker(RoleMarker);
      }
      ListRolesResponseType listRolesResponseType = AsyncRequests.<ListRolesType,ListRolesResponseType> sendSync(configuration, listRolesType);
      if (listRolesResponseType.getListRolesResult().getIsTruncated() == Boolean.TRUE) {
        RoleMarker = listRolesResponseType.getListRolesResult().getMarker();
      } else {
        seenAllRoles = true;
      }
      if (listRolesResponseType.getListRolesResult().getRoles() != null && listRolesResponseType.getListRolesResult().getRoles().getMember() != null) {
        for (RoleType roleType: listRolesResponseType.getListRolesResult().getRoles().getMember()) {
          if (roleType.getRoleName().equals(roleName)) {
            retVal = roleType;
            break;
          }
        }
      }
    }
    return retVal;
  }

  public static <T> Set<T> collectionToSetAndNullToEmpty(Collection<T> c) {
    HashSet<T> set = Sets.newLinkedHashSet();
    if (c != null) {
      set.addAll(c);
    }
    return set;
  }

  public static Set<String> getGroupNamesForUser(ServiceConfiguration configuration, String userName, String effectiveUserId) throws Exception {
    Set<String> groupSet = Sets.newLinkedHashSet();
    boolean seenAllGroups = false;
    String groupMarker = null;
    while (!seenAllGroups) {
      ListGroupsForUserType listGroupsForUserType = MessageHelper.createMessage(ListGroupsForUserType.class, effectiveUserId);
      listGroupsForUserType.setUserName(userName);
      if (groupMarker != null) {
        listGroupsForUserType.setMarker(groupMarker);
      }
      ListGroupsForUserResponseType listGroupsForUserResponseType = AsyncRequests.<ListGroupsForUserType,ListGroupsForUserResponseType> sendSync(configuration, listGroupsForUserType);
      if (listGroupsForUserResponseType.getListGroupsForUserResult().getIsTruncated() == Boolean.TRUE) {
        groupMarker = listGroupsForUserResponseType.getListGroupsForUserResult().getMarker();
      } else {
        seenAllGroups = true;
      }
      if (listGroupsForUserResponseType.getListGroupsForUserResult().getGroups() != null && listGroupsForUserResponseType.getListGroupsForUserResult().getGroups().getMemberList() != null) {
        for (GroupType groupType: listGroupsForUserResponseType.getListGroupsForUserResult().getGroups().getMemberList()) {
          groupSet.add(groupType.getGroupName());
        }
      }
    }
    return groupSet;
  }
}
