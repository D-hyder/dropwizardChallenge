package com.example;

import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


public class DatabaseConfiguration extends JdbiFactory {
    public DatabaseConfiguration() {
        super();
    }
    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }
}