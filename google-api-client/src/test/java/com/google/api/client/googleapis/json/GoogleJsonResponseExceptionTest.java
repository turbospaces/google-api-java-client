/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.googleapis.json.GoogleJsonErrorTest.ErrorTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleJsonResponseException}.
 *
 * @author Yaniv Inbar
 */
public class GoogleJsonResponseExceptionTest extends TestCase {

  public void testFrom_noDetails() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("200"));
  }

  public void testFrom_withDetails() throws Exception {
    HttpTransport transport = new ErrorTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertEquals(GoogleJsonErrorTest.ERROR, GoogleJsonErrorTest.FACTORY.toString(ge.getDetails()));
    assertTrue(ge.getMessage().startsWith("403"));
  }

  public void testFrom_detailsMissingContent() throws Exception {
    HttpTransport transport = new ErrorTransport(null, Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
  }

  public void testFrom_detailsArbitraryJsonContent() throws Exception {
    HttpTransport transport = new ErrorTransport("{\"foo\":\"bar\"}", Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
  }

  public void testFrom_detailsArbitraryXmlContent() throws Exception {
    HttpTransport transport = new ErrorTransport("<foo>", "application/atom+xml; charset=utf-8");
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
    assertTrue(ge.getMessage().contains("<foo>"));
  }

  public void testFrom_errorNoContentButWithJsonContentType() throws Exception {
    HttpTransport transport = new ErrorTransport("", Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
  }

  public void testFrom_errorEmptyContentButWithJsonContentType() throws Exception {
    HttpTransport transport = new ErrorTransport(null, Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
  }

  public void testFrom_detailsErrorObject() throws Exception {
    HttpTransport transport =
        new ErrorTransport(
            "{\"error\": {\"message\": \"invalid_token\"}, \"error_description\": \"Invalid value\"}",
            Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNotNull(ge.getDetails());
    assertEquals("invalid_token", ge.getDetails().getMessage());
    assertTrue(ge.getMessage().contains("403"));
  }

  public void testFrom_detailsErrorString() throws Exception {
    HttpTransport transport =
        new ErrorTransport(
            "{\"error\": \"invalid_token\", \"error_description\": \"Invalid value\"}",
            Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().contains("403"));
    assertTrue(ge.getMessage().contains("invalid_token"));
  }

  public void testFrom_detailsNoErrorField() throws Exception {
    HttpTransport transport =
        new ErrorTransport("{\"error_description\": \"Invalid value\"}", Json.MEDIA_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertNull(ge.getDetails());
    assertTrue(ge.getMessage().startsWith("403"));
  }
}
