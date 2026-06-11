package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.UserProfile
import javax.inject.Inject

sealed class SignedInGate {
    data object Ready : SignedInGate()
    data object NeedsProfileCompletion : SignedInGate()
}

class DetermineSignedInGateUseCase @Inject constructor() {
    operator fun invoke(profile: UserProfile?): SignedInGate {
        if (profile?.profileComplete != true) {
            return SignedInGate.NeedsProfileCompletion
        }
        return SignedInGate.Ready
    }
}
