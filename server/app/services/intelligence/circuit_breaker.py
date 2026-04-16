class CircuitBreaker:
    def check(self, service_name: str):
        # Stub for circuit breaker logic
        return True

    def report_failure(self, service_name: str):
        pass

circuit_manager = CircuitBreaker()
