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
package org.fabric3.spi.binding.serializer;

/**
 * Provides serialization and deserialization operations for sending data over a remote transport.
 *
 * @version $Revision$ $Date$
 */
public interface Serializer {

    /**
     * Serializes an object.
     *
     * @param clazz   the Java type to serialize to, e.g. a String or byte array
     * @param message the message to serialize
     * @return the serialized bytes
     * @throws SerializationException if a serialization error occurs
     */
    <T> T serialize(Class<T> clazz, Object message) throws SerializationException;

    /**
     * Serializes a fault.
     *
     * @param clazz     the Java type to serialize to, e.g. a String or byte array
     * @param exception the fault instance
     * @return the serialized bytes
     * @throws SerializationException if a serialization error occurs
     */
    <T> T serializeFault(Class<T> clazz, Throwable exception) throws SerializationException;

    /**
     * Deserializes an object.
     *
     * @param clazz      the class representing the expected type
     * @param serialized the object to deserialize. Implementations may support different formats such as base-64 encoded strings or byte arrays.
     * @param <T>        the expected type
     * @return the deserialized object
     * @throws SerializationException if a deserialization error occurs
     */
    <T> T deserialize(Class<T> clazz, Object serialized) throws SerializationException;

    /**
     * Deserializes a an object.
     *
     * @param serialized the fault to deserialize. Implementations may support different formats such as base-64 encoded strings or byte arrays.
     * @return the deserialized fault
     * @throws SerializationException if a deserialization error occurs
     */
    Throwable deserializeFault(Object serialized) throws SerializationException;

}
