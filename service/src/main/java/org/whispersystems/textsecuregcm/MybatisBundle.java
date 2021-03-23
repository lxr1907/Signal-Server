package org.whispersystems.textsecuregcm;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import org.whispersystems.textsecuregcm.mybatis.AbstractMybatisBundle;

public abstract class MybatisBundle<T extends io.dropwizard.Configuration>
        extends AbstractMybatisBundle
        implements ConfiguredBundle<T>, DatabaseConfiguration<T> {

    public MybatisBundle() {
        super();
    }

    // The wonky signature avoids ambiguity with the () and (String...) cases.
    public MybatisBundle(Class<?> mapper, Class<?>... mappers) {
        super(mapper, mappers);
    }

    // The wonky signature avoids ambiguity with the () and (Class...) cases.
    public MybatisBundle(String packageName, String... packageNames) {
        super(packageName, packageNames);
    }

    @Override
    public void run(T configuration, io.dropwizard.setup.Environment environment) throws Exception {
        ManagedDataSource dataSource = getManagedDataSource(configuration, environment);
        run(dataSource, environment);
    }

    private ManagedDataSource getManagedDataSource(T configuration, io.dropwizard.setup.Environment environment) {
        PooledDataSourceFactory dataSourceFactory = getDataSourceFactory(configuration);
        ManagedDataSource dataSource = dataSourceFactory.build(environment.metrics(), getName());
        environment.lifecycle().manage(dataSource);
        return dataSource;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }
}
