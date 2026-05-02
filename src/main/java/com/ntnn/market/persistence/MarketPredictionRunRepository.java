package com.ntnn.market.persistence;

import com.ntnn.market.api.MarketSignalResponse;

import java.sql.Date;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketPredictionRunRepository {
    private final JdbcTemplate jdbcTemplate;

    public MarketPredictionRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(MarketSignalResponse response) {
        String sql = """
                INSERT INTO market_prediction_runs
                    (symbol, model_type, action, confidence, q_value, last_close,
                     start_date, end_date, iterations, trained_rows, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                response.symbol(),
                response.modelType(),
                response.status().name(),
                response.confidence(),
                response.qValue(),
                response.lastClose(),
                Date.valueOf(response.startDate()),
                Date.valueOf(response.endDate()),
                response.iterations(),
                response.trainedRows(),
                Timestamp.valueOf(response.createdAt()));
    }
}
