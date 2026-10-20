package cl.talentodigital.appmanageevents.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.talentodigital.appmanageevents.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
    

