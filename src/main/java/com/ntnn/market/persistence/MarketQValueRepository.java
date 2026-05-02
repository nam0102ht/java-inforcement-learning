package com.ntnn.market.persistence;

import com.ntnn.market.domain.MarketAction;
import com.ntnn.market.domain.MarketState;

import com.ntnn.models.ReinforcementLearning;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketQValueRepository {
    private final JdbcTemplate jdbcTemplate;

    public MarketQValueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(String symbol, Map<ReinforcementLearning.QEntry<MarketState, MarketAction>, Double> qValues) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String sql = """
                INSERT INTO market_q_values
                    (symbol, short_trend, medium_trend, volatility, action, q_value, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (symbol, short_trend, medium_trend, volatility, action)
                DO UPDATE SET q_value = EXCLUDED.q_value, updated_at = EXCLUDED.updated_at
                """;
        LocalDateTime updatedAt = LocalDateTime.now();
        jdbcTemplate.batchUpdate(sql, qValues.entrySet(), 100, (ps, entry) -> {
            MarketState state = entry.getKey().state();
            ps.setString(1, normalizedSymbol);
            ps.setString(2, state.shortTrend().name());
            ps.setString(3, state.mediumTrend().name());
            ps.setString(4, state.volatility().name());
            ps.setString(5, entry.getKey().action().name());
            ps.setDouble(6, entry.getValue());
            ps.setTimestamp(7, Timestamp.valueOf(updatedAt));
        });
    }

    public List<StoredQValue> findBySymbol(String symbol) {
        String sql = """
                SELECT short_trend, medium_trend, volatility, action, q_value
                FROM market_q_values
                WHERE symbol = ?
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new StoredQValue(
                        new MarketState(
                                MarketState.TrendBucket.valueOf(rs.getString("short_trend")),
                                MarketState.TrendBucket.valueOf(rs.getString("medium_trend")),
                                MarketState.VolatilityBucket.valueOf(rs.getString("volatility"))),
                        MarketAction.valueOf(rs.getString("action")),
                        rs.getDouble("q_value")),
                normalizeSymbol(symbol));
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }

    public record StoredQValue(MarketState state, MarketAction action, double qValue) {
    }
}
