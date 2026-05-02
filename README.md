# Java With AI

Spring Boot application for market signal generation using Yahoo Finance price history.

The app supports two model paths:

- Q-learning reinforcement learning for `BUY`, `SELL`, or `HOLD` decisions.
- DeepLearning4J supervised neural network classification for `BUY`, `SELL`, or `HOLD`.

Market data, Q-learning values, and prediction runs are persisted in PostgreSQL through Docker Compose.

Important: this project produces trading signals from historical data. It does not predict the market exactly and should not be treated as financial advice.

## Tech Stack

- Java 25
- Spring Boot 4
- Gradle
- PostgreSQL 17
- Docker Compose
- Yahoo Finance chart data
- DeepLearning4J / ND4J
- Cucumber Gherkin tests

## Run With Docker Compose

Start the app and database:

```bash
docker compose up --build
```

The API runs on:

```text
http://localhost:8080
```

PostgreSQL runs on:

```text
localhost:5432
database: market_training
username: market_user
password: market_password
```

## API

## Training Lifecycle

Yes, the model must be trained before the signal is meaningful.

For Q-learning:

1. The app fetches Yahoo Finance historical prices or reuses cached prices from PostgreSQL.
2. The `MarketEnvironment` turns price movement into states and rewards.
3. The Q-learning agent trains for the requested number of episodes.
4. Learned Q-values are saved in `market_q_values`.
5. The latest market state is evaluated and returned as `BUY`, `SELL`, or `HOLD`.

For DeepLearning4J:

1. The app fetches or reuses historical prices.
2. It builds return and volatility features.
3. DL4J trains a neural network for the requested number of epochs.
4. The latest feature row is classified as `BUY`, `SELL`, or `HOLD`.

The `/api/market/train` endpoint trains and returns the latest signal. The `/api/market/signal` endpoint currently also trains before returning a signal, so you can use either endpoint while developing.

### Get A Q-Learning Signal

```bash
curl 'http://localhost:8080/api/market/signal?symbol=AAPL&iterations=500'
```

Example response:

```json
{
  "symbol": "AAPL",
  "modelType": "Q_LEARNING",
  "status": "HOLD",
  "lastClose": 280.14,
  "confidence": null,
  "qValue": 0.0019,
  "startDate": "2021-05-02",
  "endDate": "2026-05-02",
  "iterations": 500,
  "trainedRows": 1256,
  "createdAt": "2026-05-02T09:44:33"
}
```

### Get A DeepLearning4J Signal

```bash
curl 'http://localhost:8080/api/market/signal?symbol=AAPL&model=dl4j&iterations=50'
```

### Force Data Refresh From Yahoo Finance

```bash
curl 'http://localhost:8080/api/market/signal?symbol=AAPL&refresh=true'
```

### Train With POST

```bash
curl -X POST 'http://localhost:8080/api/market/train' \
  -H 'Content-Type: application/json' \
  -d '{
    "symbol": "AAPL",
    "modelType": "q",
    "iterations": 500,
    "refresh": false
  }'
```

Optional request fields:

- `symbol`: stock ticker, for example `AAPL`.
- `modelType`: `q` or `dl4j`.
- `startDate`: ISO date, for example `2021-01-01`.
- `endDate`: ISO date, for example `2026-05-02`.
- `iterations`: Q-learning episodes or DL4J epochs.
- `refresh`: when `true`, fetches fresh Yahoo Finance data and updates the database.

## Database Tables

The schema is created from `src/main/resources/schema.sql`.

- `market_price_bars`: cached Yahoo Finance daily close prices.
- `market_q_values`: persisted Q-learning state/action values.
- `market_prediction_runs`: generated prediction/training results.

Stored data is reused for future training unless `refresh=true`.

## Local Development

Start only PostgreSQL:

```bash
docker compose up -d postgres
```

Run the Spring Boot app locally:

```bash
./gradlew bootRun
```

Run Q-learning through CLI mode:

```bash
./gradlew bootRun --args='AAPL 500'
```

Run DeepLearning4J through CLI mode:

```bash
./gradlew bootRun --args='dl4j AAPL 50'
```

## Tests

Run unit and Cucumber behavior tests:

```bash
./gradlew test blackboxTest
```

Test configuration uses in-memory H2 with PostgreSQL compatibility mode from `src/test/resources/application.yml`, so tests do not require Docker.

## CI

GitHub Actions workflow: `.github/workflows/ci.yml`.

The workflow runs automatically on:

- Pushes to `develop`
- Pull requests targeting `develop`

It can also be started manually from the Actions tab with `workflow_dispatch`. When manually triggered, GitHub lets you select the branch to run from, so you can build the current feature branch image before merging.

The CI pipeline:

1. Sets up Java 25.
2. Runs `./gradlew test blackboxTest --no-daemon`.
3. Builds the Docker image from `Dockerfile`.

The image build validates the Dockerfile but does not push to a registry yet.

## Notes

- Yahoo Finance can rate-limit automated requests. Reusing stored database data helps reduce repeated calls.
- DeepLearning4J/ND4J uses native libraries. On newer Java versions you may see warnings from JavaCPP or ND4J; the app can still run successfully.
- On Apple Silicon, the build includes the `macosx-arm64` ND4J native artifact.
