package info.benjaminhill.geochat.domain.repository

import info.benjaminhill.geochat.domain.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
}
