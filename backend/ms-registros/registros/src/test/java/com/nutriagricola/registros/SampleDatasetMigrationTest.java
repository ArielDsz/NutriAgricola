package com.nutriagricola.registros;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleDatasetMigrationTest {
    private static final String OLD_PROGRAM = "88888888-8888-4888-8888-888888888888";

    private String databaseUrl() {
        return "jdbc:h2:mem:sample_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS registros\\;SET SCHEMA registros";
    }

    private void migrate(String url, String target) {
        Flyway.configure().dataSource(url, "sa", "")
                .schemas("registros").defaultSchema("registros")
                .locations("classpath:db/migration", "classpath:db/demo")
                .target(target).load().migrate();
    }

    private int count(Connection db, String sql) throws Exception {
        try (var statement = db.createStatement(); var result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }

    @Test
    void freshDatabaseContainsConsistentAssignmentsAnalysesAndPrograms() throws Exception {
        String url = databaseUrl();
        migrate(url, "7");
        try (var db = DriverManager.getConnection(url, "sa", "")) {
            assertEquals(3, count(db, "SELECT COUNT(*) FROM farms"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM crops"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM farm_users"));
            assertEquals(18, count(db, "SELECT COUNT(*) FROM records"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM soil_data"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM water_data"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM foliar_data"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM records r WHERE r.status = 'VALIDATED' AND NOT EXISTS (SELECT 1 FROM record_reviews v WHERE v.record_id = r.id AND v.result = 'VALIDATED')"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM programs"));
            assertEquals(30, count(db, "SELECT COUNT(*) FROM program_fertilizers"));
            assertEquals(24, count(db, "SELECT COUNT(*) FROM program_stages"));
            assertEquals(0, count(db, "SELECT COUNT(*) FROM programs p WHERE NOT EXISTS (SELECT 1 FROM crops c WHERE c.name = p.species)"));
            assertEquals(6, count(db, "SELECT COUNT(*) FROM (SELECT program_id FROM program_stages GROUP BY program_id HAVING SUM(allocation_percent) = 100) valid"));
            assertEquals(9, count(db, "SELECT (SELECT COUNT(*) FROM farms WHERE image_path LIKE 'drawable/%') + (SELECT COUNT(*) FROM crops WHERE image_path LIKE 'drawable/%')"));
            // Uva de mesa, 50 t/ha, eficiencia 90 %, redondeo de la referencia.
            assertEquals(133, count(db, "SELECT nitrogen FROM programs WHERE id = '" + OLD_PROGRAM + "'"));
            assertEquals(78, count(db, "SELECT phosphorus_oxide FROM programs WHERE id = '" + OLD_PROGRAM + "'"));
            assertEquals(130, count(db, "SELECT dose_kg_ha FROM program_fertilizers WHERE program_id = '" + OLD_PROGRAM + "' AND item_order = 0"));
        }
        // Un segundo arranque no duplica datos.
        migrate(url, "7");
        try (var db = DriverManager.getConnection(url, "sa", "")) {
            assertEquals(18, count(db, "SELECT COUNT(*) FROM records"));
        }
    }

    @Test
    void upgradePreservesCustomDataAndEditedSampleProgram() throws Exception {
        String url = databaseUrl();
        migrate(url, "6");
        try (var db = DriverManager.getConnection(url, "sa", ""); var sql = db.createStatement()) {
            sql.executeUpdate("UPDATE program_fertilizers SET dose_kg_ha = 42 WHERE program_id = '" + OLD_PROGRAM + "'");
            sql.executeUpdate("UPDATE farms SET location = 'Ubicación editada' WHERE id = '33333333-3333-4333-8333-333333333333'");
            sql.executeUpdate("INSERT INTO records (id, crop_id, user_id, date, type, status, observations) VALUES ('custom-record', '44444444-4444-4444-8444-444444444444', '11111111-1111-4111-8111-111111111111', '2026-11-22', 'SOIL', 'PENDING', 'Creado manualmente')");
            sql.executeUpdate("INSERT INTO soil_data (record_id, ph) VALUES ('custom-record', 7.3)");
        }
        migrate(url, "7");
        try (var db = DriverManager.getConnection(url, "sa", "")) {
            assertEquals(19, count(db, "SELECT COUNT(*) FROM records"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM records WHERE id = 'custom-record' AND observations = 'Creado manualmente'"));
            assertEquals(42, count(db, "SELECT dose_kg_ha FROM program_fertilizers WHERE program_id = '" + OLD_PROGRAM + "'"));
            assertEquals(1, count(db, "SELECT COUNT(*) FROM farms WHERE location = 'Ubicación editada'"));
        }
    }
}
