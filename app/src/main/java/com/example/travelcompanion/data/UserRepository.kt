package com.example.travelcompanion.data

class UserRepository(private val userDao: UserDao) {

    sealed class RegisterResult {
        data class Success(val userId: Long) : RegisterResult()
        object EmailAlreadyExists : RegisterResult()
    }

    suspend fun register(email: String, password: String): RegisterResult {
        if (userDao.getByEmail(email) != null) {
            return RegisterResult.EmailAlreadyExists
        }
        val passwordHash = PasswordHasher.hash(password)
        val userId = userDao.insert(User(email = email, passwordHash = passwordHash))
        return RegisterResult.Success(userId)
    }

    suspend fun login(email: String, password: String): User? {
        val user = userDao.getByEmail(email) ?: return null
        val passwordHash = PasswordHasher.hash(password)
        return if (user.passwordHash == passwordHash) user else null
    }
}
