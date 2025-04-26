package at.ac.tuwien.model.change.management.core.service;


import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserValidationException;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.UserEntityMapper;
import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.graphdb.dao.UserEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.entities.UserEntity;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserEntityDAO userEntityDAO;

    @Mock
    private UserEntityMapper userEntityMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    
    private UserEntity userEntity;
    
    @BeforeEach
    public void setup() {
        user = new User();
        user.setUsername("test@example.com");
        user.setPassword("Admin123!");

        userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(hashPassword(user.getPassword()));
    }

    @AfterEach
    public void teardown() {
        Mockito.reset(userEntityDAO, userEntityMapper);
    }


    @Test
    public void testGetAll_givenNoUsers_shouldReturnEmptyList() {
        List<User> res = userService.getAll();
        Assertions.assertTrue(res.isEmpty());
    }

    @Test
    public void testGetAll_givenOneUser_shouldReturnListWithUser() {
        Mockito.when(userEntityDAO.findAll()).thenReturn(List.of(userEntity));
        Mockito.when(userEntityMapper.fromEntities(List.of(userEntity))).thenReturn(List.of(user));

        List<User> res = userService.getAll();
        Assertions.assertEquals(1, res.size());
        Assertions.assertEquals(user.getUsername(), res.getFirst().getUsername());
    }

    @Test
    public void testGetUser_givenNoUsers_shouldThrowUserNotFoundException() {
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getUser(user.getUsername()));
    }

    @Test
    public void testGetUser_givenOneUser_shouldReturnUser() {
        Mockito.when(userEntityDAO.findById(user.getUsername())).thenReturn(Optional.of(userEntity));
        Mockito.when(userEntityMapper.fromEntity(userEntity)).thenReturn(user);

        User res = userService.getUser(user.getUsername());
        Assertions.assertEquals(res.getUsername(), user.getUsername());
        Assertions.assertEquals(hashPassword(res.getPassword()), userEntity.getPassword());
    }

    @Test
    public void testCreateUser_givenInvalidUsername_shouldThrowValidationException() {
        user.setUsername("invalid");
        Assertions.assertThrows(UserValidationException.class, () -> userService.createUser(user));
    }

    @Test
    public void testCreateUser_givenInvalidPassword_shouldThrowValidationException() {
        user.setPassword("invalid");
        Assertions.assertThrows(UserValidationException.class, () -> userService.createUser(user));
    }

    @Test
    public void testCreateUser_givenValidUser_shouldReturnUser() {
        Mockito.when(userEntityMapper.toEntity(user)).thenReturn(userEntity);
        Mockito.when(userEntityDAO.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userEntityMapper.fromEntity(userEntity)).thenReturn(user);

        User res = userService.createUser(user);
        Assertions.assertEquals(res.getUsername(), user.getUsername());
        Assertions.assertEquals(hashPassword(res.getPassword()), userEntity.getPassword());
    }

    @Test
    public void testUpdateUser_givenInvalidPassword_shouldThrowValidationException() {
        Mockito.when(userEntityDAO.existsById(user.getUsername())).thenReturn(true);
        user.setPassword("invalid");
        Assertions.assertThrows(UserValidationException.class, () -> userService.updateUser(user));
    }

    @Test
    public void testUpdateUser_givenNonExistentUser_shouldThrowUserNotFoundException() {
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.updateUser(user));
    }

    @Test
    public void testUpdateUser_givenValidUser_shouldReturnUser() {
        Mockito.when(userEntityDAO.existsById(user.getUsername())).thenReturn(true);
        Mockito.when(userEntityMapper.toEntity(user)).thenReturn(userEntity);

        user.setPassword("Admin1234!");
        userEntity.setPassword(hashPassword(user.getPassword()));

        Mockito.when(userEntityDAO.save(userEntity)).thenReturn(userEntity);
        Mockito.when(userEntityMapper.fromEntity(userEntity)).thenReturn(user);

        User res = userService.updateUser(user);
        Assertions.assertEquals(res.getUsername(), user.getUsername());
        Assertions.assertEquals(hashPassword(res.getPassword()), userEntity.getPassword());
    }

    @Test
    public void testDeleteUser_givenNonExistentUser_shouldThrowUserNotFoundException() {
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.deleteUser(user.getUsername()));
    }

    @Test
    public void testDeleteUser_givenExistentUser_shouldNotThrowException() {
        Mockito.when(userEntityDAO.existsById(user.getUsername())).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> userService.deleteUser(user.getUsername()));
    }

    private String hashPassword(String password) {
        // TODO: Take this from an environment variable
        final String salt = "Na5vucushuan9Ooj";
        String salted = salt + password;
        return Hashing.sha256().hashString(salted, StandardCharsets.UTF_8).toString();
    }
}
