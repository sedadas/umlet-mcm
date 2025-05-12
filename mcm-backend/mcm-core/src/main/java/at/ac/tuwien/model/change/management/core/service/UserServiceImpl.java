package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.UserAlreadyExistsException;
import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserValidationException;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.UserEntityMapper;
import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.graphdb.dao.UserEntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserEntityDAO userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public List<User> getAll() {
        var userEntities = userRepository.findAll();
        return userEntityMapper.fromEntities(userEntities);
    }

    @Override
    public User getUser(String username) throws UserNotFoundException {
        var user = userRepository.findById(username);
        if(user.isEmpty()) {
            throw new UserNotFoundException("User %s not found".formatted(username));
        }
        return userEntityMapper.fromEntity(user.get());
    }

    @Override
    public User createUser(User newUser) {
        if(userRepository.existsById(newUser.getUsername())) {
            throw new UserAlreadyExistsException("User %s already exists".formatted(newUser.getUsername()));
        }
        validateUser(newUser);
        var userEntity = userEntityMapper.toEntity(newUser);
        userEntity.setPassword(new BCryptPasswordEncoder().encode((newUser.getPassword())));
        return userEntityMapper.fromEntity(userRepository.save(userEntity));
    }

    @Override
    public User updateUser(User newUser) throws UserNotFoundException {
        if(!userRepository.existsById(newUser.getUsername())) {
            throw new UserNotFoundException("User %s not found".formatted(newUser.getUsername()));
        }
        validateUser(newUser);
        var userEntity = userEntityMapper.toEntity(newUser);
        userEntity.setPassword(new BCryptPasswordEncoder().encode((newUser.getPassword())));
        return userEntityMapper.fromEntity(userRepository.save(userEntity));
    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        if(!userRepository.existsById(username)) {
            throw new UserNotFoundException("User %s not found".formatted(username));
        }
        userRepository.deleteById(username);
    }

    private void validateUser(User user) throws UserValidationException {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if(!emailValidator.isValid(user.getUsername())) {
            throw new UserValidationException("Username %s is not a valid email".formatted(user.getUsername()));
        }

        /*
         * Password rule:
         *      at least one digit, lowercase letter, uppercase letter, special character
         *      no whitespaces
         *      at least 8 characters
         * See: https://stackoverflow.com/questions/3802192/regexp-java-for-password-validation
         */
        final String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!?@#$%^&+=_])(?=\\S+$).{8,}";

        if(user.getPassword().isEmpty() || !user.getPassword().matches(pattern)) {
            throw new UserValidationException("Password does not adhere to the password policy");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUser(username);
    }
}
