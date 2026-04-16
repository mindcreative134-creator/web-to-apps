class AbuseDetector:
    def check_behavior(self, user_id: int):
        return False

    def get_suspicious_modules(self, n: int = 10):
        # Returns empty list to satisfy dashboard
        return []

abuse_detector = AbuseDetector()
