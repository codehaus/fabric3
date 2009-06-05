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
package org.fabric3.activemq.broker;

/**
 * Encapsulates persistence adapter configuration for a broker.
 *
 * @version $Revision$ $Date$
 */
public class PersistenceAdapterConfig {
    private Long checkpointInterval;
    private Long cleanupInterval;
    private boolean disableLocking;
    private int indexBinSize;
    private int indexKeySize;
    private int indexPageSize;

    enum Type {
        AMQ, JDBC, JOURNAL, KAHA, MEMORY
    }

    private Type type;
    private boolean syncOnWrite;
    private String maxFileLength;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isSyncOnWrite() {
        return syncOnWrite;
    }

    public void setSyncOnWrite(boolean syncOnWrite) {
        this.syncOnWrite = syncOnWrite;
    }

    public String getMaxFileLength() {
        return maxFileLength;
    }

    public void setMaxFileLength(String maxFileLength) {
        this.maxFileLength = maxFileLength;
    }

    public Long getCheckpointInterval() {
        return checkpointInterval;
    }

    public void setCheckpointInterval(Long checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }

    public Long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public boolean isDisableLocking() {
        return disableLocking;
    }

    public void setDisableLocking(boolean disableLocking) {
        this.disableLocking = disableLocking;
    }

    public int getIndexBinSize() {
        return indexBinSize;
    }

    public void setIndexBinSize(int indexBinSize) {
        this.indexBinSize = indexBinSize;
    }

    public int getIndexKeySize() {
        return indexKeySize;
    }

    public void setIndexKeySize(int indexKeySize) {
        this.indexKeySize = indexKeySize;
    }

    public int getIndexPageSize() {
        return indexPageSize;
    }

    public void setIndexPageSize(int indexPageSize) {
        this.indexPageSize = indexPageSize;
    }
}
