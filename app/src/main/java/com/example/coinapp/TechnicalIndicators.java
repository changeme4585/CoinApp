package com.example.coinapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TechnicalIndicators {

    private List<Double> closingPrices;   // 종가 목록

    // 생성자: 종가 리스트를 받아서 저장
    public TechnicalIndicators(List<Double> closingPrices) {

        this.closingPrices = new ArrayList<>(closingPrices);
    }


    public double calculateRSI(int period) {
        if (closingPrices.size() < period) {
            throw new IllegalArgumentException("종가 데이터가 충분하지 않습니다. (" + period + "개 필요)");
        }

        double gainSum = 0.0;
        double lossSum = 0.0;

        for (int i = closingPrices.size() - period; i < closingPrices.size() - 1; i++) {
            double change = closingPrices.get(i + 1) - closingPrices.get(i);
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }

        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;

        if (avgLoss == 0) {
            return 100.0; // 손실이 없으면 RS가 무한대 -> RSI=100(초강세)
        }

        double rs = avgGain / avgLoss;
        double rsi = 100.0 - (100.0 / (1.0 + rs));
        return rsi;
    }


    public MACDResult calculateMACD(int shortPeriod, int longPeriod, int signalPeriod) {
        if (closingPrices.size() < longPeriod) {
            throw new IllegalArgumentException("종가 데이터가 충분하지 않습니다. (" + longPeriod + "개 필요)");
        }

        // 1) MACD 라인: 단기 EMA - 장기 EMA
        List<Double> shortEma = calculateEMA(closingPrices, shortPeriod);
        List<Double> longEma  = calculateEMA(closingPrices, longPeriod);

        int maxIndex = Math.min(shortEma.size(), longEma.size());
        List<Double> macdLine = new ArrayList<>();
        for (int i = 0; i < maxIndex; i++) {
            macdLine.add(shortEma.get(i) - longEma.get(i));
        }

        // 2) 시그널 라인: MACD 라인의 EMA
        List<Double> signalLine = calculateEMA(macdLine, signalPeriod);

        // 3) 히스토그램: MACD 라인 - 시그널 라인
        int histIndex = Math.min(macdLine.size(), signalLine.size());
        List<Double> histogram = new ArrayList<>();
        for (int i = 0; i < histIndex; i++) {
            histogram.add(macdLine.get(i) - signalLine.get(i));
        }

        return new MACDResult(macdLine, signalLine, histogram);
    }


    public List<Double> calculateSMA(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("기간(period)은 1 이상이어야 합니다.");
        }
        List<Double> smaValues = new ArrayList<>();
        double sum = 0.0;
        for (int i = 0; i < closingPrices.size(); i++) {
            sum += closingPrices.get(i);

            // period 이전까지는 누적
            if (i >= period) {
                sum -= closingPrices.get(i - period);
            }

            // period-1 인덱스부터가 실제 SMA 시작점
            if (i >= period - 1) {
                smaValues.add(sum / period);
            }
        }
        return smaValues;
    }


    public BollingerBands calculateBollingerBands(int period, double k) {
        // period(예: 20), k(예: 2.0)
        List<Double> smaList = calculateSMA(period);
        List<Double> upperBand = new ArrayList<>();
        List<Double> lowerBand = new ArrayList<>();

        for (int i = 0; i < smaList.size(); i++) {
            // SMA가 i번째 값이라면, 실제 인덱스는 i + (period - 1)
            int actualIndex = i + (period - 1);

            // period 구간의 종가들
            double sum = 0.0;
            for (int j = actualIndex - period + 1; j <= actualIndex; j++) {
                sum += closingPrices.get(j);
            }
            double mean = smaList.get(i);

            // 표준편차 계산
            double variance = 0.0;
            for (int j = actualIndex - period + 1; j <= actualIndex; j++) {
                double diff = closingPrices.get(j) - mean;
                variance += diff * diff;
            }
            double stdev = Math.sqrt(variance / period);

            // upper, lower
            upperBand.add(mean + k * stdev);
            lowerBand.add(mean - k * stdev);
        }

        return new BollingerBands(smaList, upperBand, lowerBand);
    }



    public StochasticResult calculateStochastic(int periodK, int periodD) {
        List<Double> kValues = new ArrayList<>();

        for (int i = 0; i < closingPrices.size(); i++) {
            // i번째부터 periodK일 전에 도달 못하면 계산 불가
            if (i < periodK - 1) {
                kValues.add(Double.NaN);
                continue;
            }

            double highestHigh = Double.NEGATIVE_INFINITY;
            double lowestLow = Double.POSITIVE_INFINITY;

            // 최근 periodK 구간
            for (int j = i - periodK + 1; j <= i; j++) {
                double price = closingPrices.get(j);
                if (price > highestHigh) highestHigh = price;
                if (price < lowestLow) lowestLow = price;
            }

            double currentPrice = closingPrices.get(i);
            double k;
            if (highestHigh == lowestLow) {
                // 변동폭 0 방지
                k = 100.0;
            } else {
                k = (currentPrice - lowestLow) / (highestHigh - lowestLow) * 100.0;
            }
            kValues.set(i, k);
        }

        // %D: %K의 단순이동평균(periodD)
        List<Double> dValues = simpleMovingAverage(kValues, periodD);

        return new StochasticResult(kValues, dValues);
    }

    /* ========================================
     *           내부 유틸 메서드
     * ======================================== */
    // 간단히 %K의 이동평균용
    private List<Double> simpleMovingAverage(List<Double> source, int period) {
        List<Double> result = new ArrayList<>(Collections.nCopies(source.size(), Double.NaN));
        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < source.size(); i++) {
            double val = source.get(i);
            if (!Double.isNaN(val)) {
                sum += val;
                count++;
            }

            if (i >= period) {
                double oldVal = source.get(i - period);
                if (!Double.isNaN(oldVal)) {
                    sum -= oldVal;
                    count--;
                }
            }

            if (i >= period - 1) {
                result.set(i, sum / count);
            }
        }

        return result;
    }

    /**
     * 특정 기간의 EMA(지수 이동평균)를 계산하는 메서드 (MACD용)
     */
    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        if (prices.size() == 0) return ema;

        // 초기 SMA
        double sum = 0.0;
        for (int i = 0; i < period && i < prices.size(); i++) {
            sum += prices.get(i);
        }
        double sma = sum / Math.min(period, prices.size());
        ema.add(sma);

        // EMA 공식
        double k = 2.0 / (period + 1.0);
        double prevEma = sma;

        for (int i = period; i < prices.size(); i++) {
            double price = prices.get(i);
            double currentEma = (price - prevEma) * k + prevEma;
            ema.add(currentEma);
            prevEma = currentEma;
        }

        return ema;
    }

    /* ========================================
     *       보조지표 결과를 담는 클래스들
     * ======================================== */

    /** MACD 계산 결과 */
    public static class MACDResult {
        public List<Double> macdLine;    // MACD
        public List<Double> signalLine;  // 시그널
        public List<Double> histogram;   // MACD - 시그널

        public MACDResult(List<Double> macdLine, List<Double> signalLine, List<Double> histogram) {
            this.macdLine = macdLine;
            this.signalLine = signalLine;
            this.histogram = histogram;
        }
    }

    /** Bollinger Bands 계산 결과 */
    public static class BollingerBands {
        public List<Double> middleBand;  // 기준선 (SMA)
        public List<Double> upperBand;
        public List<Double> lowerBand;

        public BollingerBands(List<Double> middleBand, List<Double> upperBand, List<Double> lowerBand) {
            this.middleBand = middleBand;
            this.upperBand = upperBand;
            this.lowerBand = lowerBand;
        }
    }

    /** Stochastic Oscillator 계산 결과 */
    public static class StochasticResult {
        public List<Double> kValues;  // %K
        public List<Double> dValues;  // %D

        public StochasticResult(List<Double> kValues, List<Double> dValues) {
            this.kValues = kValues;
            this.dValues = dValues;
        }
    }

}
