import unittest

from service import TimeSeriesAnomalyService, TimeSeriesRequest


class TimeSeriesAnomalyServiceTest(unittest.TestCase):
    def test_returns_request_identity_and_detects_spike(self):
        service = TimeSeriesAnomalyService()
        request = TimeSeriesRequest(
            requestId="exec-communication-test",
            timestamps=[1, 2, 3, 4, 5],
            features={"cpu_usage": [10, 10, 10, 10, 100]},
        )

        response = service.predict(request)

        self.assertEqual("exec-communication-test", response.requestId)
        self.assertEqual(5, response.pointCount)
        self.assertEqual(1, len(response.anomalies))
        self.assertEqual(5, response.anomalies[0].timestamp)
        self.assertEqual("cpu_usage", response.anomalies[0].metricCode)


if __name__ == "__main__":
    unittest.main()
