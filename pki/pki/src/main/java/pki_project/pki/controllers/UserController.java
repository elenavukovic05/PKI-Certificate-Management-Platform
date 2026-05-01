package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pki_project.pki.dto.UserDTO;
import pki_project.pki.model.enums.Role;
import pki_project.pki.services.UserService;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/regular")
    public List<UserDTO> getUsers() {
        return userService.getRegularUsers() ;
    }
}
