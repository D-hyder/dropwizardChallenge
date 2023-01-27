package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class DropWizardChallengeConfiguration extends Configuration {
    // TODO: implement service configuration
    @JsonProperty("database")
    private DatabaseConfiguration database = new DatabaseConfiguration();
    public DatabaseConfiguration getDatabase() {
        return database;
    }
}
