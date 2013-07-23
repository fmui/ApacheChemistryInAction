/*******************************************************************************
 * Copyright 2012 Manning Publications Co.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.manning.cmis.theblend.android.utils;

/**
 * Provides information from CMIS Server.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class CmisResult<T> {

    /** Exception from CMIS Server. */
    private Exception exception;

    /** Result data of CMIS. */
    private T data;

    public CmisResult(Exception exception, T data) {
        super();
        this.exception = exception;
        this.data = data;
    }

    /**
     * @return true if an error has been created in server side.
     */
    public boolean hasException() {
        return (exception != null);
    }

    /**
     * @return Returns the exception raised during the execution.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return the data result
     */
    public T getData() {
        return data;
    }

}
