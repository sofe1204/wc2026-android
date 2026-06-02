package com.techmomentum.wc2026.data.firebase

class FunctionsNotDeployedException(
    message: String = "Cloud Functions are not deployed.",
    cause: Throwable? = null,
) : Exception(message, cause)
