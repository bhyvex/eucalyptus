/*************************************************************************
 * Copyright 2009-2013 Eucalyptus Systems, Inc.
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
 *
 * This file may incorporate work covered under the following copyright
 * and permission notice:
 *
 *   Software License Agreement (BSD License)
 *
 *   Copyright (c) 2008, Regents of the University of California
 *   All rights reserved.
 *
 *   Redistribution and use of this software in source and binary forms,
 *   with or without modification, are permitted provided that the
 *   following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *   COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *   BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGE. USERS OF THIS SOFTWARE ACKNOWLEDGE
 *   THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE LICENSED MATERIAL,
 *   COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS SOFTWARE,
 *   AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *   IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA,
 *   SANTA BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY,
 *   WHICH IN THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION,
 *   REPLACEMENT OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO
 *   IDENTIFIED, OR WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT
 *   NEEDED TO COMPLY WITH ANY SUCH LICENSES OR RIGHTS.
 ************************************************************************/

package com.eucalyptus.objectstorage.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.eucalyptus.entities.Entities;
import com.eucalyptus.entities.TransactionResource;
import com.eucalyptus.objectstorage.BucketCorsManagers;
import com.eucalyptus.objectstorage.entities.CorsRule;
import com.eucalyptus.objectstorage.exceptions.ObjectStorageException;
import com.google.common.collect.Lists;

import net.sf.json.JSONArray;

/*
 *
 */
public class DbBucketCorsManagerImpl implements BucketCorsManager {

  private static Logger LOG = Logger.getLogger(DbBucketCorsManagerImpl.class);

  @Override
  public void start() throws Exception {
    // no-op
  }

  @Override
  public void stop() throws Exception {
    // no-op
  }

  @Override
  public void deleteCorsRules(@Nonnull String bucketUuid, TransactionResource tran) {

    if (tran == null || !tran.isActive()) {
      throw new RuntimeException(new ObjectStorageException("in DbBucketCorsManagerImpl.deleteCorsRules, "
          + "but was not given an active transaction"));
    }
    CorsRule example = new CorsRule();
    example.setBucketUuid(bucketUuid);
    List<CorsRule> existing = Entities.query(example);
    if (existing != null && existing.size() > 0) {
      // delete them
      Map<String, String> criteria = new HashMap<>();
      criteria.put("bucketUuid", bucketUuid);
      Entities.deleteAllMatching(CorsRule.class, "WHERE bucketUuid = :bucketUuid", criteria);
    }
  }

