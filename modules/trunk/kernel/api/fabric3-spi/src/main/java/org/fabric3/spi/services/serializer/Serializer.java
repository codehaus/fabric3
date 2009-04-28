/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
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
package org.fabric3.spi.services.serializer;

/**
 * Provides serialization and deserialization operations for sending data over a remote transport.
 *
 * @version $Revision$ $Date$
 */
public interface Serializer {

    /**
     * Serializes an object.
     *
     * @param message the message to serialize
     * @return the serialized bytes
     * @throws SerializationException if a serialization error occurs
     */
    byte[] serialize(Object message) throws SerializationException;

    /**
     * Deserializes a byte array.
     *
     * @param clazz the class of the expected type
     * @param bytes the bytes to deserialize
     * @param <T>   the expected type
     * @return a deserialized object
     * @throws SerializationException if a deserialization error occurs
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws SerializationException;

}
