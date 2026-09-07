"""BentoML serving contract for the deterministic time-series anomaly check."""

import math

import bentoml
from pydantic import BaseModel, Field, model_validator


class TimeSeriesRequest(BaseModel):
    contractVersion: str = "1.0"
    requestId: str = Field(min_length=1)
    modelCode: str = "zscore-communication-test"
    timestamps: list[int] = Field(min_length=3, max_length=20_000)
    features: dict[str, list[float]] = Field(min_length=1, max_length=22)
    threshold: float = Field(default=1.5, gt=0)

    @model_validator(mode="after")
    def validate_feature_lengths(self):
        # Every feature is a parallel series aligned with the timestamp vector.
        expected = len(self.timestamps)
        if any(len(values) != expected for values in self.features.values()):
            raise ValueError("each feature must contain one value per timestamp")
        return self


class AnomalyPoint(BaseModel):
    metricCode: str
    index: int
    timestamp: int
    value: float
    score: float


class TimeSeriesResponse(BaseModel):
    contractVersion: str
    requestId: str
    engine: str
    modelCode: str
    modelVersion: str
    pointCount: int
    anomalies: list[AnomalyPoint]


@bentoml.service(name="time-series-anomaly-verification")
class TimeSeriesAnomalyService:
    """A deterministic fake model used only to verify the serving contract."""

    @bentoml.api
    def predict(self, request: TimeSeriesRequest, /) -> TimeSeriesResponse:
        anomalies: list[AnomalyPoint] = []
        for metric_code, values in request.features.items():
            # The communication-test model scores each point by its absolute Z-score.
            mean = sum(values) / len(values)
            variance = sum((value - mean) ** 2 for value in values) / len(values)
            standard_deviation = math.sqrt(variance)

            for index, value in enumerate(values):
                score = 0.0 if standard_deviation == 0 else abs(value - mean) / standard_deviation
                if score >= request.threshold:
                    # Keep the original index and timestamp so the platform can link the alert to a sample.
                    anomalies.append(
                        AnomalyPoint(
                            metricCode=metric_code,
                            index=index,
                            timestamp=request.timestamps[index],
                            value=value,
                            score=round(score, 6),
                        )
                    )

        # Echo the contract metadata so the Java adapter can reject mismatched responses.
        return TimeSeriesResponse(
            contractVersion=request.contractVersion,
            requestId=request.requestId,
            engine="BentoML",
            modelCode=request.modelCode,
            modelVersion="communication-test-1.0.0",
            pointCount=len(request.timestamps),
            anomalies=anomalies,
        )
