// Copyright 2006-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class JcrAuthorizationManagerTest extends TestCase {

  public final void testAuthorizeDocids() throws RepositoryException {

    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthorizationManager authorizationManager =
        new JcrAuthorizationManager(session);

    {
      String username = "joe";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.TRUE);
      expectedResults.put("doc2", Boolean.TRUE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.FALSE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "bill";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.TRUE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "fred";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.TRUE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "murgatroyd";

      Map expectedResults = new HashMap();
      expectedResults.put("doc1", Boolean.FALSE);
      expectedResults.put("doc2", Boolean.FALSE);
      expectedResults.put("doc3", Boolean.TRUE);
      expectedResults.put("doc4", Boolean.FALSE);
      expectedResults.put("doc5", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }
  }

  public final void testAuthorizeNewFormat() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLogAcl.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthorizationManager authorizationManager =
        new JcrAuthorizationManager(session);
    {
      String username = "joe";
      Map expectedResults = new HashMap();

      expectedResults.put("no_acl", Boolean.TRUE);
      expectedResults.put("user_acl", Boolean.TRUE);
      expectedResults.put("user_role_acl", Boolean.TRUE);
      expectedResults.put("user_scoped_role_acl", Boolean.TRUE);
      expectedResults.put("user_group_acl", Boolean.TRUE);
      expectedResults.put("user_group_role_acl", Boolean.TRUE);
      expectedResults.put("user_reader_acl", Boolean.TRUE);
      expectedResults.put("user_owner_acl", Boolean.TRUE);
      expectedResults.put("user_scoped_owner_acl", Boolean.TRUE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "mary";
      Map expectedResults = new HashMap();

      expectedResults.put("no_acl", Boolean.TRUE);
      expectedResults.put("user_acl", Boolean.TRUE);
      expectedResults.put("user_role_acl", Boolean.TRUE);
      expectedResults.put("user_scoped_role_acl", Boolean.TRUE);
      expectedResults.put("user_group_acl", Boolean.TRUE);
      expectedResults.put("user_group_role_acl", Boolean.TRUE);
      expectedResults.put("user_reader_acl", Boolean.FALSE);
      expectedResults.put("user_owner_acl", Boolean.FALSE);
      expectedResults.put("user_scoped_owner_acl", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }

    {
      String username = "eng";
      Map expectedResults = new HashMap();

      expectedResults.put("no_acl", Boolean.TRUE);
      expectedResults.put("user_acl", Boolean.FALSE);
      expectedResults.put("user_role_acl", Boolean.FALSE);
      expectedResults.put("user_scoped_role_acl", Boolean.FALSE);
      expectedResults.put("user_group_acl", Boolean.FALSE);
      expectedResults.put("user_group_role_acl", Boolean.FALSE);
      expectedResults.put("user_reader_acl", Boolean.FALSE);
      expectedResults.put("user_owner_acl", Boolean.FALSE);
      expectedResults.put("user_scoped_owner_acl", Boolean.FALSE);

      testAuthorization(authorizationManager, expectedResults, username);
    }
  }

  private void testAuthorization(AuthorizationManager authorizationManager,
      Map expectedResults, String username)
      throws com.google.enterprise.connector.spi.RepositoryException {
    List docids = new LinkedList(expectedResults.keySet());

    AuthenticationIdentity identity = 
      new SimpleAuthenticationIdentity(username);
    Collection results = authorizationManager.authorizeDocids(docids, identity);

    for (Iterator i = results.iterator(); i.hasNext();) {
      AuthorizationResponse response = (AuthorizationResponse) i.next();
      String uuid = response.getDocid();
      boolean ok = response.isValid();
      Boolean expected = (Boolean) expectedResults.get(uuid);
      Assert.assertEquals(username + " access to " + uuid, expected
          .booleanValue(), ok);
    }
  }

}
