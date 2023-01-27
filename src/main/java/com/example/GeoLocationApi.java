package com.example;
import ch.qos.logback.core.db.dialect.SQLiteDialect;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.DBIFatory;
import org.skife.jdbi.v2.Handle;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.Location;

public class GeolocationAPI extends Application<DropWizardChallengeConfiguration> {
    private Connection conn;
    private final ConcurrentHashMap<String, Location> cache = new ConcurrentHashMap<>();
    private final long CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(1);

    public static void main(String[] args) throws Exception {
        new GeolocationAPI().run(args);
    }
    @Override
    public String getName() {
        return "geolocation";
    }

    @Override
    public void initialize(final Bootstrap<DropWizardChallengeConfiguration> bootstrap) {
        // Add any additional initialization here
    }

    /*
    @Override
    public void run(GeolocationAPIConfig config, Environment env) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:geolocation.db");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS location (ip TEXT PRIMARY KEY, city TEXT NOT NULL, region TEXT NOT NULL, country TEXT NOT NULL, postal_code TEXT, latitude REAL NOT NULL, longitude REAL NOT NULL, timezone TEXT NOT NULL, utc_offset TEXT NOT NULL, country_code TEXT NOT NULL, region_code TEXT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        final Client client = ClientBuilder.newClient();
        env.jersey().register(new GeolocationResource(client, conn));
    }*/

    @Override
    public void run(final DropWizardChallengeConfiguration configuration,
                    final Environment environment) {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDatabase().getDataSourceFactory(), "sqlite");
        final SQLiteDialect dialect = new SQLiteDialect();
        jdbi.registerArgumentFactory(new SqlDateArgumentFactory());
        jdbi.registerArgumentFactory(new SqlTimestampArgumentFactory());
        jdbi.registerContainerFactory(new SqlContainerFactory());
        jdbi.registerMapper(new SqlMapper());
        try(Handle handle = jdbi.open()){
            handle.execute("CREATE TABLE IF NOT EXISTS geolocation (ip VARCHAR(15) PRIMARY KEY, country VARCHAR(50), region VARCHAR(50), city VARCHAR(50), latitude DOUBLE, longitude DOUBLE, timezone VARCHAR(50), isp VARCHAR(50), org VARCHAR(50), as VARCHAR(50), query VARCHAR(50))");
        }
        final GeolocationResource resource = new GeolocationResource(jdbi, dialect);
        final GeolocationCacheResource cacheResource = new GeolocationCacheResource(resource);
        environment.jersey().register(cacheResource);
    }

    @Path("/geolocation/{ip}")
    @Produces(MediaType.APPLICATION_JSON)
    public class GeolocationResource {
        private final Client client;
        private final Connection conn;

        public GeolocationResource(Client client, Connection conn) {
            this.client = client;
            this.conn = conn;
        }

        @GET
        public Response getGeolocation(@PathParam("ip") String ip) {
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM location WHERE ip = ?");
                stmt.setString(1, ip);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Response.ok(rs.getString("ip")+" "+rs.getString("city")+" "+rs.getString("region")+" "+rs.getString("country")+" "+rs.getString("postal_code")+" "+rs.getDouble("latitude")+" "+rs.getDouble("longitude")+" "+rs.getString("timezone")+" "+rs.getString("utc_offset")+" "+rs.getString("country_code")+" "+rs.getString("region_code")).build();
                }
                else {
                    Response externalApiResponse = client.target("http://ip-api.com/json/" + ip)
                            .request(MediaType.APPLICATION_JSON)
                            .get();
                    String response = externalApiResponse.readEntity(String.class);

                    PreparedStatement insert = conn.prepareStatement("INSERT INTO location (ip, city, region, country, postal_code, latitude, longitude, timezone, utc_offset, country_code, region_code) VALUES (?,?,?,?,?,?,?,?,?,?,?);");
                    insert.setString(1,ip);
                    insert.setString(2,city);
                    insert.setString(3,region);
                    insert.setString(4,country);
                    insert.setString(5,postal_code);
                    insert.setDouble(6,latitude);
                    insert.setDouble(7,longitude);
                    insert.setString(8,timezone);
                    insert.setString(9,utc_offset);
                    insert.setString(10,country_code);
                    insert.setString(11,region_code);
                    insert.execute();
                    return externalApiResponse;
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return Response.serverError().build();
            }
        }
    }
}
