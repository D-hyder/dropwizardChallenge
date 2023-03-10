Connection conn = DriverManager.getConnection("jdbc:sqlite:Schema.sql");'US', 'United States', 'CA', 'California', 'Mountain View', '94043', 37.3860, -122.0838, 'America/Los_Angeles', 'Google LLC', 'Google LLC', 15169, 'success');

Statement stmt = conn.createStatement();
stmt.executeUpdate(
    "CREATE TABLE location (id INTEGER PRIMARY KEY AUTOINCREMENT, ip TEXT NOT NULL, city TEXT NOT NULL, region TEXT NOT NULL, country TEXT NOT NULL, postal_code TEXT, latitude REAL NOT NULL, longitude REAL NOT NULL, timezone TEXT NOT NULL, utc_offset TEXT NOT NULL, country_code TEXT NOT NULL, region_code TEXT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
);

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
