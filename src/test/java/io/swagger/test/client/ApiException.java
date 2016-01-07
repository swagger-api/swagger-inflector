/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.test.client;

import java.util.List;
import java.util.Map;

public class ApiException extends Exception {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;
    private int code = 0;
    private String message = null;
    private Map<String, List<String>> responseHeaders = null;
    private String responseBody = null;

    public ApiException() {
    }

    public ApiException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiException(int code, String message, Map<String, List<String>> responseHeaders, String responseBody) {
        this.code = code;
        this.message = message;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Get the HTTP response headers.
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     */
    public String getResponseBody() {
        return responseBody;
    }
}
