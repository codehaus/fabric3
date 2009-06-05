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

/**
 * @version $Revision$ $Date$
 */
public class JsonRpcResponse {

    private String jsonrpc = "2.0";
    private String id;

    private Object result;


    public JsonRpcResponse(String id) {
        this.id = id;
    }

    public JsonRpcResponse(String id, Object result) {
        this.id = id;
        this.result = result;
    }

    public String getjsonrpc() {
        return jsonrpc;
    }

    public Object getResult() {
        return result;
    }

    public String getId() {
        return id;
    }

}
