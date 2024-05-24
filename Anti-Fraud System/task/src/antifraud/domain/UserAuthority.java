package antifraud.domain;

import antifraud.api.auth.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "user_authority")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthority {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_authority_seq")
    @SequenceGenerator(name = "user_authority_seq", sequenceName = "USER_AUTHORITY_SEQ", allocationSize = 1)
    private Long id;
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy = "userAuthority")
    private List<UserAccount> userAccounts;

    public UserAuthority(Role role) {
        this.name = Role.getAuthorityNameByRole(role);
    }
}