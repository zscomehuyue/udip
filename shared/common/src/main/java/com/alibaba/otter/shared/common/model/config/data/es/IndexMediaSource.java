package com.alibaba.otter.shared.common.model.config.data.es;

import com.alibaba.otter.shared.common.model.config.Transient;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Properties;

@NoArgsConstructor
@AllArgsConstructor
public class IndexMediaSource extends DataMediaSource {
    private static final long serialVersionUID = 6286323857113071880L;
    private String clusterName;
    private String clusterNodes;
    @Transient
    private Properties properties;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getUrl() {
        return "clusterNodes=" + clusterNodes + ",clusterName=" + clusterName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }
}
