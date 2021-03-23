package org.whispersystems.textsecuregcm;

import com.loginbox.dropwizard.mybatis.MybatisBundle;
import io.dropwizard.db.DataSourceFactory;

public class MybatisBundleImpl extends MybatisBundle<WhisperServerConfiguration> {
    public MybatisBundleImpl(String packageName, String... packageNames) {
        super(packageName, packageNames);
    }
    @Override
    public DataSourceFactory getDataSourceFactory(WhisperServerConfiguration configuration) {
        return configuration.getDataSourceFactory();
    }
}
