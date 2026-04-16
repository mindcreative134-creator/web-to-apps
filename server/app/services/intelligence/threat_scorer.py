class ThreatScorer:
    def __init__(self):
        self._scores = {}

    def score_request(self, request):
        return 0.0

    def get_top_threats(self, n: int = 10):
        # Returns an empty list to satisfy dashboard requests
        return []

threat_scorer = ThreatScorer()
