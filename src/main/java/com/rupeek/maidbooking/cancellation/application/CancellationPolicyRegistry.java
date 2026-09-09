package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.cancellation.domain.CancellationPolicyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CancellationPolicyRegistry {
    private final Map<CancellationPolicyType, CancellationPolicy> policies;

    public CancellationPolicyRegistry(List<CancellationPolicy> policies) {
        EnumMap<CancellationPolicyType, CancellationPolicy> registered = new EnumMap<>(CancellationPolicyType.class);
        policies.forEach(policy -> {
            if (registered.put(policy.supports(), policy) != null) {
                throw new IllegalStateException("Duplicate cancellation policy: " + policy.supports());
            }
        });
        this.policies = Map.copyOf(registered);
    }

    public CancellationPolicy get(CancellationPolicyType type) {
        CancellationPolicy policy = policies.get(type);
        if (policy == null) {
            throw new IllegalArgumentException("Unsupported cancellation policy: " + type);
        }
        return policy;
    }
}
