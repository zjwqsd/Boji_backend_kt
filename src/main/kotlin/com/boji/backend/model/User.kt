package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"], name = "unique_user_id")]
)
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", length = 50, unique = true)
    val userId: String? = null,

    @Column(length = 100)
    var nickname: String? = null,

    @Column(length = 100)
    var realname: String? = null,

    @Column(length = 255)
    var address: String? = null,

    @Column(length = 255)
    var company: String? = null,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 100, unique = true)
    val email: String? = null,

    @Column(length = 255, nullable = false)
    var password: String = "",

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    @Column(name = "is_sub_user")
    val isSubUser: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "parent_id")
    val parentUser: User? = null,

    @OneToMany(mappedBy = "parentUser")
    val subUsers: List<User> = emptyList(),

    @OneToMany(mappedBy = "user")
    val pdfPermissions: List<UserPdfPermission> = emptyList()

)