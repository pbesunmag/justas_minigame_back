package com.jousting.rpg.repository;

import com.jousting.rpg.domain.model.UserAccount;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio en memoria para el almacenamiento de cuentas de usuario.
 */
@Repository
public class UserAccountRepository {

    private final Map<String, UserAccount> usersByUsername = new ConcurrentHashMap<>();
    private final Map<Long, UserAccount> usersById = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserAccount save(UserAccount userAccount) {
        if (userAccount.getId() == null) {
            userAccount.setId(idGenerator.getAndIncrement());
        }
        usersByUsername.put(userAccount.getUsername().toLowerCase(), userAccount);
        usersById.put(userAccount.getId(), userAccount);
        return userAccount;
    }

    public Optional<UserAccount> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username.toLowerCase()));
    }

    public Optional<UserAccount> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(id));
    }

    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return usersByUsername.containsKey(username.toLowerCase());
    }

    public void clear() {
        usersByUsername.clear();
        usersById.clear();
    }
}
