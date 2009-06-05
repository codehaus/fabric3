/*
 * Fabric3
 * Copyright © 2008-2009 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.json.format.jsonrpc;

import java.util.List;

/**
 * Represents a JSON RPC 2.0 invocation.
 *
 * @version $Revision$ $Date$
 */
public class JsonRpcRequest {

    private String jsonrpc = "2.0";
    private String method;
    private List<Object> params;
    private String id;

    /**
     * Ctor required for deserialization.
     */
    public JsonRpcRequest() {
    }

    /**
     * Creates a request.
     *
     * @param id     the request id
     * @param method the operation
     * @param params the parameters
     */
    public JsonRpcRequest(String id, String method, List<Object> params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    /**
     * Creates a request with a null parameter.
     *
     * @param id     the request id
     * @param method the operation
     */
    public JsonRpcRequest(String id, String method) {
        this.id = id;
        this.method = method;
    }

    public String getjsonrpc() {
        return jsonrpc;
    }

    public void setjsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
