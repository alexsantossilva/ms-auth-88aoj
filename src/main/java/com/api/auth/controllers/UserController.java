package com.api.auth.controllers;

import com.api.auth.dtos.UserDto;
import com.api.auth.models.UserModel;
import com.api.auth.services.UserMessageService;
import com.api.auth.services.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("user")
public class UserController {

    final UserService userService;

    final UserMessageService userMessageService;

    public UserController(UserService userService, UserMessageService userMessageService) {
        this.userMessageService = userMessageService;
        this.userService = userService;
    }

    @ApiOperation(value = "Cria um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuário criado com sucesso"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    @PostMapping
    public ResponseEntity<Object> saveUser(@RequestBody @Valid UserDto userDto) {
        if(userService.existsEmail(userDto.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Email is already in use!");
        }
        userDto.setPassword(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        UserModel user = userService.save(userModel);
        userMessageService.userNotification(user.getFirstName(), user.getLastName(), user.getEmail(), "Usuário criado com sucesso.");
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @ApiOperation(value = "Retorna uma lista de usuários")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retorna a lista de usuários"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll(pageable));
    }

    @ApiOperation(value = "Procura por usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuário retornado com sucesso"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "id") UUID id) {
        Optional<UserModel> userModelOptional = userService.findById(id);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
    }

    @ApiOperation(value = "Exclui um usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User deleted successfully!"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") UUID id) {
        Optional<UserModel> userModelOptional = userService.findById(id);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        UserModel userModel = userModelOptional.get();
        userService.delete(userModel);
        userMessageService.userNotification(userModel.getFirstName(), userModel.getLastName(), userModel.getEmail(), "Usuário excluído com sucesso.");
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully!");
    }

    @ApiOperation(value = "Atualiza os dados de um usuário")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuário exibindo com sucesso"),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id,
                                                    @RequestBody @Valid UserDto userDto) {
        Optional<UserModel> userModelOptional = userService.findById(id);
        if (!userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        //@TODO Primeira maneira
        var userModel = new UserModel();
        userDto.setPassword(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setId(userModelOptional.get().getId());
        userModel.setRegistrationDate(userModelOptional.get().getRegistrationDate());
        UserModel user = userService.save(userModel);
        userMessageService.userNotification(user.getFirstName(), user.getLastName(), user.getEmail(), "Usuário atualizado com sucesso.");
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
