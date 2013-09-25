/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.plus.samples.quickstart.domains;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Acl;
import com.google.api.services.plusDomains.model.Activity;
import com.google.api.services.plusDomains.model.PlusDomainsAclentryResource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Simple program to demonstrate the Google+ Domains API.
 *
 * This program shows how to authenticate an app for domain-wide delegation and how
 * to complete an activities.insert API call. For details on how to authenticate on
 * a per-user basis using OAuth 2.0, or for examples of other API calls, please see
 * the documentation at https://developers.google.com/+/domains/.
 *
 * @author joannasmith@google.com (Joanna Smith)
 */
public class DomainDelegation {
  /**
   * Update SERVICE_ACCOUNT_EMAIL with the email address of the service account for the client ID
   *  created in the developer console.
   */
  private static final String SERVICE_ACCOUNT_EMAIL = "<some-id>@developer.gserviceaccount.com";

  /**
   * Update SERVICE_ACCOUNT_PKCS12_FILE_PATH with the file path to the private key file downloaded
   *  from the developer console.
   */
  private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH =
      "/path/to/<public_key_fingerprint>-privatekey.p12";

  /**
   * Update USER_EMAIL with the email address of the user within your domain that you would like
   *  to act on behalf of.
   */
  private static final String USER_EMAIL = "user@mydomain.com";


  /**
   * plus.me and plus.stream.write are the scopes required to perform the tasks in this quickstart.
   *  For a full list of available scopes and their uses, please see the documentation.
   */
  private static final List<String> SCOPE = Arrays.asList(
      "https://www.googleapis.com/auth/plus.me",
      "https://www.googleapis.com/auth/plus.stream.write");


  /**
   * Builds and returns a Plus service object authorized with the service accounts
   * that act on behalf of the given user.
   *
   * @return Plus service object that is ready to make requests.
   * @throws GeneralSecurityException if authentication fails.
   * @throws IOException if authentication fails.
   */
  private static PlusDomains authenticate() throws GeneralSecurityException, IOException {

    System.out.println(String.format("Authenticate the domain for %s", USER_EMAIL));

    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    // Setting the sub field with USER_EMAIL allows you to make API calls using the special keyword 
    // 'me' in place of a user id for that user.
    GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(jsonFactory)
        .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
        .setServiceAccountScopes(SCOPE)
        .setServiceAccountUser(USER_EMAIL)
        .setServiceAccountPrivateKeyFromP12File(
            new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
        .build();

    // Create and return the Plus service object
    PlusDomains service = new PlusDomains.Builder(httpTransport, jsonFactory, credential).build();
    return service;
  }

  /**
   * Create a new post on behalf of the user associated with the credential object of the service,
   * restricted to the domain.
   *
   * @param service Plus service object that is ready to make requests.
   * @throws IOException if the insert operation fails or if authentication fails.
   * @throws GeneralSecurityException if authentication fails.
   */
  public static void main(String[] args) throws Exception {
    // Create an authorized API client
    PlusDomains service = authenticate();

    // Set the user's ID to 'me': requires the plus.me scope
    String userId = "me";
    String msg = "Happy Monday! #caseofthemondays";

    System.out.println("Inserting activity");

    // Create the audience of the post
    PlusDomainsAclentryResource res = new PlusDomainsAclentryResource();
    // Share to the domain
    res.setType("domain");

    List<PlusDomainsAclentryResource> aclEntries = new ArrayList<PlusDomainsAclentryResource>();
    aclEntries.add(res);

    Acl acl = new Acl();
    acl.setItems(aclEntries);
    // Required, this does the domain restriction
    acl.setDomainRestricted(true);

    Activity activity = new Activity()
        .setObject(new Activity.PlusDomainsObject().setOriginalContent(msg))
        .setAccess(acl);

    activity = service.activities().insert(userId, activity).execute();

    System.out.println(activity);
  }
}
