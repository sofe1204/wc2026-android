package com.techmomentum.wc2026.domain.usecase

import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.data.repository.AuthRepository
import javax.inject.Inject

sealed class SignedInGate {
    data object Ready : SignedInGate()
    data object NeedsEmailVerification : SignedInGate()
    data object NeedsProfileCompletion : SignedInGate()
}

class DetermineSignedInGateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(profile: UserProfile?): SignedInGate {
        if (authRepository.isPasswordAccount && !authRepository.isEmailVerified) {
            return SignedInGate.NeedsEmailVerification
        }
        if (profile?.profileComplete != true) {
            return SignedInGate.NeedsProfileCompletion
        }
        return SignedInGate.Ready
    }
}
