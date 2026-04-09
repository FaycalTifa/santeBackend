package com.uab.sante.configSecurity;

import com.uab.sante.entities.Role;
import com.uab.sante.entities.Utilisateur;
import com.uab.sante.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== loadUserByUsername ===");
        System.out.println("Email recherché: " + email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.err.println("❌ Utilisateur non trouvé avec l'email: " + email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
                });

        System.out.println("✅ Utilisateur trouvé: " + utilisateur.getEmail());
        System.out.println("Mot de passe stocké: " + utilisateur.getPassword());
        System.out.println("Nombre de rôles: " + utilisateur.getRoles().size());

        // ✅ Afficher les rôles
        for (Role role : utilisateur.getRoles()) {
            System.out.println("  - Rôle: " + role.getCode());
        }

        // ✅ Convertir les rôles en autorités Spring Security
        Collection<SimpleGrantedAuthority> authorities = utilisateur.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toList());

        System.out.println("Authorités créées: " + authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")));

        // ✅ Retourner un User Spring Security avec les autorités
        return new User(
                utilisateur.getEmail(),
                utilisateur.getPassword(),
                authorities
        );
    }
}
