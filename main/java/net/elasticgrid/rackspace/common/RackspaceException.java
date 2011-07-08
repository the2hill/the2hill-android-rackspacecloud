/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package net.elasticgrid.rackspace.common;

/**
 * A wrapper exception to simplify catching errors related to Rackspace activity.
 *
 * @author Jerome Bernard
 */
public class RackspaceException extends Exception {
    private int code;
    private String details;

    public RackspaceException(String message) {
        this(message, null);
    }

    public RackspaceException(int code, String message, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public RackspaceException(Throwable cause) {
        super(cause);
        if (cause instanceof RackspaceException) {
            this.code = ((RackspaceException) cause).code;
            this.details = ((RackspaceException) cause).details;
        }
    }

    public RackspaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Error " + getCode() + ": " + super.getMessage() + "\nDetails: " + getDetails();
    }
}
