package com.example.coinapp;

import java.util.ArrayList;
import java.util.List;

public class TechnicalIndicators { // 보조지표 관련 함수

    private List<Double> closingPrices;   // 종가 목록

    // 생성자: 종가 리스트를 받아서 저장
    public TechnicalIndicators(List<Double> closingPrices) {
        // 방어적 복사(defensive copy)를 위해 내부적으로 새로운 리스트를 만든다.
        this.closingPrices = new ArrayList<>(closingPrices);
    }

    /**
     * RSI 계산 메서드 (간단 버전)
     * @param period RSI를 계산할 기간 (일반적으로 14)
     * @return 마지막 시점의 RSI 값(또는 전체 시간대의 RSI 목록)
     */
    public double calculateRSI(int period) {
        if (closingPrices.size() < period) {
            throw new IllegalArgumentException("종가 데이터가 충분하지 않습니다. (" + period + "개 필요)");
        }

        double gainSum = 0.0;
        double lossSum = 0.0;

        // 최근 period 구간의 상승분, 하락분 합산
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

        // 0으로 나눌 수 없으므로 예외처리
        if (avgLoss == 0) {
            return 100.0; // 이론적으로는 초강세
        }

        double rs = avgGain / avgLoss;
        double rsi = 100.0 - (100.0 / (1.0 + rs));

        return rsi;
    }

    /**
     * MACD 계산 메서드 (간단 버전)
     * @param shortPeriod 일반적으로 12
     * @param longPeriod  일반적으로 26
     * @param signalPeriod 일반적으로 9
     * @return MACD 값(MACD라인, 시그널라인, 히스토그램)을 담고 있는 객체
     */
    public MACDResult calculateMACD(int shortPeriod, int longPeriod, int signalPeriod) {
        if (closingPrices.size() < longPeriod) {
            throw new IllegalArgumentException("종가 데이터가 충분하지 않습니다. (" + longPeriod + "개 필요)");
        }

        // 1) MACD 라인 = 단기 EMA(12) - 장기 EMA(26)
        List<Double> shortEma = calculateEMA(closingPrices, shortPeriod);
        List<Double> longEma  = calculateEMA(closingPrices, longPeriod);

        // 두 지표가 시작되는 위치가 다르므로, 긴 쪽 길이에 맞춰서 계산
        int maxIndex = Math.min(shortEma.size(), longEma.size());
        List<Double> macdLine = new ArrayList<>();
        for (int i = 0; i < maxIndex; i++) {
            macdLine.add(shortEma.get(i) - longEma.get(i));
        }

        // 2) 시그널 라인 = MACD 라인의 EMA(9)
        List<Double> signalLine = calculateEMA(macdLine, signalPeriod);

        // 3) 히스토그램 = MACD 라인 - 시그널 라인
        maxIndex = Math.min(macdLine.size(), signalLine.size());
        List<Double> histogram = new ArrayList<>();
        for (int i = 0; i < maxIndex; i++) {
            histogram.add(macdLine.get(i) - signalLine.get(i));
        }

        return new MACDResult(macdLine, signalLine, histogram);
    }

    /**
     * 특정 기간의 EMA(지수 이동평균)를 계산하는 메서드 (간단 버전)
     * @param prices 종가 목록
     * @param period EMA 기간
     * @return EMA가 적용된 새로운 리스트
     */
    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        if (prices.size() == 0) return ema;

        // 초기 SMA(단순 이동평균)로 시작
        double sum = 0.0;
        int startIndex = 0;
        for (int i = 0; i < period && i < prices.size(); i++) {
            sum += prices.get(i);
        }
        double sma = sum / Math.min(period, prices.size());
        ema.add(sma);

        // EMA 공식: EMA(today) = (Price(today) - EMA(yesterday)) * K + EMA(yesterday)
        // K = 2 / (period + 1)
        double k = 2.0 / (period + 1.0);

        // 이전 EMA값
        double prevEma = sma;
        for (int i = period; i < prices.size(); i++) {
            double price = prices.get(i);
            double currentEma = (price - prevEma) * k + prevEma;
            ema.add(currentEma);
            prevEma = currentEma;
        }

        return ema;
    }

    /**
     * MACD 계산 결과를 담는 내부 클래스
     */
    public static class MACDResult {
        public List<Double> macdLine;
        public List<Double> signalLine;
        public List<Double> histogram;

        public MACDResult(List<Double> macdLine, List<Double> signalLine, List<Double> histogram) {
            this.macdLine = macdLine;
            this.signalLine = signalLine;
            this.histogram = histogram;
        }
    }
}
