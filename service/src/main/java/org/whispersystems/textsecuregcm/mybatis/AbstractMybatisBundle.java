package org.whispersystems.textsecuregcm.mybatis;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.whispersystems.textsecuregcm.mybatis.mapper.AccountCoinBalanceMapper;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractMybatisBundle {

    private static final String DEFAULT_NAME = "mybatis";

    private SqlSessionFactory sqlSessionFactory = null;

    private final Consumer<Configuration> configureCallback;

    /**
     * Creates a bundle with no mappers configured automatically. The bundle's Mybatis {@link SqlSessionFactory} can
     * still be configured by overriding {@link #configureMybatis(Configuration)}.
     */
    public AbstractMybatisBundle() {
        this.configureCallback = config -> {
        };
    }

    /**
     * Creates a bundle with an explicit list of mapper interfaces. The bundle's Mybatis {@link SqlSessionFactory} can
     * be further configured by overriding {@link #configureMybatis(Configuration)}.
     *
     * @param mapper  the first mapper to register.
     * @param mappers the remaining mappers to register.
     */
    // The wonky signature avoids ambiguity with the () and (String...) cases.
    public AbstractMybatisBundle(Class<?> mapper, Class<?>... mappers) {
        this.configureCallback = configure(Configuration::addMapper, mapper, mappers);
    }

    /**
     * Creates a bundle by scanning packages for mappers to configure. Mybatis will automatically scan subpackages of
     * the named packages. The bundle's Mybatis {@link SqlSessionFactory} can be further configured by overriding {@link
     * #configureMybatis(Configuration)}.
     *
     * @param packageName  the first package to scan.
     * @param packageNames the remaining packages to scan.
     */
    // The wonky signature avoids ambiguity with the () and (Class...) cases.
    public AbstractMybatisBundle(String packageName, String... packageNames) {
        this.configureCallback = configure(Configuration::addMappers, packageName, packageNames);
    }

    /**
     * Returns the {@link SqlSessionFactory} created by this bundle. Until {@link #run(DataSource,
     * io.dropwizard.setup.Environment)} completes, this will return <code>null</code>.
     * <p>
     * Care is needed when using this bundle in other bundles: this method will return null throughout the
     * <code>initialize</code> phase of Dropwizard's startup. It's generally better to pass this whole bundle to its
     * dependents, and let them obtain session factories at the appropriate time, than it is to take the session factory
     * from this bundle and pass it to other bundles. This awkwardness is inherent in MyBatis' design.
     *
     * @return the configured session factory for this bundle, or <code>null</code> if the bundle hasn't been started
     * yet.
     */
    @Nullable
    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    /**
     * Does nothing on its own, but may be overridden to customize Mybatis configuration more flexibly.
     * <p>
     * This will be called <em>after</em> <ol> <li>the bundle's own suite of type mappers have been registered in the
     * configuration</li> <li>the bundle's own mappers and other objects have been registered in the configuration,
     * and</li> <li>any mappers or packages from the constructor have been registered in the configuration.</li> </ol>
     *
     * @param configuration the MyBatis configuration in flight.
     * @throws Exception if the custom configuration fails. This will be thrown out through {@link
     *                   #run(DataSource, io.dropwizard.setup.Environment)}, which will normally abort application startup.
     */
    protected void configureMybatis(Configuration configuration) throws Exception {
    }

    /**
     * Creates the bundle's MyBatis session factory and registers health checks.
     *
     * @param dataSource  the data source to use for the Mybatis environment.
     * @param environment the Dropwizard environment being started.
     * @throws Exception if MyBatis setup fails for any reason. MyBatis exceptions will be thrown as-is.
     */
    public void run(DataSource dataSource, io.dropwizard.setup.Environment environment) throws Exception {
        sqlSessionFactory = createSqlSessionFactory(dataSource);

        environment.healthChecks().register(getName(), new SqlSessionFactoryHealthCheck(sqlSessionFactory));
        environment.jersey().register(SqlSessionProvider.binder(sqlSessionFactory));
    }

    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        Configuration mybatisConfiguration = buildMybatisConfiguration(dataSource);
        return new SqlSessionFactoryBuilder().build(mybatisConfiguration);
    }

    private Configuration buildMybatisConfiguration(DataSource dataSource) throws Exception {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment mybatisEnvironment = new Environment(getName(), transactionFactory, dataSource);
        Configuration configuration = new Configuration(mybatisEnvironment);
        registerTypeHandlers(configuration.getTypeHandlerRegistry());
        registerOwnMappers(configuration);
        registerClientMappers(configuration);
        configureMybatis(configuration);
        return configuration;
    }

    private void registerTypeHandlers(TypeHandlerRegistry registry) {
        registry.register(UUID.class, new UuidTypeHandler());
        registry.register(java.util.Optional.class, new Java8OptionalTypeHandler());
        registry.register(com.google.common.base.Optional.class, new GuavaOptionalTypeHandler());
        registry.register(URL.class, new UrlTypeHandler());
        registry.register(URI.class, new UriTypeHandler());
    }

    private void registerClientMappers(Configuration configuration) {
        configureCallback.accept(configuration);
    }

    private void registerOwnMappers(Configuration configuration) {
       //configuration.addMappers("org.whispersystems.textsecuregcm.mybatis.mapper");
    }

    /* @SafeVarargs would be appropriate, but it's not permitted on private methods until Java 9. */
    @SuppressWarnings("unchecked")
    private <V> Consumer<Configuration> configure(BiConsumer<Configuration, V> valueApplicator, V value, V... values) {
        return (configuration) -> {
            valueApplicator.accept(configuration, value);
            for (V v : values) {
                valueApplicator.accept(configuration, v);
            }
        };
    }

    protected String getName() {
        return DEFAULT_NAME;
    }
}
