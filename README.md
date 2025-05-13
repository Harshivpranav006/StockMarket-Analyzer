# Real-Time Stock Market Analyzer

A real-time stock market analysis application that provides live stock data, price charts, and market news.

## Features

- Real-time stock price updates
- Interactive price charts
- Market news feed
- WebSocket support for live data
- Responsive design

## Prerequisites

- Java 11 or higher
- Maven
- Alpha Vantage API key (for stock data)
- News API key (for market news)

## Setup

1. Clone the repository
2. Get your API keys:
   - Sign up for Alpha Vantage API: https://www.alphavantage.co/
   - Sign up for News API: https://newsapi.org/
3. Update `src/main/resources/application.properties` with your API keys:
   ```properties
   alphavantage.api.key=YOUR_ALPHA_VANTAGE_API_KEY
   newsapi.api.key=YOUR_NEWS_API_KEY
   ```

## Running the Application

1. Build the project:

   ```bash
   mvn clean install
   ```

2. Run the application:

   ```bash
   mvn spring-boot:run
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## Usage

1. Enter a stock symbol (e.g., AAPL, GOOGL, MSFT) in the search box
2. View real-time stock data and price chart
3. Browse market news in the news feed section

## Technologies Used

- Frontend:

  - HTML5
  - CSS3
  - JavaScript
  - Chart.js
  - Bootstrap 5

- Backend:
  - Java 11
  - Spring Boot
  - WebSocket
  - OkHttp
  - JSON

## API Integration

- Alpha Vantage API for stock data
- News API for market news

## License

This project is licensed under the MIT License.