  @Override
  public void deleteCorsRules(@Nonnull String bucketUuid) throws ObjectStorageException {
    try (final TransactionResource tran = Entities.transactionFor(CorsRule.class)) {
      BucketCorsManagers.getInstance().deleteCorsRules(bucketUuid, tran);
      tran.commit();
    } catch (Exception ex) {
      LOG.error("Exception caught while deleting CORS rules for bucket " + bucketUuid + ": " + ex.getMessage());
      throw new ObjectStorageException("InternalServerError", "Exception caught while deleting CORS rules for bucket "
          + bucketUuid, "Bucket", bucketUuid, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void addCorsRules(@Nonnull List<com.eucalyptus.storage.msgs.s3.CorsRule> rules, @Nonnull String bucketUuid)
      throws ObjectStorageException {

    LOG.debug("In addCorsRule");

    try (TransactionResource tran = Entities.transactionFor(CorsRule.class)) {
      // first get rid of existing rules
      BucketCorsManagers.getInstance().deleteCorsRules(bucketUuid, tran);
      // now add the rules from the messages
      if (rules != null && rules.size() > 0) {
        for (com.eucalyptus.storage.msgs.s3.CorsRule ruleInfo : rules) {
          CorsRule converted = convertCorsRule(ruleInfo, bucketUuid);
          Entities.merge(converted);
        }
      }
      tran.commit();
    } catch (Exception ex) {
      LOG.error("Exception caught while adding CORS rules for bucket " + bucketUuid + ": " + ex.getMessage());
      throw new ObjectStorageException("InternalServerError", "An exception was caught while adding CORS rules for bucket "
          + bucketUuid, "Bucket", bucketUuid, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
    LOG.debug("Finished addCorsRule");

  }

  @Override
  public List<com.eucalyptus.storage.msgs.s3.CorsRule> getCorsRules(@Nonnull String bucketUuid) throws Exception {

    List<com.eucalyptus.storage.msgs.s3.CorsRule> responseRules = Lists.newArrayList();

    List<CorsRule> rulesFromDb = null;

    CorsRule exampleRule = new CorsRule();
    exampleRule.setBucketUuid(bucketUuid);
    try (final TransactionResource tran = Entities.transactionFor(CorsRule.class)) {
      rulesFromDb = Entities.query(exampleRule);
      tran.commit();
    } catch (NoSuchElementException e) {
      // No CORS configuration exists. An empty list will be returned.
    } catch (Exception ex) {
      LOG.error("Exception caught while retrieving CORS rules for bucket " + bucketUuid + ": ", ex);
    }

    if (rulesFromDb != null) {
      for (CorsRule fromDb : rulesFromDb) {
        responseRules.add(convertCorsRule(fromDb));
      }
    }

    return responseRules;
  }

  private CorsRule convertCorsRule(com.eucalyptus.storage.msgs.s3.CorsRule rule, String bucketUuid) {

    LOG.debug("In convertCorsRule from message to DB entity");

    CorsRule entity = new CorsRule();
    entity.setBucketUuid(bucketUuid);
    entity.setRuleId(rule.getId());
    entity.setMaxAgeSeconds(rule.getMaxAgeSeconds());

    entity.setAllowedMethodsJSON(convertCorsArrayToJSON(rule.getAllowedMethods()));
    entity.setAllowedOriginsJSON(convertCorsArrayToJSON(rule.getAllowedOrigins()));
    entity.setAllowedHeadersJSON(convertCorsArrayToJSON(rule.getAllowedHeaders()));
    entity.setExposeHeadersJSON(convertCorsArrayToJSON(rule.getExposeHeaders()));

    LOG.debug("Finished convertCorsRule from message to DB entity");
    return entity;
  }

  private String convertCorsArrayToJSON(String[] corsArray) {
    JSONArray corsJSON = new JSONArray();
    if (corsArray != null) {
      for (int idx = 0; idx < corsArray.length; idx++) {
        corsJSON.add(corsArray[idx]);
      }
    }
    return corsJSON.toString();
  }

  private com.eucalyptus.storage.msgs.s3.CorsRule convertCorsRule(CorsRule entity) {

    LOG.debug("In convertCorsRule from DB entity to message");
    com.eucalyptus.storage.msgs.s3.CorsRule ruleResponse = new com.eucalyptus.storage.msgs.s3.CorsRule();
    ruleResponse.setId(entity.getRuleId());
    ruleResponse.setMaxAgeSeconds(entity.getMaxAgeSeconds());

    ruleResponse.setAllowedMethods(convertCorsJSONToArray(entity.getAllowedMethodsJSON()));
    ruleResponse.setAllowedOrigins(convertCorsJSONToArray(entity.getAllowedOriginsJSON()));
    ruleResponse.setAllowedHeaders(convertCorsJSONToArray(entity.getAllowedHeadersJSON()));
    ruleResponse.setExposeHeaders(convertCorsJSONToArray(entity.getExposeHeadersJSON()));

    LOG.debug("Finished convertCorsRule from DB entity to message");

    return ruleResponse;
  }

  private String[] convertCorsJSONToArray(String corsJSONString) {
    JSONArray corsJSON = JSONArray.fromObject(corsJSONString);
    String[] corsArray = new String[corsJSON.size()];
    for (int idx = 0; idx < corsJSON.size(); idx++) {
      corsArray[idx] = corsJSON.getString(idx);
    }
    return corsArray;
  }

}