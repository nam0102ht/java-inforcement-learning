package com.ntnn.market.persistence;

import com.ntnn.market.domain.PriceBar;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketPriceBarRepository {
    private final JdbcTemplate jdbcTemplate;

    public MarketPriceBarRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PriceBar> findBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT price_date, close_price
                FROM market_price_bars
                WHERE symbol = ? AND price_date BETWEEN ? AND ?
                ORDER BY price_date
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PriceBar(rs.getDate("price_date").toLocalDate(), rs.getDouble("close_price")),
                normalizeSymbol(symbol),
                Date.valueOf(startDate),
                Date.valueOf(endDate));
    }

    public void saveAll(String symbol, List<PriceBar> bars) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String sql = """
                INSERT INTO market_price_bars (symbol, price_date, close_price)
                VALUES (?, ?, ?)
                ON CONFLICT (symbol, price_date)
                DO UPDATE SET close_price = EXCLUDED.close_price
                """;
        jdbcTemplate.batchUpdate(sql, bars, 100, (ps, bar) -> {
            ps.setString(1, normalizedSymbol);
            ps.setDate(2, Date.valueOf(bar.date()));
            ps.setDouble(3, bar.close());
        });
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
}
