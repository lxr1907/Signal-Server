package org.whispersystems.textsecuregcm;

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
