package hu.unicon.szakdoga.model

data class User(
    val id: Long,
    val username: String,
    val name: String,
    val role: String
) {
    fun getRoleDisplayName(): String {
        return when (role.uppercase()) {
            "ADMIN" -> "Admin"
            "MUSZI", "MUESZI" -> "Műszakvezető"
            else -> "Dolgozó"
        }
    }
}

data class LoginRequest(
    val username: String
)
